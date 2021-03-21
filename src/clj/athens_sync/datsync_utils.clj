(ns athens-sync.datsync-utils
  (:require
    [athens-sync.db.core :as db]
    [dat.sync.server]
    [datahike.api :as d]))


(defn apply-remote-tx!
  "Takes a client transaction and transacts it"
  [tx]
  (let [tx' (mapv (partial dat.sync.server/translate-tx-form
                           @db/dh-conn dat.sync.server/tempid-map) tx)]
    (d/transact db/dh-conn tx')))


(defn tx-report->datoms
  "Convert tx-report from datahike into eavt datoms"
  [tx-report]
  (->> tx-report :tx-data
       (map (fn [[e a v _t b]]
              [({true :db/add false :db/retract} b) e a v]))))