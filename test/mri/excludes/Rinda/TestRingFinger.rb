exclude :test_make_socket_ipv4_multicast, "NoMethodError: undefined method `ipv4_multicast_loop' for Socket::Option:Class"
exclude :test_make_socket_ipv4_multicast_hops, "NoMethodError: undefined method `ipv4_multicast_loop' for Socket::Option:Class"
exclude :test_make_socket_ipv6_multicast, "Errno::ENODEV: No such device - unable to set socket option"
exclude :test_make_socket_ipv6_multicast_hops, "Errno::ENODEV: No such device - unable to set socket option"
