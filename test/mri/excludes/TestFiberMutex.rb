exclude :test_condition_variable, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_mutex_deadlock, "| #<Thread:0x498 -:4 run> terminated with exception (report_on_exception is true): | -:6:in `block in <main>': undefined method `set_scheduler' for Fiber:Class (NoMethodError)"
exclude :test_mutex_fiber_deadlock_no_scheduler, "hangs"
exclude :test_mutex_fiber_raise, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_mutex_interleaved_locking, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_mutex_synchronize, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_mutex_thread, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_queue, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_queue_pop_waits, "hangs"
