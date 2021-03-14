(ns athens-sync.handler
  (:require
    [athens-sync.middleware :as middleware]
    [athens-sync.layout :refer [error-page]]
    [athens-sync.routes.sync :as h]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [athens-sync.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(def app-routes
  (compojure.core/routes (var h/main-ring-handler)))

(defn app []
  (middleware/wrap-base #'app-routes))
