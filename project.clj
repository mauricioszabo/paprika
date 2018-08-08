(defproject paprika "0.1.1"
  :description "Simple utility functions"
  :url "http://github.com/mauricioszabo/paprika"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.13.0"]
                 [prismatic/schema "1.1.7"]]

  :profiles {:dev {:src-paths ["dev"]
                   :dependencies [[midje "1.8.3"]
                                  [org.clojure/test.check "0.9.0"]]
                   :plugins [[lein-midje "3.2.1"]]}}

  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]])
