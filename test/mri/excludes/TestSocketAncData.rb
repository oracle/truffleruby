exclude :test_ipv6_pktinfo, "SocketError: Unknown IPV6 control message: PKTINFO" if RUBY_PLATFORM.include?('darwin')
