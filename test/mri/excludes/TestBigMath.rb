exclude :test_log, "Timeout::Error: execution of assert_separately expired (took longer than 30 seconds)" if RUBY_PLATFORM.include?('darwin')
