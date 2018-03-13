(ns paprika.schemas.coerce
  (:require [schema.core :as s]
            [schema.coerce :as coerce]
            [schema.utils :as utils]
            [paprika.time :as time]
            [paprika.collection :as coll]))

; Functions got from here: https://gist.github.com/abp/0c4106eba7b72802347b
; following this thread: https://groups.google.com/forum/#!topic/prismatic-plumbing/SaOBraHzoHE
; and this one: https://stackoverflow.com/questions/31587590/prismatic-schema-removing-unanticipated-keys
(defn filter-schema-keys [m schema-keys extra-keys-walker]
  (reduce-kv (fn [m k v]
               (if (or (contains? schema-keys k)
                       (and extra-keys-walker
                            (not (utils/error? (extra-keys-walker k)))))
                 m
                 (dissoc m k)))
             m
             m))

(defn map-filter-matcher [s]
  (when (or (instance? clojure.lang.PersistentArrayMap s)
            (instance? clojure.lang.PersistentHashMap s))
    (let [extra-keys-schema (s/find-extra-keys-schema s)
          extra-keys-walker (when extra-keys-schema (s/checker extra-keys-schema))
          explicit-keys (some->> (dissoc s extra-keys-schema)
                                 keys
                                 (mapv s/explicit-schema-key)
                                 (into #{}))]
      (when (or extra-keys-walker (seq explicit-keys))
        (fn [x]
          (if (map? x)
            (filter-schema-keys x explicit-keys extra-keys-walker)
            x))))))

(defn- gen-min-digits-spec [min-digits]
  (s/constrained s/Str
                 #(-> % count (>= min-digits) (and (re-matches #"\d+" %)))
                 (symbol (str "string-with-at-least" min-digits "-digits"))))

(defn strict-coercer [coercions]
  (fn [schema]
    (or (coercions schema)
        (coerce/keyword-enum-matcher schema))))

(defn loose-coercer [coercions]
  (let [strict (strict-coercer coercions)]
    (fn [schema]
      (or (map-filter-matcher schema)
          (strict schema)))))
