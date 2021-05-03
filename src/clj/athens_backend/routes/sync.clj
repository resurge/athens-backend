(ns athens-backend.routes.sync
  (:require
    [athens-backend.datsync-utils :as dat-s]
    [athens-backend.db.core :as db]
    [clj-time.coerce :as time-co]
    [clj-time.core :as time-c]
    [com.rpl.specter :as s]
    [compojure.core :refer (defroutes GET POST)]
    [datahike.api :as d]
    [ring.middleware.defaults :as ring-defaults]
    [taoensso.sente :as sente]
    [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
    [taoensso.timbre :as log]))


;;-------------------------------------------------------------------
;;--- Sockets setup ---

(declare channel-socket)
(def current-presence (atom nil))


(defn broadcast! [event]
  (doseq [uid (:any @(:connected-uids channel-socket))]
    ((:send-fn channel-socket) uid event)))


(defn start-websocket! []
  #_:clj-kondo/ignore
  (defonce channel-socket
    (sente/make-channel-socket-server!
      (get-sch-adapter) {:packer :edn :csrf-token-fn #(and % (identity "x"))})))


(defroutes ring-routes
  (GET "/chsk" ring-req ((:ajax-get-or-ws-handshake-fn channel-socket) ring-req))
  (POST "/chsk" ring-req ((:ajax-post-fn channel-socket) ring-req)))


(def main-ring-handler
  (ring-defaults/wrap-defaults
    ring-routes (assoc-in ring-defaults/site-defaults
                          [:security :anti-forgery] false)))


(defmulti -event-msg-handler :id)


(defn event-msg-handler
  [{:as ev-msg}]
  (-event-msg-handler ev-msg))


(defmethod -event-msg-handler :default
  [{:keys [id ?data]}]
  (log/info (str "Unhandled event:" id ?data)))


(defmethod -event-msg-handler :chsk/uidport-open
  [{:keys [uid client-id]}]
  (log/info "New connection:" uid client-id))


(defmethod -event-msg-handler :chsk/uidport-close
  [{:keys [uid]}]
  (log/info "Disconnected:" uid))


(defmethod -event-msg-handler :user/details
  [{:keys [?data]}]
  (swap! current-presence
         (fn [curr]
           (assoc curr (:random/id ?data)
                       (assoc ?data :last-seen-ts (time-co/to-long (time-c/now)))))))


(defn start-router! []
  #_:clj-kondo/ignore
  (defonce router (sente/start-chsk-router!
                    (:ch-recv channel-socket) event-msg-handler)))


;;-------------------------------------------------------------------
;;--- presence ticker ---

(defn ticker!
  "Send current presence to all clients. Remove stale clients
   who haven't sent their ping in the last 10seconds"
  []
  (while true
    (Thread/sleep 500)
    (try
      ;; remove stale users
      (swap! current-presence
             (fn [curr]
               (->> curr (s/select
                           [s/ALL #(< (- (time-co/to-long (time-c/now))
                                         (-> % second :last-seen-ts))
                                      10000)])
                    (into {}))))
      (broadcast! [:presence/now @current-presence])
      (catch Exception ex
        (println ex)))))


(defn start-broadcast-ticker! []
  #_:clj-kondo/ignore
  (defonce ticker-thread
    (doto (Thread. ticker!)
      (.start))))


;;-------------------------------------------------------------------
;;--- Transaction related --


(def !last-tx-uid (atom nil))


(defmethod -event-msg-handler :dat.sync.client/tx
  [{:keys [?data]}]
  (let [[user-uid tx-data] ?data]
    ;; apply-remote-tx! is synchronous
    ;; i.e once txn is done watch fn is triggered that broadcasts
    ;; i.e we always know who triggered a txn broadcast
    (reset! !last-tx-uid user-uid)
    (dat-s/apply-remote-tx! tx-data)))


(defn get-bootstrap-datoms
  "Get all current datoms for current datahike implementation,
   this will transact all data to client"
  []
  (->> (d/datoms @db/dh-conn :eavt)
       ;; no need to transact schema as we do it already for front end
       ;; while setting up datascript
       (remove (fn [[_e a]]
                 (contains? #{:db/cardinality :db/valueType :db/ident :db/unique}
                            a)))
       (mapv (fn [[e a v _t]]
               [:db/add e a v]))))


(defmethod -event-msg-handler :dat.sync.client/request-bootstrap
  [{:keys [uid]}]
  ((:send-fn channel-socket)
   uid [:dat.sync.client/bootstrap (get-bootstrap-datoms)]))


(defn handle-transaction-report!
  "Send all transactions to all clients"
  [tx-report]
  (broadcast! [:dat.sync.client/recv-remote-tx
               [@!last-tx-uid
                (dat-s/tx-report->datoms tx-report)]]))


(defn start-db-watch! []
  (d/listen db/dh-conn :send-tx handle-transaction-report!))
