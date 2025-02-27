exclude :test_OR, "Failed assertion, no message given."
exclude :test_ary_new, "ArgumentError expected but nothing was raised."
exclude :test_assoc, "<[\"pork\", \"porcine\"]> expected but was <nil>."
exclude :test_big_array_literal_with_kwsplat, "<10000> expected but was <10001>."
exclude :test_bsearch_typechecks_return_values, "Expected Exception(TypeError) was raised, but the message doesn't match. Expected /C\\u{309a 26a1 26c4 1f300}/ to match \"wrong argument type (must be numeric, true, false or nil)\"."
exclude :test_combination_clear, "hangs"
exclude :test_combination_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_count, "| | | | "
exclude :test_flatten_recursive, "ArgumentError: tried to flatten recursive array"
exclude :test_flatten_respond_to_missing, "ArgumentError: unknown event: raise"
exclude :test_flatten_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_freeze_inside_sort!, "FrozenError expected but nothing was raised."
exclude :test_initialize, "ArgumentError expected but nothing was raised."
exclude :test_insert, "TypeError expected but nothing was raised."
exclude :test_join, "expected: /non-nil value/"
exclude :test_join_recheck_array_length, "<\"b\"> expected but was <\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaz\">."
exclude :test_join_recheck_elements_type, "<\"ab012z\"> expected but was <\"abcz\">."
exclude :test_permutation_stack_error, "assert_separately failed with error message"
exclude :test_permutation_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_product2, "Exception raised: <#<RangeError: product result is too large>>"
exclude :test_product_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_rassoc, "<[\"pork\", \"porcine\"]> expected but was <nil>."
exclude :test_reject_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_repeated_combination_stack_error, "assert_separately failed with error message"
exclude :test_repeated_combination_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_repeated_permutation_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_replace_wb_variable_width_alloc, "NoMethodError: undefined method `verify_internal_consistency' for GC:Module"
exclude :test_reverse_each2, "<[5, 3, 1]> expected but was <[5, nil, nil, nil, nil, nil]>."
exclude :test_sample_random_generator, "<[]> expected but was <[nil]>."
exclude :test_sample_random_invalid_generator, "NoMethodError expected but nothing was raised."
exclude :test_shuffle, "Exception(ArgumentError) with message matches to /unknown keyword/."
exclude :test_shuffle_random_clobbering, "RuntimeError expected but nothing was raised."
exclude :test_shuffle_random_invalid_generator, "NoMethodError expected but nothing was raised."
exclude :test_slice_out_of_range, "Expected Exception(RangeError) was raised, but the message doesn't match. <\"((-101..-1).%(2)) out of range\"> expected but was <\"((-101..-1).step(2)) out of range\">."
exclude :test_sort_bang_with_freeze, "frozen during comparison."
exclude :test_sort_with_callcc, "RuntimeError: Continuations are unsupported on TruffleRuby"
exclude :test_sort_with_replace, "assert_separately failed with error message"
exclude :test_times, "[ArgumentError] exception expected, not #<RangeError: new array size too large>."
exclude :test_union, "Failed assertion, no message given."
exclude :test_uniq_bang_with_freeze, "frozen during comparison."
