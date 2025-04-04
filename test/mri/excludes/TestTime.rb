exclude :test_2038, "RangeError: bignum too big to convert into `long'"
exclude :test_asctime, "<#<Encoding:US-ASCII>> expected but was <#<Encoding:UTF-8>>."
exclude :test_at, "<0> expected but was <1>."
exclude :test_future, "RangeError: bignum too big to convert into `long'"
exclude :test_getlocal_utc, "Expected 2000-01-01 00:00:00 +0000 to be utc?."
exclude :test_inspect, "<\"2000-01-01 00:00:00 1/10000000000 UTC\"> expected but was <\"2000-01-01 00:00:00 UTC\">."
exclude :test_marshal_broken_month, "ArgumentError: argument out of range"
exclude :test_marshal_distant_future, "RangeError: float 5.67E9 out of range of integer"
exclude :test_marshal_distant_past, "ArgumentError: year too big to marshal: 1890"
exclude :test_marshal_nsec_191, "<1970-01-01 00:00:00.123456789 +0000> expected but was <1970-01-01 00:00:00.123456 +0000>."
exclude :test_memsize, "NameError: uninitialized constant GC::INTERNAL_CONSTANTS"
exclude :test_new, "Exception(ArgumentError) with message matches to /invalid value for Integer/."
exclude :test_new_from_string, "ArgumentError expected but nothing was raised."
exclude :test_past, "RangeError: bignum too big to convert into `long'"
exclude :test_plus_type, "TypeError expected but nothing was raised."
exclude :test_reinitialize, "[TypeError] exception expected, not #<ArgumentError: wrong number of arguments (given 3, expected 0)>."
exclude :test_strftime, "expected: /strftime called with empty format string/"
exclude :test_strftime_flags, "<\"SAT JAN  1 00:00:00 2000\"> expected but was <\"SAT Jan  1 00:00:00 2000\">."
exclude :test_strftime_rational, "<\"03/14/1592  6:53:58.97932384626433832795028841971\"> expected but was <\"03/14/1592  6:53:58.97932384600000000000000000000\">."
exclude :test_strftime_seconds_from_epoch, "RangeError: UNIX epoch + 10000000000000000000000 seconds out of range for Time (java.time limitation)"
exclude :test_strftime_too_wide, "padding width 8189 too large (java.lang.IndexOutOfBoundsException) from org.truffleruby.core.time.RubyTimeOutputFormatter.padding(RubyTimeOutputFormatter.java:158)"
exclude :test_strftime_wide_precision, "<28> expected but was <49>."
exclude :test_strftime_year, "RangeError: bignum too big to convert into `long'"
exclude :test_utc_or_local, "NoMethodError: undefined method `*' for :foo:Symbol"
