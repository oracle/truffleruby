exclude :test_qsort, "dyld: missing symbol called" if RUBY_PLATFORM.include?('darwin')
exclude :test_qsort_slice, "dyld: missing symbol called" if RUBY_PLATFORM.include?('darwin')
