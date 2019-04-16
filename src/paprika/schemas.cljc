(ns paprika.schemas
  (:require [schema.core :as s]
            [schema.coerce :as coerce]
            [paprika.schemas.coerce :as schema-coercers]
            [paprika.time :as time]))

(defn positive?
  "Any integer z in Z where z > 0."
  [i]
  (and (integer? i) (pos? i)))

(def PositiveInt
  (s/pred positive? 'positive-integer?))

(def NonEmptyStr
  (s/constrained s/Str not-empty))

(defn digits-string [num-digits]
  (s/constrained s/Str
                 #(-> % count (= num-digits) (and (re-matches #"\d+" %)))
                 (symbol (str "string-with-" num-digits "-digits"))))

(defn- gen-time-schema [msg-symbol]
  (s/pred #(instance? time/Time %) msg-symbol))

(def Time (gen-time-schema 'a-joda-DateTime))
(def Date (gen-time-schema 'a-joda-Date))

(defn- parse-date [string] (time/from-string (str string "T00:00:00Z")))

(defn- safe-date [f]
  (fn [obj]
    (if (string? obj)
      ((coerce/safe f) obj)
      obj)))

(def ^:dynamic *coercions*
  (merge coerce/+json-coercions+
         #?(:clj {Time (safe-date time/from-string)
                  Date (safe-date parse-date)
                  java.math.BigDecimal (coerce/safe bigdec)}
            :cljs {Time (safe-date time/from-string)
                   Date (safe-date parse-date)})))

(defn coercer-for
  ([schema] (coercer-for schema *coercions*))
  ([schema coercions]
   (coerce/coercer! schema (schema-coercers/loose-coercer coercions))))

(defn strict-coercer-for
  ([schema] (strict-coercer-for schema *coercions*))
  ([schema coercions]
   (coerce/coercer! schema (schema-coercers/strict-coercer coercions))))
