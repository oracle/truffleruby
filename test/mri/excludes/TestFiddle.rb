exclude :test_dlopen_linker_script_group_linux, "NotImplementedError: NotImplementedError" if RUBY_PLATFORM.include?('linux')
exclude :test_nil_true_etc, "NameError: uninitialized constant Fiddle::Qtrue"
