exclude :test_fallback_to_sh, "<true> expected but was <nil>."
exclude :test_system, "<\"678\"> expected but was <\"\">."
exclude :test_system_exception, "Expected Exception(RuntimeError) was raised, but the message doesn't match. Expected /\\ACommand failed with exit / to match \"command failed\"."
exclude :test_system_exception_nonascii, "Exception(RuntimeError) with message matches to /テスト\\.cmd/."
