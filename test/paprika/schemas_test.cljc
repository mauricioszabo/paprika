(ns paprika.schemas-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [paprika.schemas :as schemas]
            [paprika.time :as time]
            [schema.core :as s]))

(deftest new-schemas
  (testing "Positive integers"
    (is (s/validate schemas/PositiveInt 10)))
  (testing "Non empty strings"
    (is (s/validate schemas/NonEmptyStr "foo")))
  (testing "Strings with only digits"
    (is (s/validate (schemas/digits-string 2) "12"))))

(def SchemaEx {:foo (schemas/digits-string 3)
               :bar schemas/NonEmptyStr})
(def coerce! (schemas/coercer-for SchemaEx))
(def strict! (schemas/strict-coercer-for SchemaEx))

; (prn :FOO (dt-coerce! {:time "1982-12-10T10:20:30Z"}))

(def dt-coerce! (schemas/coercer-for {:time schemas/Date}))
(deftest coercers
  (testing "removes additional keys"
    (is (match? (m/equals {:foo "123" :bar "Lol"})
                (coerce! {:foo "123" :bar "Lol" :wow 19}))))

  (testing "fails if there are additional keys"
    (is (thrown-match? #?(:clj clojure.lang.ExceptionInfo :cljs ExceptionInfo)
                       {}
                       (strict! {:foo "123" :bar "Lol" :wow 19}))))

  (testing "coerces string to datetime"
    (is (time/= (time/date-time 1982 12 10 10 20 30)
                (:time (dt-coerce! {:time "1982-12-10T10:20:30Z"}))))))
