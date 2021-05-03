(ns athens-backend.env
  (:require [taoensso.timbre :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[athens-backend started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[athens-backend has shut down successfully]=-"))
   :middleware identity})
