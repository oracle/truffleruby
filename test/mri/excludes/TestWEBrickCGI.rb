exclude :test_cgi, "too slow (>170s)"
exclude :test_cgi_env, "cgi_runner.rb clears all of ENV so the locale is not properly set anymore"
