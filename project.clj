(defproject lively "0.1.3-SNAPSHOT"
  :description "ClojureScript live coding with ease"
  :url "http://github.com/immoh/lively"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [org.clojure/clojurescript "0.0-2665"]]
                   :plugins [[lein-cljsbuild "1.0.4"]]}}
  :cljsbuild {:builds {:main {:source-paths ["src"]
                              :compiler {:optimizations :none}}}})
