(ns paprika.collection)

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
