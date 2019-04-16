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

(defn- remap-bindings2 [bindings-vec]
  (loop [[f s t & rest] bindings-vec
         bindings []]
    (cond
      (and (empty? rest) (arrow? s)) (conj bindings [f 1 t])
      (arrow? s) (recur rest (conj bindings [f 1 t]))
      (empty? rest) (conj bindings [f nil s])
      :else (recur (cons t rest) (conj bindings [f nil s])))))

(defn- wrap-promise [body var]
  `((.then ^js ~var (fn [~var] ~@body))))

(defn- wrap-bindings [bindings-map body]
  (loop [bindings (reverse bindings-map)
         seed body]
    (clj/let [[non-p ps] (split-with (complement second) bindings)]
      (cond
        (empty? bindings) seed
        (empty? non-p) (clj/let [[[var _ prom] & rest] ps]
                          (recur rest `((.then ~prom (fn [~var] ~@seed)))))
        :else (recur ps `((clj/let ~(->> non-p
                                         reverse
                                         (mapcat #(vector (first %) (last %)))
                                         vec)
                                   ~@seed)))))))

(defmacro let [bindings-vec & body]
  (clj/let [new-body (-> bindings-vec remap-bindings2 (wrap-bindings body))]
    (if (-> new-body count (= 1))
      (first new-body)
      `(do ~@new-body))))
  ; (clj/let [[bindings remaps] (remap-bindings bindings-vec)
  ;           new-body (->> remaps keys reverse
  ;                         (reduce wrap-promise
  ;                                 body))]
  ;   `(js/Promise.
  ;      (fn [resolve#]
  ;        (resolve# (clj/let ~bindings ~@new-body))))))

#_
(prn (macroexpand-1
      '(paprika.promise/let [v 10
                             p <- (. js/Promise resolve 1)
                             p2 <- (+ p 10)]
         10)))

#_
(def bindings
  (remap-bindings2 '[v 10
                     a 10
                     p <- (. js/Promise resolve 1)
                     p2 <- (+ p 10)
                     c 30]))

  ; (loop [bindings]
  ;     (if (empty? non-p)))))
