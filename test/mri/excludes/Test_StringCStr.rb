exclude :test_embed, "<0> expected but was <120>."
exclude :test_embedded_from_heap, "Encoding::CompatibilityError: incompatible character encodings: UTF-16BE and UTF-8"
exclude :test_frozen, "<0> expected but was <120>."
exclude :test_long, "<0> expected but was <120>."
exclude :test_rb_str_new_frozen_embed, "NoMethodError: undefined method `cstr_noembed' for Bug::String:Class"
exclude :test_shared, "JVM crash; SIGSEGV: C  [string.so+0x30ec]  bug_str_unterminated_substring+0x5c"
exclude :test_wchar_embed, "<0> expected but was <30720>."
exclude :test_wchar_long, "UTF-16BE."
exclude :test_wchar_sub!, 'the given string is not compatible to the expected encoding "UTF_16BE", did you forget to convert it? (java.lang.IllegalArgumentException)'
