(ns athens-sync.env
  (:require [taoensso.timbre :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[athens-sync started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[athens-sync has shut down successfully]=-"))
   :middleware identity})
