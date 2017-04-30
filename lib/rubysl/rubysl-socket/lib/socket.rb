require 'fcntl'

require 'rubysl/socket'
require 'rubysl/socket/version'
require 'rubysl/socket/socket_options'

require 'socket/socket_error'
require 'socket/basic_socket'
require 'socket/constants'

require 'rubysl/socket/foreign/addrinfo'
require 'rubysl/socket/foreign/linger'
require 'rubysl/socket/foreign/ifaddrs'
require 'rubysl/socket/foreign/sockaddr'
require 'rubysl/socket/foreign/sockaddr_in'
require 'rubysl/socket/foreign/sockaddr_in6'

if RubySL::Socket.unix_socket_support?
  require 'rubysl/socket/foreign/sockaddr_un'
end

require 'rubysl/socket/foreign/iovec'
require 'rubysl/socket/foreign/msghdr'
require 'rubysl/socket/foreign/hostent'
require 'rubysl/socket/foreign/servent'

require 'rubysl/socket/ipv6'
require 'rubysl/socket/ancillary_data'
require 'rubysl/socket/foreign'
require 'rubysl/socket/error'
require 'rubysl/socket/bsd' if RubySL::Socket.bsd_support?
require 'rubysl/socket/linux' if RubySL::Socket.linux_support?

require 'socket/socket'
require 'socket/option'
require 'socket/ancillary_data'
require 'socket/mri'
require 'socket/unix_socket'
require 'socket/unix_server'
require 'socket/ip_socket'
require 'socket/udp_socket'
require 'socket/tcp_socket'
require 'socket/tcp_server'
require 'socket/addrinfo'
require 'socket/ifaddr'
