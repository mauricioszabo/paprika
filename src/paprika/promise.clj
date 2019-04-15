(ns paprika.promise
  (:refer-clojure :exclude [let])
  (:require [clojure.core :as clj]))

(defmacro let [bindings-vec & body]
  `(js/Promise.
    (fn [resolve#]
      (resolve# (clj/let ~bindings-vec ~@body)))))

(defmacro then [promise var & body]
  `(.then ~promise (fn [~var] ~@body)))
