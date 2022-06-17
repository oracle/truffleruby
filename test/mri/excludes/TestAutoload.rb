exclude :test_source_location, "needs investigation"
exclude :test_threaded_accessing_inner_constant, "transient" if RUBY_PLATFORM.start_with?('aarch64-darwin')
