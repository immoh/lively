(defproject hello "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2356"]
                 [compojure "1.2.0"]
                 [lively "0.1.0-SNAPSHOT"]]
  :plugins [[lein-ring "0.8.12"]
            [lein-cljsbuild "1.0.3"]]
  :ring {:handler hello.core/app
         :browser-uri "/index-dev.html"}
  :cljsbuild {:builds {:main {:source-paths ["src/cljs"]
                              :compiler {:output-to     "resources/public/js/hello.js"
                                         :output-dir    "resources/public/js/out"
                                         :optimizations :none}}}})
