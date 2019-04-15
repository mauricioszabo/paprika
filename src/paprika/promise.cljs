(ns paprika.promise)

(defn all
  "Awaits for all promises to resolve, then returns a promise of vectors with results"
  [^js promises]
  (.. js/Promise
      (all promises)
      (then vec)))
