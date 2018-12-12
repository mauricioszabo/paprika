(ns paprika.collection
  (:require [clojure.core.reducers :as reducers]))

(defn map-kv [f clj-map]
  (->> clj-map
       (map (fn [[k v]] (f k v)))
       (into {})))

(defn map-keys [f clj-map]
  (map-kv #(vector (f %1) %2) clj-map))

(defn map-values [f clj-map]
  (map-kv #(vector %1 (f %2)) clj-map))

(defn update-in-when [map path fun & args]
  (let [curr-val (get-in map path)
        new-val (apply fun curr-val args)]
    (if (nil? new-val)
      map
      (assoc-in map path new-val))))

(defn index-by 
  "Exactly the same as clojure.core/group-by, except it returns a single
   element. If there are more than one element that matches the function,
   the ending indexed element is undefined"
  [f collection]
  (reducers/reduce (fn [map m] (assoc map (f m) m)) {} collection))
