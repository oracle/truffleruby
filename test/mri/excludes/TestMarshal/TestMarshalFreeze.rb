exclude :test_return_objects_are_frozen, "Expected [\"foo\", {}, /foo/, 1..2] to be frozen?."
exclude :test_object_prepend, "[ruby-core:53202] [Bug #8041](/Users/andrykonchin/projects/truffleruby-ws/truffleruby/test/mri/tests/ruby/marshaltestlib.rb:45:in `marshal_equal_with_ancestry')."
exclude :test_proc_returned_object_are_not_frozen, "Expected /foo/ to not be frozen?."
exclude :test_singleton, "TypeError expected but nothing was raised."
exclude :test_time_subclass, "ArgumentError: year too big to marshal: 10"
exclude :test_regexp, "RegexpError: incompatible character encoding"
exclude :test_range_cyclic, "ArgumentError: dump format error (unlinked)"
exclude :test_regexp_subclass, "TypeError: no implicit conversion of Integer into String"
exclude :test_range_subclass, "ArgumentError: wrong number of arguments (given 4, expected 2..3)"