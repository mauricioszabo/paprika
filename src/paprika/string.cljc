(ns paprika.string
  (:require [clojure.string :as str]))

(defn to-dashes
  "Converts a CamelCase or camelCase string to dashes
Accepts an optional second parameter that will keep 'get-' in front of string"
  ([string] (to-dashes string true))
  ([string remove-get]
   (let [strip-get #(if remove-get (str/replace-first % #"^get" "") %)]
     (-> string
         (str/replace #"([A-Z]+)" "-$1")
         strip-get
         (str/replace-first #"^-" "")
         str/lower-case))))
