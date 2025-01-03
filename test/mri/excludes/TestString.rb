exclude :test_byterindex, "Expected #<MatchData \"byterindex with string does not affect $~\"> to be nil."
exclude :test_chomp!, "Exception(FrozenError) with message matches to /frozen/."
exclude :test_crypt, "[ArgumentError] exception expected, not #<Errno::EINVAL: Invalid argument>."
exclude :test_dummy_inspect, "<\"\\\"\\\\e\\\\x24\\\\x42\\\\x22\\\\x4C\\\\x22\\\\x68\\\\e\\\\x28\\\\x42\\\"\"> expected but was <\"\\\"\\\\x1B\\\\x24\\\\x42\\\\x22\\\\x4C\\\\x22\\\\x68\\\\x1B\\\\x28\\\\x42\\\"\">."
exclude :test_each, "<\"hello\\n\" + \"\\n\"> expected but was <\"hello\\n\" + \"\\n\" + \"\\n\">."
exclude :test_each_grapheme_cluster, "<1> expected but was <2>."
exclude :test_each_line, "<\"hello\\n\" + \"\\n\"> expected but was <\"hello\\n\" + \"\\n\" + \"\\n\">."
exclude :test_each_line_chomp, "<\"hello\"> expected but was <\"hello\\n\">."
exclude :test_fs, "Exception(TypeError) with message matches to /\\$;/."
exclude :test_fs_setter, "Expected Exception(TypeError) was raised, but the message doesn't match. Expected /\\$分行/ to match \"$/ must be a String\"."
exclude :test_hash, "<0> expected to be != to"
exclude :test_initialize, "FrozenError expected but nothing was raised."
exclude :test_inspect_next_line, "<\"\\\"\\\\u0085\\\"\"> expected but was <\"\\\"\u0085\\\"\">."
exclude :test_match_method, "TypeError: can't define singleton"
exclude :test_respond_to, "<false> expected but was <true>."
exclude :test_rindex, "Expected #<MatchData \"rindex with string does not affect $~\"> to be nil."
exclude :test_rstrip, "Encoding::CompatibilityError expected but nothing was raised."
exclude :test_rstrip_bang, "Encoding::CompatibilityError expected but nothing was raised."
exclude :test_start_with?, "Expected \"Ä\".start_with?(\"\\xC3\") to return false."
exclude :test_string_interpolations_across_size_pools_get_embedded, "NameError: uninitialized constant GC::INTERNAL_CONSTANTS"
exclude :test_to_i, "Encoding::CompatibilityError expected but nothing was raised."
exclude :test_tr, "<\"XYC\"> expected but was <\"YYC\">."
exclude :test_tr!, "<\"XYC\"> expected but was <\"YYC\">."
exclude :test_tr_s, "<\"XYC\"> expected but was <\"YC\">."
exclude :test_tr_s!, "<\"XYC\"> expected but was <\"YC\">."
exclude :test_uminus_frozen, "Expected \"foobarfoobarfoobar\" (oid=96) to be the same as \"foobarfoobarfoobar\" (oid=15960)."
exclude :test_undump, "Expected Exception(RuntimeError) was raised, but the message doesn't match. Expected /invalid/ to match \"dumped string contained Unicode escape but used force_encoding\"."
exclude :test_upcase, "<\"\\u{10574}\"> expected but was <\"\\u{1059B}\">."
exclude :test_upto_nonalnum, "<83> expected but was <1>."
exclude :test_upto_numeric, "<#<Encoding:US-ASCII>> expected but was <#<Encoding:UTF-8>>."
