# Paprika

Utility functions for Clojure Delight! Make your code spicy!

## Usage

### Collection Functions:

```clojure
(import '[paprika.collection :as coll])

(def example-map {:a 10 :b 20})

(coll/map-values str example-map) => {:a "10" :b "20"}
(coll/map-keys name example-map) => {"a" 10 "b" 20}
(coll/map-kv (fn [k v] [(name k) (inc v)]) example-map)
=> {"a" 11 "b" 21}
```

### Time functions

All time functions will parse in UTC or local time, **only**. Functions that parses to
local time are suffixed with `-local`. So, for instance, `(time/from-string "10:20")` will
assume that we're asking for 10:20 in UTC format. `(time/from-string-local "10:20")` will
assume we're asking for 10:20 in local time, or 13:20 in UTC (if we're not on DST).

To aid us on tests we're creating some helper reader macros:

```clojure
(require '[paprika.time :as time])
(def utc-time #time/utc "2011-10-20T10:00:00")
(def local-time #time/local "2011-10-20T10:00:00")

(def sql-timestamp #sql/utc "2011-10-20T10:00:00")
(def local-sql-timestamp #sql/local "2011-10-20T10:00:00")

(fact "UTC is equal to TIMESTAMP"
  utc-time => (time/same-as? sql-timestamp))
(fact "Local is equal to TIMESTAMP"
  local-time => (time/same-as? local-sql-timestamp))
```

## License

MIT License
