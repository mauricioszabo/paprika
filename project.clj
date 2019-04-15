(defproject paprika "0.1.2"
  :description "Simple utility functions"
  :url "http://github.com/mauricioszabo/paprika"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-time "0.13.0"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [prismatic/schema "1.1.10"]]

  :profiles {:dev {:src-paths ["dev"]
                   :dependencies [[midje "1.8.3"]
                                  [org.clojure/test.check "0.9.0"]
                                  [nubank/matcher-combinators "0.8.3"]]
                   :plugins [[lein-midje "3.2.1"]]}}

  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]])
