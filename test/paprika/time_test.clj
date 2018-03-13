(ns paprika.time-test
  (:require [midje.sweet :refer :all]
            [clj-time.coerce :as time-coerce]
            [paprika.time :as time]))

(facts "about converting times"
  (fact "will convert times"
    (time/parse "12/10/1982 08:20:30" "dd/MM/yyyy hh:mm:ss")
    => (time/from-string "1982-10-12T08:20:30"))

  (fact "will convert to local time"
    (time/parse-local "12/10/1982 10:20:30" "dd/MM/yyyy hh:mm:ss")
    => (time/from-string-local "1982-10-12T10:20:30"))

  (fact "will interpret times as local"
    (-> "1982-10-12T10:20:30" time/from-string time/as-local)
    => (time/from-string-local "1982-10-12T10:20:30"))

  (fact "will interpret times as UTC"
    (-> "1982-10-12T10:20:30" time/from-string-local time/as-utc)
    => (time/from-string "1982-10-12T10:20:30"))

  (fact "will convert to UTC and to local"
    (-> "1982-10-12T10:20:30" time/from-string time/to-local)
    => (time/same-as? (time/from-string "1982-10-12T10:20:30"))))

(facts "about unparsing dates"
  (fact "will unparse in UTC"
    (time/unparse (time/from-string "10:20:30") "HH-mm-ss") => "10-20-30")

  (fact "will unparse in local time"
    (time/unparse-local (time/from-string "10:20:30") "HH-mm-ss") => "13-20-30"
    (provided
     (time/default-time-zone) => (time/time-zone-for-offset 3))))

(fact "will publish clj-time.core functions"
  (-> "1982-10-12T08:20:30"
      time/from-string
      (time/minus (time/days 1)))
  => (time/from-string "1982-10-11T08:20:30"))

(facts "about date coercion"
  (fact "will coerce to SQL timestamp"
    (-> "2010-10-20T10:00:00" time/from-string time/to-sql)
    => #(instance? java.sql.Timestamp %))

  (fact "will coerce from SQL timestamp"
    (-> "2010-10-20T10:00:00" time/from-string time/to-sql time/from-sql)
    => (time/from-string "2010-10-20T10:00:00")

    (-> "2010-10-20T10:00:00" time/from-string time-coerce/to-sql-date time/from-sql)
    => (time/from-string "2010-10-20T10:00:00")

    (let [time (time/from-string "2010-10-20T10:00:00")
          sql-time (time/to-sql time)]
      (time/from-sql {:field 10 :nested {:times [sql-time time] :other-time sql-time}})
      => {:field 10 :nested {:times [time time] :other-time time}})))
