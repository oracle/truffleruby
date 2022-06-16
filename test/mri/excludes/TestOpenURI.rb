exclude :test_encoding, "needs investigation"
exclude :test_open_timeout, "transient" if RUBY_PLATFORM.start_with?('aarch64-darwin')
