exclude :test_ascii_incomat_inspect, "Encoding::CompatibilityError: incompatible encoding regexp match (US-ASCII regexp with UTF-16LE string)"
exclude :test_eq_can_be_redefined, "| Error while formatting Ruby exception: | <internal:core> core/posix.rb:150:in `as_pointer': Message not supported. (Polyglot::UnsupportedMessageError)"
exclude :test_match_method, "TypeError: can't define singleton"
exclude :test_to_proc_arg, "Expected #<Proc:0x2c8(&:itself) (lambda)> (oid=712) to be the same as #<Proc:0x2d8(&:itself) (lambda)> (oid=728)."
exclude :test_to_proc_binding, "ArgumentError expected but nothing was raised."
