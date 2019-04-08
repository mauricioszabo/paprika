(ns paprika.schemas-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [paprika.schemas :as schemas]
            [schema.core :as s]))

(deftest new-schemas
  (testing "Positive integers"
    (is (s/validate schemas/PositiveInt 10)))

  (testing "Non empty strings"
    (is (s/validate schemas/NonEmptyStr "foo")))


  (testing "Strings with only digits"
    (is (s/validate (schemas/digits-string 2) "12"))))
