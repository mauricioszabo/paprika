(ns paprika.collection-test
  (:require [midje.sweet :refer :all]
            [paprika.collection :as coll]))

(def some-map {:a 10 :b 20})
(fact "Maps additional functions"
  (fact "maps keys and values"
    (coll/map-kv #(vector (name %1) (str %2)) some-map) => {"a" "10" "b" "20"})

  (fact "maps keys"
    (coll/map-keys name some-map) => {"a" 10 "b" 20})

  (fact "maps values"
    (coll/map-values str some-map) => {:a "10" :b "20"}))

(fact "update only if not nil"
  (coll/update-in-when {} [:foo :bar] (constantly nil)) => {}
  (coll/update-in-when {} [:foo :baz] (constantly 10)) => {:foo {:baz 10}})
