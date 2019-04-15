(ns paprika.string-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [paprika.string :as str]))

(deftest conversions
  (testing "converts CamelCase to clojure-case"
    (is (match? "camel-case"
                (str/to-dashes "CamelCase")))
    (is (match? "camel-case"
                (str/to-dashes "getCamelCase")))
    (is (match? "camel-case"
                (str/to-dashes "camelCase")))
    (is (match? "camel-case"
                (str/to-dashes "CamelCASE")))
    (is (match? "get-camel-case"
                (str/to-dashes "getCamelCase" false)))))
