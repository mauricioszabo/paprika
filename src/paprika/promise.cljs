(ns paprika.promise
  (:refer-clojure :exclude [map])
  (:require [clojure.core :as clj]))

(defn all
  "Awaits for all promises to resolve, then returns a promise of vectors with results"
  [^js promises]
  (.. js/Promise
      (all promises)
      (then vec)))

(defn map
  "Same as clojure.core/map, but for promises. Return a promise with the result"
  [fun p1 & promises]
  (.then (all (cons p1 promises))
         #(apply fun %)))

(defn intercept
  "Same as clojure.core/map, but keeps the old result of promises. Used primarily
for side-effects functions that depends on promises' results"
  [fun p1 & promises]
  (if (empty? promises)
    (.then ^js p1 (fn [res] (fun res) res))
    (.then (all (cons p1 promises))
           (fn [results]
             (apply fun results)
             results))))
