(ns paprika.time
  (:refer-clojure :exclude [second extend])
  (:require [clojure.walk :as walk]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clj-time.coerce :as time-coerce])
  (:import [java.sql Timestamp]
           [org.joda.time DateTime]))

(doseq [[name fun] (ns-publics 'clj-time.core)]
  (let [meta (-> fun meta
                 (dissoc :file :name :ns :line :column)
                 str
                 (clojure.string/replace-first #"(:arglists )" "$1'"))]
    (load-string
     (str "(def ^" meta " " name " " fun ")"))))

(def format-for
  (let [f (fn [format-str]
            (time-format/formatter-local format-str))]
    (memoize f)))

(defn to-local [date]
  (time/to-time-zone date (default-time-zone)))

(defn as-local [date]
  (time/from-time-zone date (default-time-zone)))

(defn to-utc [date]
  (time/to-time-zone date time/utc))
(defn as-utc [date]
  (time/from-time-zone date time/utc))

(defn parse-local [string format]
  (time-format/parse (format-for format) string))

(defn parse [string format]
  (-> string (parse-local format) as-utc))

(defn unparse [date format]
  (->> date
       to-utc
       (time-format/unparse (format-for format))))

(defn unparse-local [date format]
  (->> date
       to-local
       (time-format/unparse (format-for format))))

(def from-string #'time-coerce/from-string)
(defn from-string-macro [str] `(from-string ~str))

(defn from-string-local [string] (-> string from-string as-local))
(defn from-string-local-macro [str] `(from-string-local ~str))

(def to-sql time-coerce/to-sql-time)
(defn from-sql [time]
  (cond
    (coll? time) (walk/postwalk #(cond-> % (instance? Timestamp %) from-sql) time)
    (instance? java.sql.Timestamp time) (time-coerce/from-sql-time time)
    :java.sql.Date (time-coerce/from-sql-date time)))

(defn from-timestamp-macro [string]
  `(-> ~string from-string to-sql))
(defn from-timestamp-local-macro [string]
  `(-> ~string from-string-local to-sql))

;; Test helper
(defn same-as? [other-time]
  (fn [time]
    (or (time/equal? other-time time)
        (with-meta
          {:notes [(if (after? time other-time)
                     "Expected is lower than result"
                     "Expected is greater than result")]}
          {:midje/data-laden-falsehood true}))))

(defmethod print-method DateTime [d ^java.io.Writer w]
  (if (-> d .getZone .getID (= "UTC"))
    (.write w "#time/utc ")
    (.write w "#time/local "))
  (.write w (str "\"" d "\"")))
