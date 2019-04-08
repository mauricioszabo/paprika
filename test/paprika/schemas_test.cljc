(ns paprika.schemas-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [paprika.schemas :as schemas]
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
(deftest coercers
  (testing "removes additional keys"
    (is (match? (m/equals {:foo "123" :bar "Lol"})
                (coerce! {:foo "123" :bar "Lol" :wow 19}))))

  (testing "fails if there are additional keys"
    (is (thrown-match? #?(:clj clojure.lang.ExceptionInfo :cljs ExceptionInfo)
                       {}
                       (strict! {:foo "123" :bar "Lol" :wow 19})))))
