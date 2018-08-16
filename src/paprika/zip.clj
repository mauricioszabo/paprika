(ns paprika.zip
  (:require [clojure.java.io :as io]
            [paprika.io.byte-array :as barray])
  (:import [java.util.zip ZipFile ZipInputStream]
           [java.io ByteArrayOutputStream]))

(defn read-file [filename]
  (let [file (ZipFile. filename)
        it (.iterator (.stream file))
        next (fn next [] (when (.hasNext it)
                           (lazy-seq (cons (.next it) (next)))))]
    (for [entry (next)
          :let [os (ByteArrayOutputStream.)
                bytearray (delay
                           (io/copy (.getInputStream file entry) os)
                           (.toByteArray os))]]
      {:entry entry
       :name (.getName entry)
       :directory? (.isDirectory entry)
       :size (.getSize entry)
       :binary bytearray
       :contents (delay (String. @bytearray))})))

(defn- read-entry [stream]
  (let [ba (byte-array 1024)
        out (ByteArrayOutputStream.)]
    (loop [read (.read stream ba 0 1024)]
      (if (= -1 read)
        (.toByteArray out)
        (do
          (.write out ba 0 read)
          (recur (.read stream ba 0 1024)))))))

(defn from-stream [stream]
  (let [zip (ZipInputStream. stream)
        next-entry (fn aux []
                     (lazy-seq
                      (when-let [entry (.getNextEntry zip)]
                        (let [bin (read-entry zip)]
                          (cons {:entry entry
                                 :name (.getName entry)
                                 :directory? (.isDirectory entry)
                                 :size (.getSize entry)
                                 :binary bin
                                 :contents (delay (String. bin))}
                                (aux))))))]
    (next-entry)))

(defn from-stream [stream]
  (let [zip (ZipInputStream. stream)
        next (fn next [] (some-> zip .getNextEntry (cons (next)) lazy-seq))
        ba (fn [size] (let [ba (byte-array size)]
                        (.closeEntry zip)
                        (prn [:SIZE size])
                        (prn [:SIZE-READ (.read zip ba 0 size)])
                        ba))]
    (for [entry (next)
          :let [size (.getSize entry)
                bytearray (ba size)]]
      {:entry entry
       :name (.getName entry)
       :directory? (.isDirectory entry)
       :binary bytearray
       :contents (delay (String. bytearray))
       :size size})))
