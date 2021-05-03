(ns athens-backend.core
  (:require
    [athens-backend.config :refer [env]]
    [athens-backend.db.core :as db]
    [athens-backend.nrepl :as nrepl]
    [athens-backend.routes.sync :as sync]
    [clojure.tools.cli :refer [parse-opts]]
    [mount.core :as mount]
    [org.httpkit.server :as http-kit]
    [taoensso.timbre :as log])
  (:gen-class))


;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))}))))


(def cli-options
  [["-p" "--http-port HTTP_PORT" "HTTP Port number"
    :default 1337
    :parse-fn #(Integer/parseInt %)]])


(mount/defstate ^{:on-reload :noop} http-server
  :start
  (http-kit/run-server
    (var sync/main-ring-handler)
    ;; heroku sets sysenv as port to which app has to bind
    ;; failing to bind to it will stop the process and
    ;; the dyno completely
    {:port (some #(get-in env %) [[:port] [:options :http-port]])})

  :stop
  (http-server))


(mount/defstate ^{:on-reload :noop} repl-server
  :start
  (when (env :nrepl-port)
    (nrepl/start {:bind (env :nrepl-bind)
                  :port (env :nrepl-port)}))
  :stop
  (when repl-server
    (nrepl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))


(defn start-app [args]
  (db/init-db!)
  (sync/start-websocket!)
  (sync/start-router!)
  (sync/start-broadcast-ticker!)
  (sync/start-db-watch!)
  (let [parsed-args (parse-opts args cli-options)]
    (log/info :parsed-args (pr-str parsed-args))
    (doseq [component (-> parsed-args
                          mount/start-with-args
                          :started)]
      (log/info component "started")))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (start-app args))
