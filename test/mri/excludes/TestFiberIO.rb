exclude :test_backquote, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_epipe_on_read, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_heavy_read, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_puts_empty, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_read, "NoMethodError: undefined method `set_scheduler' for Fiber:Class"
exclude :test_read_write_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_tcp_accept, "hangs, no Fiber.set_scheduler"
exclude :test_tcp_connect, "hangs, no Fiber.set_scheduler"
exclude :test_close_while_reading_on_thread, "hangs, no Fiber.set_scheduler"
