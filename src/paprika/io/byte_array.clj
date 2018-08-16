(ns paprika.io.byte-array
  (:refer-clojure :exclude [slurp spit])
  (:require [clojure.java.io :as io])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

(defn slurp [file-name]
  (let [in (io/input-stream file-name)
        out (ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn spit [file-name contents]
  (io/copy (ByteArrayInputStream. contents)
           (io/file file-name)))

(defn stream-for [byte-array]
  (if (string? byte-array)
    (stream-for (slurp byte-array))
    (ByteArrayInputStream. byte-array)))
