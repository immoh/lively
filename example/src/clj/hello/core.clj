(ns hello.core
  (:require [compojure.core :refer [defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  (route/resources "/"))

(def app
  (handler/site app-routes))
