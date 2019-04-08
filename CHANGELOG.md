# Change Log

## 0.1.2
- ClojureScript support for collections, schemas, and string additions
- Tests for coercer

## 0.1.1
- Simple ZIP file reading support
- Some simple helpers for Java's byte arrays
- Added `time/>`, `time/>=`, and friends to compare date/times
- Added metadata (doc and arglists) for every fn that we import from clj-time namespace
- Pretty-printing joda-times (so you can copy-paste a string representation of JodaTime and use it in your source file or tests)
- Bugfix: when coercing dates, don't throw exceptions with daylight saving time transitions

## 0.1.0
  - Added everything
