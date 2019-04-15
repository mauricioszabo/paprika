(ns paprika.cljs-test.async)

(defmacro testing [message done & body]
  `(cljs.test/async ~done
     (js/setTimeout (fn []
                      (throw (ex-info "Not finished" {:test ~message}))
                      (~done))
                    3000)
     (cljs.test/testing ~message
       ~@body)))
