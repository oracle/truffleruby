exclude :test_chown, "transient (GR-41382)" if RUBY_PLATFORM.include?('darwin')
exclude :test_chown_R, "transient (GR-41382)" if RUBY_PLATFORM.include?('darwin')
exclude :test_chown_dir_group_ownership_not_recursive, "transient (GR-41382)" if RUBY_PLATFORM.include?('darwin')
exclude :test_chown_noop, "transient (GR-41382)" if RUBY_PLATFORM.include?('darwin')
