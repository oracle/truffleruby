exclude :test_addrinfo_getaddrinfo_any_non_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_full_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_ipv4_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_ipv6_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_localhost, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_no_host_non_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_non_existing_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_numeric_non_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_getaddrinfo_pf_unspec_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_ip_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_tcp_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_addrinfo_udp_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_ip_socket_getaddress_domain_blocking, "hangs, no Fiber.set_scheduler"
exclude :test_socket_getnameinfo_domain_blocking, "hangs, no Fiber.set_scheduler"
