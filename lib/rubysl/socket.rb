# Copyright (c) 2013, Brian Shirai
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice, this
#    list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. Neither the name of the library nor the names of its contributors may be
#    used to endorse or promote products derived from this software without
#    specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
# OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

require 'fcntl'

require 'socket/rubysl'
require 'socket/rubysl/version'
require 'socket/rubysl/socket_options'

require 'socket/socket_error'
require 'socket/basic_socket'
require 'socket/constants'

require 'socket/rubysl/foreign/addrinfo'
require 'socket/rubysl/foreign/linger'
require 'socket/rubysl/foreign/ifaddrs'
require 'socket/rubysl/foreign/sockaddr'
require 'socket/rubysl/foreign/sockaddr_in'
require 'socket/rubysl/foreign/sockaddr_in6'

if RubySL::Socket.unix_socket_support?
  require 'socket/rubysl/foreign/sockaddr_un'
end

require 'socket/rubysl/foreign/iovec'
require 'socket/rubysl/foreign/msghdr'
require 'socket/rubysl/foreign/hostent'
require 'socket/rubysl/foreign/servent'

require 'socket/rubysl/ipv6'
require 'socket/rubysl/ancillary_data'
require 'socket/rubysl/foreign'
require 'socket/rubysl/error'
require 'socket/rubysl/bsd' if RubySL::Socket.bsd_support?
require 'socket/rubysl/linux' if RubySL::Socket.linux_support?

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
