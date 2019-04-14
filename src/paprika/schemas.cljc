(ns paprika.schemas
  (:require [schema.core :as s]
            [schema.coerce :as coerce]
            [paprika.schemas.coerce :as schema-coercers]
            #?(:clj [paprika.time :as time])))

(defn- positive?
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

#?(:clj
   (defn- gen-time-schema [msg-symbol]
     (s/pred #(instance? org.joda.time.DateTime %) msg-symbol)))

#?(:clj
   (def Time (gen-time-schema 'a-joda-DateTime)))
#?(:clj
   (def Date (gen-time-schema 'a-joda-Date)))

#?(:clj
   (defn- parse-date [string] (time/from-string (str string "T00:00:00Z"))))

(defn- safe-date [f]
  (fn [obj]
    (if (string? obj)
      ((coerce/safe f) obj)
      obj)))

((safe-date time/from-string) "1982-12-10T10:20:30Z")

(def ^:dynamic *coercions*
  (merge coerce/+json-coercions+
         #?(:clj {Time (safe-date time/from-string)
                  Date (safe-date parse-date)
                  java.math.BigDecimal (coerce/safe bigdec)})))

(defn coercer-for
  "Defines a coercer for any schema that'll remove additional keys.

(def coerce (coercer/for <schema> [<possible-coercions>]))
(coerce {:some 1 :bar 20 :additional 1}) => {:some 1 :bar 20}"
  ([schema] (coercer-for schema *coercions*))
  ([schema coercions]
   (coerce/coercer! schema (schema-coercers/loose-coercer coercions))))

(defn strict-coercer-for
  "Defines a coercer for any schema that won't remove additional keys.

(def coerce (coercer/for <schema> [<possible-coercions>]))
(coerce {:some 1 :bar 20 :additional 1}) => CRASH
(coerce {:some 1 :bar 20}) => {:some 1 :bar 20}"
  ([schema] (strict-coercer-for schema *coercions*))
  ([schema coercions]
   (coerce/coercer! schema (schema-coercers/strict-coercer coercions))))
