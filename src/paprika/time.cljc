(ns paprika.time
  (:refer-clojure :exclude [second extend < <= > >= =])
  #?(:clj
     (:require [clojure.walk :as walk]
               [clj-time.core :as time]
               [clj-time.format :as time-format]
               [clj-time.coerce :as time-coerce]
               [clojure.core :as clj])
     :cljs
     (:require [clojure.walk :as walk]
               [cljs-time.core :as time]
               [cljs-time.format :as time-format]
               [cljs-time.coerce :as time-coerce]
               [clojure.core :as clj]))
  #?(:clj
     (:import [java.sql Timestamp]
              [org.joda.time DateTime])))

(def ago #'time/ago)
(def date-midnight #'time/date-midnight)
(def date-time #'time/date-time)
(def days #'time/days)
(def days? #'time/days?)
(def default-time-zone #'time/default-time-zone)
(def deprecated #'time/deprecated)
(def do-at* #'time/do-at*)
(def earliest #'time/earliest)
(def end #'time/end)
(def epoch #'time/epoch)
(def extend #'time/extend)
(def first-day-of-the-month #'time/first-day-of-the-month)
(def floor #'time/floor)
(def from-now #'time/from-now)
(def hours #'time/hours)
(def hours? #'time/hours)
(def interval #'time/interval)
(def last-day-of-the-month #'time/last-day-of-the-month)
(def latest #'time/latest)
(def local-date #'time/local-date)
(def local-date-time #'time/local-date-time)
(def millis #'time/millis)
(def mins-ago #'time/mins-ago)
(def minus #'time/minus)
(def minutes #'time/minutes)
(def minutes? #'time/minutes?)
(def months #'time/months)
(def months? #'time/months?)
(def now #'time/now)
(def number-of-days-in-the-month #'time/number-of-days-in-the-month)
(def overlap #'time/overlap)
(def overlaps? #'time/overlaps?)
(def plus #'time/plus)
(def seconds #'time/seconds)
(def seconds? #'time/seconds?)
(def start #'time/start)
(def time-now #'time/time-now)
(def time-zone-for-offset #'time/time-zone-for-offset)
(def today #'time/today)
(def today-at #'time/today-at)
(def today-at-midnight #'time/today-at-midnight)
(def utc #'time/utc)
(def weeks #'time/weeks)
(def weeks? #'time/weeks?)
(def within? #'time/within?)
(def years #'time/years)
(def years? #'time/years?)
(def yesterday #'time/yesterday)

#?(:cljs
   (do
     (def at-midnight #'time/at-midnight)
     (def conversion-error #'time/conversion-error)
     (def date? #'time/date?)
     (def default-ms-fn #'time/default-ms-fn)
     (def from-default-time-zone #'time/from-default-time-zone)
     (def from-utc-time-zone #'time/from-utc-time-zone)
     (def interval? #'time/interval?)
     (def offset-ms-fn #'time/offset-ms-fn)
     (def period #'time/period)
     (def period-fn #'time/period-fn)
     (def period-fns #'time/period-fns)
     (def period-type? #'time/period-type?)
     (def period? #'time/period?)
     (def periods #'time/periods)
     (def static-ms-fn #'time/static-ms-fn)
     (def to-default-time-zone #'time/to-default-time-zone)
     (def to-utc #'time/to-utc-time-zone)
     (def minute time/minute)
     (def hour time/hour)
     (def day time/day)
     (def second time/second)
     (def day-of-week time/day-of-week)
     (def month time/month)
     (def year time/year)
     (def week-number-of-year time/week-number-of-year)
     (def sec time/sec)
     (def milli time/milli))
   :clj
   (doseq [[name fun] (ns-publics 'clj-time.core)]
     (let [meta (-> fun meta
                    (dissoc :file :name :ns :line :column)
                    str
                    (clojure.string/replace-first #"(:arglists )" "$1'"))]
       (load-string
        (str "(def ^" meta " " name " " fun ")")))))

(defn = [ & args]
  (reduce (fn [sofar [f s]] (and sofar
                                 #?(:clj (time/equal? f s) :cljs (.equals f s))))
          true
          (partition 2 1 args)))

(defn < [ & args]
  (reduce (fn [sofar [f s]] (and sofar (time/before? f s)))
          true
          (partition 2 1 args)))

(defn <= [ & args]
  (reduce (fn [sofar [f s]] (and sofar (or (time/equal? f s)
                                           (time/before? f s))))
          true
          (partition 2 1 args)))

(defn > [ & args]
  (reduce (fn [sofar [f s]] (and sofar (time/after? f s)))
          true
          (partition 2 1 args)))

(defn >= [ & args]
  (reduce (fn [sofar [f s]] (and sofar (or (time/equal? f s)
                                           (time/after? f s))))
          true
          (partition 2 1 args)))

(def format-for
  (let [f (fn [format-str]
            (time-format/formatter format-str))]
    (memoize f)))

#?(:clj
   (defn to-local [date]
     (time/to-time-zone date (default-time-zone)))
   :cljs
   (defn to-local [date]
     (time/to-default-time-zone date)))

#?(:clj
   (defn as-local [date]
     (time/from-time-zone date (default-time-zone)))
   :cljs
   (defn as-local [date]
     (time/from-default-time-zone date)))

#?(:clj
   (defn to-utc [date]
     (time/to-time-zone date time/utc)))
#?(:clj
   (defn as-utc [date]
     (time/from-time-zone date time/utc))
   :cljs
   (defn as-utc [date]
     (time/from-utc-time-zone date)))

(defn parse-local [string format]
  #?(:clj
     (.toDateTime (time-format/parse-local (format-for format) string))
     :cljs
      (-> format
          format-for
          (time-format/parse-local string))))

(defn parse [string format]
  #?(:clj
     (->> string (time-format/parse (format-for format)) as-utc)
     :cljs
     (time-format/parse (format-for format) string)))

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

#?(:clj
   (do
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
       `(-> ~string from-string-local to-sql))))

; ;; Test helper
; (defn same-as? [other-time]
;   (fn [time]
;     (or (time/equal? other-time time)
;         (with-meta
;           {:notes [(if (after? time other-time)
;                      "Expected is lower than result"
;                      "Expected is greater than result")]}
;           {:midje/data-laden-falsehood true}))))

(def Time
  #?(:clj org.joda.time.DateTime
     :cljs (.. js/goog -date -DateTime)))

#?(:clj
   (defmethod print-method DateTime [d ^java.io.Writer w]
     (if (-> d .getZone .getID (clj/= "UTC"))
       (.write w "#time/utc ")
       (.write w "#time/local "))
     (.write w (str "\"" d "\"")))

   :cljs
   (extend-protocol IPrintWithWriter
     Time
     (-pr-writer [d writer _]
       (let [dt-str (str d)
             norm (.replace dt-str
                            #"(\d{4})(\d\d)(\d\d)T(\d\d)(\d\d)(\d\d)"
                            "$1-$2-$3T$4:$5:$6")
             mil (->> d milli (str "00") (take-last 3) (apply str))]
         (-write writer (str "#time/utc \"" norm "." mil "Z\""))))))
