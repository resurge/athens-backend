(ns athens-sync.routes.sync
  (:require
    [ring.util.response]
    [ring.middleware.defaults :as ring-defaults]
    [compojure.core :as comp :refer (defroutes GET POST)]
    [taoensso.sente :as sente]
    [clj-time.core :as time-c]
    [clj-time.coerce :as time-co]
    [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter) :as http]
    [clojure.core.async :as async :refer (<! go-loop)]
    [com.rpl.specter :as s]))


(declare channel-socket)
(def current-presence (atom nil))


(defn start-websocket! []
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
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))


(defmethod -event-msg-handler :default
  [{:keys [event id ?data ring-req ?reply-fn send-fn]}]
  (println (str "Unhandled event:" id ?data)))


(defmethod -event-msg-handler :chsk/uidport-open
  [{:keys [uid client-id]}]
  (println "New connection:" uid client-id))


(defmethod -event-msg-handler :chsk/uidport-close
  [{:keys [uid]}]
  (println "Disconnected:" uid))


(defmethod -event-msg-handler :user/details
  [{:keys [?data]}]
  (swap! current-presence (fn [curr]
                            (assoc curr (:random/id ?data)
                                        (assoc ?data :last-seen-ts (time-co/to-long (time-c/now)))))))


(defn start-router! []
  (defonce router (sente/start-chsk-router! (:ch-recv channel-socket) event-msg-handler)))


(defn ticker! []
  (while true
    (Thread/sleep 1000)
    (try
      (doseq [uid (:any @(:connected-uids channel-socket))]
        (swap! current-presence (fn [curr]
                                  (->> curr (s/select
                                              [s/ALL #(< (- (time-co/to-long (time-c/now))
                                                            (-> % second :last-seen-ts))
                                                         10000)])
                                       (into {}))))

        ((:send-fn channel-socket) uid
         [:presence/now @current-presence]))
      (catch Exception ex
        (println ex)))))


(defn start-broadcast-ticker! []
  (defonce ticker-thread
    (doto (Thread. ticker!)
      (.start))))

