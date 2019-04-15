(ns paprika.promise-test
  (:require [clojure.test :refer [deftest testing is]]
            [paprika.promise :as p :include-macros true]
            [matcher-combinators.test]
            [paprika.cljs-test.async :as a :include-macros true]))

(deftest combining-promises
  (a/testing "awaits all promises" done
    (let [p1 (. js/Promise resolve 1)
          p2 (. js/Promise resolve 2)
          p3 (p/all [p1 p2])]
      (p/then p3 res
        (is (match? [1 2] res))
        (done)))))

(deftest mapping-promises
  (a/testing "use of map to compose promises" done
    (let [p1 (. js/Promise resolve 1)
          p2 (. js/Promise resolve 2)
          p3 (p/map inc p2)]
      (p/map (fn [r1 r2]
               (is (= 1 r1))
               (is (= 3 r2))
               (done))
             p1 p3))))

(deftest wait-and-go
  (a/testing "awaits the promise results, but propagates old" done
    (let [p1 (. js/Promise resolve 1)
          p2 (. js/Promise resolve 2)
          p3 (p/intercept inc p2)]
      (p/intercept (fn [r1 r2]
                     (is (= 1 r1))
                     (is (= 2 r2))
                     (done))
                   p1 p3))))

(deftest async-lets
  (a/testing "wraps let in a promise" done
    (let [prom (p/let [foo 10 bar 20] (+ foo bar))]
      (. prom then (fn [res]
                     (is (= res 30))
                     (done))))))
