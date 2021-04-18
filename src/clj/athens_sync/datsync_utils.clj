(ns athens-sync.datsync-utils
  (:require
    [athens-sync.db.core :as db]
    [datahike.api :as d]))


;;-------------------------------------------------------------------
;;--- re-used from datsync-server ---


(defmulti translate-tx-form
  (fn [db tempid-map [op]] op))


(defmethod translate-tx-form :db/add
  [db tempid-map [op e a v]]
  [op (tempid-map e) a (tempid-map db a v)])


(defmethod translate-tx-form :db/retract
  [db tempid-map [op e a v]]
  [op (tempid-map e) a (tempid-map db a v)])


(defmethod translate-tx-form :db.fn/retractEntity
  [db tempid-map [op e]]
  [op (tempid-map e)])


(defn reverse-ref-attribute?
  [attr-kw]
  (= \_ (first (name attr-kw))))


(defn tempid-map
  ([e]
   (if (and (integer? e) (< e 0))
     (d/tempid :db.part/user e) e))
  ([db a e]
   (if (and (keyword? a)
            (or (reverse-ref-attribute? a)
                (d/q '[:find ?a .
                       :in $ ?a-ident
                       :where
                       [?a :db/ident ?a-ident]
                       [?a :db/valueType :db.type/ref]]
                     db a))
            (integer? e)
            (< e 0))
     (d/tempid :db.part/user e) e)))


;;-------------------------------------------------------------------
;;--- exposed api ---


(defn apply-remote-tx!
  "Takes a client transaction and transacts it"
  [tx]
  (let [tx (->> tx
                (mapv (partial translate-tx-form
                               @db/dh-conn tempid-map))
                (remove #(and (contains? #{:db/retract :db/retractEntity}
                                         (first %))
                              (string? (second %)))))]
    (d/transact db/dh-conn tx)))


(defn tx-report->datoms
  "Convert tx-report from datahike into eavt datoms"
  [tx-report]
  (->> tx-report :tx-data
       (map (fn [[e a v _t b]]
              [({true :db/add false :db/retract} b) e a v]))))