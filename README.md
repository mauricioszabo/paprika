[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/mauricioszabo/paprika) 

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

Also, there are comparators for time functions:

```clojure
(def yesterday (-> 1 time/days time/ago))
(def tomorrow (-> 1 time/days time/from-now))
(time/<= yesterday yesterday tomorrow) ; => true
```

### Schema functions

There are some helper functions for Prismatic Schema too.

First, there are some common schemas that we probably want to use on a daily basis:

* `NonEmptyStr` - simply a string that dissalows `""`
* `PositiveInt` - an integer that can't be 0 or negative
* `(digits-string n)` - simply a string composed only by digits, that **needs to have** `n` digits, exactly.
* `Time` - accepts a Joda DateTime object
* `Date` - accepts a Joda DateTime object, but coercer will accept only `yyyy-MM-dd` format

Also, it adds some coercers: first, it'll add a dynamic var `*coercions*` that allows anybody to extend the already existing coercions by hand, if needed. By default, it have coercions for DateTime (in ISO format), Date (in `yyyy-MM-dd` format, will transform it to a Joda DateTime at midnight at UTC timezone), and a coercer for BigDecimal (will transform a string to bigdecimal). There are two principal coercers: strict and non-strict.

```clojure
(require '[paprika.schemas :as schemas])

(def SomeFormat {:name schemas/NonEmptyStr
                 :salary java.math.BigDecimal
                 :birth schemas/Date})

(def strict (schemas/strict-coercer-for SomeFormat))
(strict {:name "Foo" :salary "19000.90" :birth "1912-10-01"})
; => {:name "Foo", :birth "1912-10-01T00:00:00.000Z", :salary 19000.9}

(strict {:name "Foo" :salary "19000.90" :birth "1912-10-01" :unknown "attribute"})
; => Exception

(def non-strict (schemas/coercer-for SomeFormat))
(non-strict {:name "Foo" :salary "19000.90" :birth "1912-10-01" :unknown "attribute"})
; => {:name "Foo", :birth "1912-10-01T00:00:00.000Z", :salary 19000.9}
```

So, strict coercers will disallow unknown keys, non-strict will silently remove then

## License

MIT License
