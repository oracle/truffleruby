exclude :test_heavy_read, "needs investigation"
exclude :test_read_write_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_tcp_connect, "hangs, no Fiber.set_scheduler"
exclude :test_tcp_accept, "hangs, no Fiber.set_scheduler"
