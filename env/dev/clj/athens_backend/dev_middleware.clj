(ns athens-backend.dev-middleware
  (:require
    [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-dev [handler]
  (-> handler
      wrap-reload))
