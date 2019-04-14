(ns paprika.time-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            #?(:clj [clj-time.coerce :as time-coerce]
               :cljs [cljs-time.coerce :as time-coerce])
            [paprika.time :as time]))

(def yesterday (-> 1 time/days time/ago))
(def tomorrow (-> 1 time/days time/from-now))
(deftest comparing-dates
  (testing "will compare if it's less than another date"
    (is (time/< yesterday (time/now) tomorrow))
    (is (time/<= yesterday yesterday tomorrow)))

  (testing "will compare if it's greater than another date"
    (is (time/> tomorrow (time/now) yesterday))
    (is (time/>= tomorrow yesterday yesterday))))

(def tz-offset
  #?(:clj
     (time/millis (.getStandardOffset (time/default-time-zone) 0))
     :cljs
     (let [tz (time/default-time-zone)
           offset (-> tz :offset second
                      (cond-> (-> tz :offset first (= :-)) -))]
       (time/hours offset))))

(deftest parsing-utc-times
  (testing "will convert times"
    (is (time/= (time/from-string "1982-10-12T10:20:30")
                (time/parse "12/10/1982 10:20:30" "dd/MM/yyyy HH:mm:ss")))))

  ; FIXME: this only works for Midje now...
  ; (testing "will convert to UTC and to local"
  ;   (is (match? (time/same-as? (time/from-string "1982-10-12T10:20:30"))
  ;               (-> "1982-10-12T10:20:30" time/from-string time/to-local)))))

(deftest local-date
  (testing "converts string to local time"
    (is (time/= (-> "1982-10-12T10:20:30"
                    time/from-string
                    (time/minus tz-offset))
                (time/from-string-local "1982-10-12T10:20:30"))))

  (testing "parses to local time"
    (is (time/= (-> "1982-10-12T10:20:30"
                    time/from-string
                    (time/minus tz-offset))
                (time/parse-local "12/10/1982 10:20:30" "dd/MM/yyyy HH:mm:ss"))))

  (testing "parity between from-string-local and parse-local"
    (is (time/= (time/from-string-local "1982-10-12T10:20:30")
                (time/parse-local "12/10/1982 10:20:30" "dd/MM/yyyy HH:mm:ss"))))

  (testing "conversion from UTC to local"
    (is (time/= (time/from-string-local "1982-10-12T10:20:30")
                (-> "1982-10-12T10:20:30" time/from-string time/as-local))))

  (testing "changing timezone of local date to UTC"
    (is (time/= (time/from-string "1982-10-12T10:20:30")
                (-> "1982-10-12T10:20:30" time/from-string-local time/as-utc)))))

(deftest unparsing-dates
  (testing "will unparse in UTC"
    (is (match? "10-20-30"
                (time/unparse (time/from-string "10:20:30") "HH-mm-ss"))))

  #_
  (testing "will unparse in local time"
    (with-redefs [time/default-time-zone (constantly (time/time-zone-for-offset 3))]
      (is (match? "13-20-30"
                  (time/unparse-local (time/from-string "10:20:30") "HH-mm-ss"))))))

(deftest publishing-clj-time-functions
  (is (time/= (time/from-string "1982-10-11T08:20:30")
              (-> "1982-10-12T08:20:30"
                  time/from-string
                  (time/minus (time/days 1))))))

#?(:clj
   (deftest sql-date-coercion
     (testing "will coerce to SQL timestamp"
       (is (instance? java.sql.Timestamp
                      (-> "2010-10-20T10:00:00" time/from-string time/to-sql))))

     (testing "will coerce from SQL timestamp"
       (is (= (time/from-string "2010-10-20T10:00:00")
              (-> "2010-10-20T10:00:00" time/from-string time/to-sql time/from-sql)))

       (is (= (time/from-string "2010-10-20T10:00:00")
              (-> "2010-10-20T10:00:00" time/from-string time-coerce/to-sql-date time/from-sql)))

       (let [time (time/from-string "2010-10-20T10:00:00")
             sql-time (time/to-sql time)]
         (is (= {:field 10 :nested {:times [time time] :other-time time}}
                (time/from-sql {:field 10 :nested {:times [sql-time time] :other-time sql-time}})))))))

(deftest pretty-print-dates
  (testing "will parse UTC dates"
    (is (match? "#time/utc \"2010-10-20T10:00:00.000Z\""
                (pr-str (time/from-string "2010-10-20T10:00:00Z")))))

  #?(:clj
     (testing "will parse TZ dates"
       (is (match? #"#time/local \"2010-10-2.*"
                   (-> "2010-10-21T10:00:00+03:00" time/from-string
                       (time/to-time-zone (time/time-zone-for-id "America/Sao_Paulo"))
                       pr-str))))))
