(ns athens-sync.datsync-utils
  (:require
    [athens-sync.db.core :as db]
    [datahike.api :as d]
    [clojure.set :as set]))


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
;;--- invariants ---

;; Using a datalog comes with great extensibility to entity.
;; But the downside of it is losing the power
;; of invariants to data

;; Invariants are things that are always true for an entity
;; in our case
;; 1. every block has an order unless it's node/title
;;    a. block/order of each entity is unique for a given set of children
;; 2. Every block has a block/uid
;;    b. add block id if not present

;; working with these
;; get the db after, find the ids and check the invariants apply
;; assumption db-before is always correct

(defn gen-block-uid
  []
  (subs (str (java.util.UUID/randomUUID)) 27))


(defn get-all-ids-involved
  [{:keys [tx-data]}]
  (->> tx-data
       (remove #(= (:a %) :db/txInstant))
       (map :e) set))


(defn check-block-uid
  [{:keys [db-after]} id]
  (when-not (->> id (d/pull db-after '[*])
                 :block/uid)
    [[:db/add id :block/uid (gen-block-uid)]]))


(defn check-block-order
  [{:keys [db-after]} id]
  (when-not (->> id (d/pull db-after '[*])
                 :block/order)
    [[:db/add id :block/order 0]]))


(defn check-children-order
  [{:keys [db-after] :as tx-report} id]
  (let [children
        (->> id (d/pull db-after
                        '[{:block/children [*]}])
             :block/children
             (sort-by :block/order))

        monotonous-inc? (every? #{1} (map #(- (:block/order %1)
                                              (:block/order %2))
                                          (rest children) children))]
    (when-not monotonous-inc?
      (let [ids-that-changed (set/intersection
                               (get-all-ids-involved tx-report)
                               (->> children (map :db/id) set))
            curr-max (or (some->> children
                                  (remove #(contains? ids-that-changed (:db/id %)))
                                  (apply max-key :block/order)
                                  :block/order))]
        (->> ids-that-changed seq
             (map-indexed
               (fn [i id]
                 [:db/add id :block/order
                  (+ 1 i curr-max)])))))))


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
                              (string? (second %)))))

        {:keys [tempids] :as tx-report} (d/with @db/dh-conn tx)
        eid->temp (set/map-invert tempids)
        changed-ids (get-all-ids-involved (d/with @db/dh-conn tx))]
    (d/transact db/dh-conn
                (->> (concat tx
                             (mapcat
                               (fn [fn]
                                 (mapcat (partial fn tx-report) changed-ids))
                               [check-block-uid check-block-order
                                check-children-order]))
                     (remove nil?)
                     (map (fn [[op e a v]]
                            [op (or (eid->temp e) e) a (or (eid->temp v) v)]))))))


(defn tx-report->datoms
  "Convert tx-report from datahike into eavt datoms"
  [tx-report]
  (->> tx-report :tx-data
       (map (fn [[e a v _t b]]
              [({true :db/add false :db/retract} b) e a v]))))