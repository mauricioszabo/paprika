(ns paprika.collection-test
  (:require [clojure.test :refer [deftest testing is] :include-macros true]
            [paprika.collection :as coll]
            [matcher-combinators.test]))


(def some-map {:a 10 :b 20})

(deftest functions-for-maps
  (testing "maps keys and values"
    (is (match? {"a" "10" "b" "20"}
                (coll/map-kv #(vector (name %1) (str %2)) some-map))))
  (testing "maps keys"
    (is (match? {"a" 10 "b" 20}
                (coll/map-keys name some-map))))

  (testing "maps values"
    (is (match? {:a "10" :b "20"}
                (coll/map-values str some-map)))))

(deftest update-if-not-nil
  (is (match? {}
              (coll/update-in-when {} [:foo :bar] (constantly nil))))
  (is (match? {:foo {:baz 10}}
              (coll/update-in-when {} [:foo :baz] (constantly 10)))))

(deftest indexing-collections
  (is (match? {1 {:id 1 :name "Foo"}, 2 {:id 2 :name "Bar"}}
              (coll/index-by :id [{:id 1 :name "Foo"} {:id 2 :name "Bar"}]))))
