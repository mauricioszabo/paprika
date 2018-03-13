(ns paprika.string-test
  (:require [midje.sweet :refer :all]
            [paprika.string :as str]))

(fact "Converts CamelCase to clojure-case"
  (str/to-dashes "CamelCase") => "camel-case"
  (str/to-dashes "getCamelCase") => "camel-case"
  (str/to-dashes "camelCase") => "camel-case"
  (str/to-dashes "CamelCASE") => "camel-case"
  (str/to-dashes "getCamelCase" false) => "get-camel-case")
