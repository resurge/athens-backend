(defproject athens-sync "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [selmer "1.0.2"]
                 [markdown-clj "1.10.1"]
                 [ring "1.8.1"]
                 [expound "0.8.9"]
                 [ring-middleware-format "0.7.2"]
                 [metosin/ring-http-response "0.6.5"]
                 [compojure "1.6.1"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.3.2"]
                 [mount "0.1.16"]
                 [cprop "0.1.6"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.reader "1.0.5"]
                 [luminus-nrepl "0.1.6"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [luminus-immutant "0.1.9"]
                 [luminus-transit "0.1.2"]
                 [luminus-undertow "0.1.10"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [org.clojure/tools.reader "1.1.1"]
                 [metosin/muuntaja "0.6.8"]
                 [metosin/muuntaja "0.6.8"]
                 [metosin/reitit "0.5.12"]
                 [metosin/ring-http-response "0.9.2"]
                 [org.clojure/data.xml "0.2.0-alpha2"]

                 ;; Additional dependencies
                 [clj-http "2.2.0"]
                 [com.cemerick/url "0.1.1"]
                 [clj-time "0.15.2"]
                 [swiss-arrows "1.0.0"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [ring-cors "0.1.13"]
                 [com.climate/claypoole "1.1.4"]
                 [org.clojure/core.async "0.4.490"]
                 [crypto-random "1.2.0"]
                 [aleph "0.4.3"  :exclusions [io.netty/netty-all]]
                 [bk/ring-gzip "0.2.1"]
                 [com.cognitect/transit-clj "0.8.313"]
                 [clj-jwt "0.1.1"]
                 [org.clojure/data.codec "0.1.1"]

                 [org.clojure/tools.logging "0.4.0"]
                 [com.taoensso/sente "1.16.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [http-kit "2.4.0"]
                 [ring-logger "1.0.1"]
                 [ring.middleware.conditional "0.2.0"]
                 [com.fzakaria/slf4j-timbre "0.3.8"]
                 [org.slf4j/log4j-over-slf4j "1.7.14"]
                 [org.slf4j/jul-to-slf4j "1.7.14"]
                 [org.slf4j/jcl-over-slf4j "1.7.14"]

                 [com.rpl/specter "1.1.2"]
                 [io.sentry/sentry "1.7.5"]
                 [org.clojars.akiel/async-error "0.3"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/core.memoize "0.8.2"]
                 [joda-time "2.10.6"]]

  :min-lein-version "2.5.0"
  :min-java-version "1.8"
  
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot athens-sync.core

  :plugins [[lein-tools-deps "0.4.5"]
            [lein-cprop "1.0.1"]
            [lein-cljsbuild "1.1.7"]
            [min-java-version "0.1.0"]
            [lein-pprint "1.1.1"]
            [lein-aot-order "0.1.0"]
            [lein-set-version "0.4.1"]
            [lein-kibit "0.1.6"]
            [lein-doo "0.1.10"]]

  :profiles
  {:uberjar       {:omit-source    true
                   :aot            :all
                   :uberjar-name   "athens-sync.jar"
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn"]
                   :dependencies   [[prone "1.0.2"]
                                    [ring/ring-mock "0.3.0"]
                                    [ring/ring-devel "1.4.0"]
                                    [com.billpiel/sayid "0.0.16"]
                                    [com.bhauman/rebel-readline "0.1.4"]
                                    [vvvvalvalval/scope-capture "0.3.2"]
                                    [vvvvalvalval/scope-capture-nrepl "0.3.1"]
                                    [spyscope "0.1.6"]
                                    [cider/cider-nrepl "0.25.1"]]

                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user
                                    :timeout 120000}}
   :project/test  {:jvm-opts       ["-Dconf=test-config.edn"]
                   :resource-paths ["env/test/resources"]}
   :profiles/dev  {}
   :profiles/test {}})
