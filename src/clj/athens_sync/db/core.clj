(ns athens-sync.db.core
  (:require [datahike.api :as d]
            [datascript.transit :as dt :refer [write-transit-str]]
            [mount.core :refer [args defstate]]))

(declare conn)
(def cfg {:store {:backend :file :path "/tmp/dh-store"}})


(def schema
  [{:db/ident       :schema/version
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/uid
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
   {:db/ident       :block/title
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
   {:db/ident       :block/string
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :node/title
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
   {:db/ident       :attrs/lookup
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/many}
   {:db/ident       :block/children
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :block/refs
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :create/time
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :edit/time
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/open
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/order
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :from-history
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident       :from-undo-redo
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}])



(defn init!
  []
  (let [_ (d/delete-database cfg)
        _ (d/create-database cfg)
        c (d/connect cfg)]
    (d/transact c schema)
    (->> "/Users/abhinav/Documents/personal/newx/index.transit"
         slurp dt/read-transit-str (into {}) :eavt
         (mapcat (fn [[id attr val _txn sig?]]
                   [[(if sig? :db/add :db/retract) id attr val]]))
         (d/transact c))
    (def conn c)))

