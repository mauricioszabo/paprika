(ns paprika.io.byte-array
  (:refer-clojure :exclude [slurp spit])
  (:require [clojure.java.io :as io])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

(defn slurp
  "Reads a binary file and returns a byte array instead of a string"
  [file-name]
  (let [in (io/input-stream file-name)
        out (ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn spit
  "Saves a binary data (byte array) to a file"
  [file-name contents]
  (io/copy (ByteArrayInputStream. contents)
           (io/file file-name)))
