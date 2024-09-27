exclude :test_02_unknown, "<\"DRbTests::DRbEx::\"> expected but was <\"DRbTests::DRbEx::FooBar\">."
exclude :test_06_timeout, "[Timeout::Error] exception expected, not #<DRb::DRbConnError: execution expired>."
exclude :test_11_remote_no_method_error, "[DRb::DRbRemoteError] exception expected, not #<NoMethodError: undefined method `invoke_no_method' for #<DRbTests::DRbEx:0x4d8>>."
