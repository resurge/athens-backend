(ns athens-sync.db.core
  (:require [clojure.java.shell :refer [sh]]
            [datahike.api :as d]
            [datascript.transit :as dt]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as log]))


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
    :db/cardinality :db.cardinality/one}
   {:db/ident       :page/sidebar
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}])


(def DATAHIKE-DB-PATH "srv/db/athens-sync")


(def cfg {:store {:backend :file :path DATAHIKE-DB-PATH}})


(defstate dh-conn
          :start
          (let [transit-file "resources/index.transit"]
            (try
              (let [_    (sh "mkdir" "-p" DATAHIKE-DB-PATH)
                    _    (d/create-database cfg)
                    conn (d/connect cfg)]
                (d/transact conn schema)
                (->> transit-file
                     slurp dt/read-transit-str (into {}) :eavt
                     (mapcat (fn [[id attr val _txn sig?]]
                               [[(if sig? :db/add :db/retract) id attr val]]))
                     (d/transact conn)))
              (catch Exception e
                (log/info "Db already exists")))
            (d/connect cfg))

          :stop
          (when dh-conn
            (d/release dh-conn)))


(defn init-db!
  []
  (mount/start [#'dh-conn]))
