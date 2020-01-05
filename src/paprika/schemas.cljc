(ns paprika.schemas
  (:require [clojure.walk :as walk]
            [schema.core :as s]
            #?(:clj [schema.macros :as m])
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

(declare fn-s)
(defn- norm-body [body args]
  (let [fn-args (filter (fn [[_ schema]] (and (list? schema)
                                              (-> schema first (= 'schema.core/=>))))
                        args)
        fn-args (repeatedly #(gensym "arg-"))
        lets (for [[bind schema] args
                   :when (and (list? schema) (-> schema first (= 'schema.core/=>)))
                   :let [[_ ret & args] schema
                         fn-args (take (count args) fn-args)
                         fn-args-types (mapcat (fn [a s] [a ':- s]) fn-args args)]]
               [bind `(fn-s ~(symbol (str bind "'")) :- ~ret [~@fn-args-types]
                        (~bind ~@fn-args))])]
    (if (empty? lets)
      body
      `((let [~@(mapcat identity lets)] ~@body)))))

(defn- normalize-rest-of-fn [args body]
  (let [norm-schema #(if (= '=> %) `s/=> %)
        norm-args (map (fn [a] [(with-meta a nil)
                                (->> a meta :schema (walk/prewalk norm-schema))])
                      args)]
    (if (s/fn-validation?)
      (cons (->> norm-args (mapcat #(interpose ':- %)) vec)
            (norm-body body norm-args))
      (cons (->> norm-args (mapcat #(interpose ':- %)) vec)
            body))))

(defn- separate-body-schema [possible-body env]
  (if (-> possible-body first (= ':-))
      [(nth possible-body 1)
       (m/process-arrow-schematized-args env (nth possible-body 2))
       (drop 2 possible-body)]
      [`s/Any
       (->> possible-body first (m/process-arrow-schematized-args env))
       (rest possible-body)]))

(defmacro fn-s [name-or-args & body]
  (if (symbol? name-or-args)
    (let [[schema args body] (separate-body-schema body &env)]
      `(s/fn ~name-or-args ~@(normalize-rest-of-fn args body)))
    (let [[schema args body] (separate-body-schema (cons name-or-args body) &env)]
      `(s/fn ~@(normalize-rest-of-fn args body)))))


(defmacro defn-s [name & body]
  (let [[ret body] (if (-> body first (= ':-))
                     [(second body) (drop 2 body)]
                     [`s/Any body])
        [docstring body] (if (string? (first body))
                           [(first body) (rest body)]
                           ["" body])
        [schema args body] (separate-body-schema body &env)]
    ; `(def ~name ~docstring (fn-s ~name ~args ~@body))
    `(s/defn ~name ~':- ~ret ~docstring ~@(normalize-rest-of-fn args body))))

(defn-s lol :- s/Str
  "SWowooo"
  [a :- (=> s/Int s/Str)])
