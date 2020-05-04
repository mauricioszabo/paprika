(ns paprika.schemas-test
  (:require [clojure.test :refer [deftest testing is]]
            [check.core :refer [check] :include-macros true]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [paprika.schemas :as schemas :include-macros true]
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
(def time-coerce! (schemas/coercer-for {:time schemas/Time}))
(def dt-coerce! (schemas/coercer-for {:date schemas/Date}))

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
                (:time (time-coerce! {:time "1982-12-10T10:20:30Z"})))))

  (testing "coerces string to datetime"
    (is (time/= (time/date-time 1982 12 10 0 0 0)
                (:date (dt-coerce! {:date "1982-12-10"}))))))

(s/set-fn-validation! true)
(deftest high-order-fns
  (testing "sanity checks"
    (let [f (s/fn :- s/Int [a :- s/Int] (str a))]
      (check (f 10) =throws=> #?(:clj clojure.lang.ExceptionInfo
                                    :cljs ExceptionInfo))
      (check (f "10") =throws=> #?(:clj clojure.lang.ExceptionInfo
                                      :cljs ExceptionInfo))))

  (testing "checks high order fn"
    (let [f (schemas/fn-s [high :- (=> s/Str s/Int) arg]
                        (high arg))]
      (check (f str 10) => "10")
      (check (f str "10") =throws=> #?(:clj clojure.lang.ExceptionInfo
                                          :cljs ExceptionInfo))))

  #_
  (testing "checks output of high order fn"
    (let [f (schemas/fn-s [high :- (=> s/Int s/Int) arg]
                        (high arg))]
      (check (f inc 10) => 11)
      (check (f str 10) =throws=> #?(:clj clojure.lang.ExceptionInfo
                                         :cljs ExceptionInfo)))))

(def example {:name {:doc "The name of this thing" :schema s/Str}
              :age {:doc "The age, in aeons or centuries" :schema s/Int}
              :more {:doc "More info"
                     :schema {:doc "Additional data"
                              :schema {s/Any s/Any}}}})
(deftest schemas-with-docs
  (testing "Will generate a schema definition"
    (check (schemas/schema-for example) =expect=> {:name s/Str
                                                   :age s/Int
                                                   :more {s/Any s/Any}})))
