(ns paprika.zip
  (:require [clojure.java.io :as io])
  (:import [java.util.zip ZipFile]
           [java.io ByteArrayOutputStream]))


(defn read-file [filename]
  (let [file (ZipFile. filename)
        it (.iterator (.stream file))
        next (fn next [] (when (.hasNext it)
                           (lazy-seq (cons (.next it) (next)))))]
    (map (fn [entry]
           (let [os (ByteArrayOutputStream.)
                 bytearray (delay
                            (io/copy (.getInputStream file entry) os)
                            (.toByteArray os))]
             {:entry entry
              :name (.getName entry)
              :directory? (.isDirectory entry)
              :size (.getSize entry)
              :binary bytearray
              :contents (delay (String. @bytearray))}))
         (next))))
