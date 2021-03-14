(ns athens-sync.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [athens-sync.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[athens-sync started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[athens-sync has shut down successfully]=-"))
   :middleware wrap-dev})
