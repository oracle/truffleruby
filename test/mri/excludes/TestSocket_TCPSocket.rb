exclude :test_initialize_connect_timeout, "transient"
exclude :test_initialize_failure, "Expected /for\\ \"192\\.0\\.2\\.1\"\\ port\\ 8000/ to match \"Cannot assign requested address - bind(2)\"."
exclude :test_initialize_resolv_timeout, "NoMethodError: undefined method `close' for nil:NilClass"
exclude :test_inspect, "Expected /AF_INET/ to match \"#<TCPServer:fd 8>\"."
exclude :test_recvfrom, "SocketError: need IPv4 or IPv6 address"
