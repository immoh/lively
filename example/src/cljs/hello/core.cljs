(ns hello.core
  (:require [lively.core :as lively]))

(defn greet []
  (js/alert "Hello!"))

(lively/start "/js/hello.js" {:on-reload #(.log js/console "Reloaded!")})
