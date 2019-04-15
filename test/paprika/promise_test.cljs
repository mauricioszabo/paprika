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

(deftest async-await
  (a/testing "let can await for promises" done
    (p/let [v 10
            p <- (. js/Promise resolve 1)]
      (is (= 10 v))
      (is (= 1 p))
      (done))))

(prn (macroexpand-1
      '(p/let [p1 <- (js/Promise. (fn [res] (swap! a conj 1) (res 1)))
               p2 <- (js/Promise. (fn [res] (swap! a conj 2) (res 2)))
               p3 <-- (js/Promise. (fn [res] (swap! a conj 3) (res 3)))
               p4 <-- (js/Promise. (fn [res] (swap! a conj 4) (res 4)))]
        (is (= p1 1))
        (is (= p2 2))
        (is (= p3 3))
        (is (= p4 4))
        (is (= [1 3 2 4] @a)))))

(deftest multi-async-await
  (a/testing "lets user control what runs in paralel when awaiting" done
    (let [a (atom [])]
      (p/let [p1 <- (js/Promise. (fn [res] (swap! a conj 1) (res 1)))
              p2 <- (js/Promise. (fn [res] (swap! a conj 2) (res 2)))
              p3 <-- (js/Promise. (fn [res] (swap! a conj 3) (res 3)))
              p4 <-- (js/Promise. (fn [res] (swap! a conj 4) (res 4)))]
        (is (= p1 1))
        (is (= p2 2))
        (is (= p3 3))
        (is (= p4 4))
        (is (= [1 3 2 4] @a))
        (done)))))
