(ns lively.core
  (:require [cljs.core.async :as async :refer [<! >! chan close! timeout]]
            goog.net.jsloader
            goog.net.XhrIo
            goog.string
            goog.Uri)
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce initialized? (atom nil))

(defn headers-changed? [cache uri headers]
  (when-not (= (get @cache uri) headers)
    (swap! cache assoc uri headers)))

(defn put-and-close! [port val]
  (go
    (>! port val)
    (close! port)))

(defn make-unique [uri]
  (.makeUnique (goog.Uri/parse uri)))

(defn <reload-js-file [uri]
  (let [channel (chan)]
    (-> (goog.net.jsloader/load (make-unique uri))
        (.addCallbacks (fn [& _] (put-and-close! channel :ok))
                       (fn [& _] (put-and-close! channel :failed))))
    channel))

(defn pick-headers [target]
  (let [headers ["Last-Modified" "Content-Length"]]
    (zipmap headers (map #(.getResponseHeader target %) headers))))

(defn <headers [uri]
  (let [channel (chan)]
    (goog.net.XhrIo/send (make-unique uri)
                         (fn [event]
                           (let [target (.-target event)
                                 response {:success? (.isSuccess target)
                                           :headers  (pick-headers target)}]
                             (put-and-close! channel response)))
                         "HEAD")
    channel))

(defn <headers-for-uris [uris]
  (async/map list (map <headers uris)))

(defn resolve-uri [rel]
  (.toString (goog.Uri/resolve (.-basePath js/goog) rel)))

(defn has-unhandled-require? [remaining {:keys [requires]}]
  (some (set (map :name remaining)) requires))

(defn topo-sort [deps]
  (loop [sorted [] remaining deps]
    (if-let [next-deps (seq (remove (partial has-unhandled-require? remaining) remaining))]
      (recur (concat sorted next-deps) (remove (set next-deps) remaining))
      sorted)))

(defn immutable? [{:keys [name]}]
  (or (#{"goog"
         "cljs.core"
         "an.existing.path"
         "dup.base"
         "far.out"
         "ns"
         "someprotopackage.TestPackageTypes"
         "svgpan.SvgPan"
         "testDep.bar"} name)
       (some (partial goog.string/startsWith name) ["goog." "cljs." "clojure." "fake." "proto2."])))

(defn expand-transitive-deps [all-deps deps]
  (loop [deps (set deps)]
    (let [expanded-deps (into deps (filter (comp (set (mapcat :requires deps)) :name) all-deps))]
      (if (= deps expanded-deps)
        deps
        (recur expanded-deps)))))

(defn get-all-deps []
  (let [deps (.-dependencies_ js/goog)
        requires (js->clj (.-requires deps))]
    (map (fn [[name path]]
           {:name name :uri (resolve-uri path) :requires (set (keys (get requires path)))})
         (js->clj (.-nameToPath deps)))))

(defn get-reloadable-deps []
  (let [all-deps (get-all-deps)]
    (->> all-deps
         (remove immutable?)
         (expand-transitive-deps all-deps))))

;; Thanks, lein-figwheel!
(defn patch-goog-base []
  (set! (.-provide js/goog) (.-exportPath_ js/goog))
  (set! (.-CLOSURE_IMPORT_SCRIPT (.-global js/goog)) (fn [file]
                                                       (when (.inHtmlDocument_ js/goog)
                                                         (goog.net.jsloader/load file)))))

(defn check-optimization-level []
  (when-not (and js/goog (.-dependencies_ js/goog))
    (throw (js/Error. "Lively requires that ClojureScript is compiled with :optimizations set to :none"))))

(defn check-protocol []
  (when-not (= "http" (-> js/goog .-basePath goog.Uri/parse .getScheme))
    (throw (js/Error. "Lively requires that JavaScript files are loaded over HTTP protocol"))))

(defn start
  "Start polling for changes in compiled JavaScript files and reload them when they change.
   Takes location of the main JavaScript file and optionally map of options with following keys:

     :polling-rate  Milliseconds to sleep between polls. Defaults to 1000.
     :on-reload     Callback function to call after files have been reloaded.

   Throws an error if ClojureScript hasn't been compiled with optimization level  :none, or
   if JavaScript files are not loaded over HTTP.

   Returns nil."
  ([main-js-location]
   (start main-js-location nil))
  ([main-js-location {:keys [polling-rate on-reload]}]
   (when-not @initialized?
     (reset! initialized? true)
     (check-optimization-level)
     (check-protocol)
     (patch-goog-base)
     (go
       (let [cljs-deps-uri (resolve-uri  "../cljs_deps.js")
             main-js-location (if (:success? (<! (<headers cljs-deps-uri)))
                                cljs-deps-uri
                                main-js-location)
             deps (get-reloadable-deps)
             headers-cache (atom (let [uris (conj (distinct (map :uri (remove immutable? deps)))
                                                  main-js-location)]
                                   (zipmap uris (map :headers (<! (<headers-for-uris uris))))))
             loaded-cljs-deps (atom (set (map :name (filter immutable? deps))))]
         (while true
           (let [{:keys [success? headers]} (<! (<headers main-js-location))]
             (when (and success? (headers-changed? headers-cache main-js-location headers))
               (<! (<reload-js-file main-js-location))
               (let [deps (get-reloadable-deps)
                     uris (->> deps
                               (remove (comp @loaded-cljs-deps :name))
                               (topo-sort)
                               (map :uri)
                               (distinct))
                     headers-for-uris (zipmap uris (<! (<headers-for-uris uris)))]
                 (doseq [uri uris
                         :let [{:keys [success? headers]} (get headers-for-uris uri)]
                         :when (and success? (headers-changed? headers-cache uri headers))]
                   (<! (<reload-js-file uri)))
                 (swap! loaded-cljs-deps into (map :name (filter immutable? deps))))
               (when on-reload (on-reload))))
           (<! (timeout (or polling-rate 1000))))))
     nil)))
