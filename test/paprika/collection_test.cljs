(ns paprika.collection-test
  (:require [clojure.test :refer [deftest testing is] :include-macros true]
            [paprika.collection :as coll]
            ; [matcher-combinators.parser]))
            ; [matcher-combinators.matchers :as m]
            ; [matcher-combinators.core :as c]
            [matcher-combinators.test]))


(def some-map {:a 10 :b 20})

(defn main []
  (prn *clojurescript-version*)
  ; (prn assert-expr)
  (prn "NOONE"))
(deftest functions-for-maps
  (testing "maps keys and values"
    (is (match? {"a" "10" "b" "20"}
                (coll/map-kv #(vector (name %1) (str %2)) some-map)))))
; (fact "Maps additional functions"
;   (fact "maps keys and values"
;     (coll/map-kv #(vector (name %1) (str %2)) some-map) => {"a" "10" "b" "20"})
;
;   (fact "maps keys"
;     (coll/map-keys name some-map) => {"a" 10 "b" 20})
;
;   (fact "maps values"
;     (coll/map-values str some-map) => {:a "10" :b "20"}))
;
; (fact "update only if not nil"
;   (coll/update-in-when {} [:foo :bar] (constantly nil)) => {}
;   (coll/update-in-when {} [:foo :baz] (constantly 10)) => {:foo {:baz 10}})
;
; (fact "indexes collection"
;   (coll/index-by :id [{:id 1 :name "Foo"} {:id 2 :name "Bar"}])
;   => {1 {:id 1 :name "Foo"}, 2 {:id 2 :name "Bar"}})
