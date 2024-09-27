exclude :test_trace, "<40414> expected but was <0>."
exclude :test_trace_break, "NoMethodError: undefined method `add_trace_func' for #<Thread:0x3d8>"
exclude :test_trace_proc_that_raises_exception, "RuntimeError expected but nothing was raised."
exclude :test_trace_string, "<:bar> expected but was <0>."
