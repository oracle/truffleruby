exclude :test_callcc_at_exit, "| continuation.rb: warning: callcc is obsolete; use Fiber instead"
exclude :test_internal_errinfo_at_exit, "NotImplementedError: fork is not available"
exclude :test_propagate_signaled, "| Exception in thread \"SIGINT handler\" java.lang.IllegalStateException: Language environment is already disposed."
