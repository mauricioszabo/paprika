(ns paprika.promise
  (:refer-clojure :exclude [let])
  (:require [clojure.core :as clj]))

(defmacro then [promise var & body]
  `(.then ~promise (fn [~var] ~@body)))

(defn- arrow? [sym]
  (and (symbol? sym)
       (re-matches #"\<\-+" (str sym))))

(defn- remap-bindings [bindings-vec]
  (loop [[f s t & rest] bindings-vec
         bindings []
         result {}]
    (cond
      (and (empty? rest) (arrow? s)) [(conj bindings f t) (assoc result f 1)]
      (arrow? s) (recur rest (conj bindings f t) (assoc result f 1))
      (empty? rest) [(conj bindings f s) result]
      :else (recur (cons t rest) (conj bindings f s) result))))

(defn- wrap-promise [body var]
  `((.then ^js ~var (fn [~var] ~@body))))

(defmacro let [bindings-vec & body]
  (clj/let [[bindings remaps] (remap-bindings bindings-vec)
            new-body (->> remaps keys reverse
                          (reduce wrap-promise
                                  body))]
    `(js/Promise.
       (fn [resolve#]
         (resolve# (clj/let ~bindings ~@new-body))))))
