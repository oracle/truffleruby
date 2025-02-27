# frozen_string_literal: true
# truffleruby_primitives: true

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

class UDPSocket < IPSocket
  def initialize(family = Socket::AF_INET)
    @no_reverse_lookup = Primitive.class(self).do_not_reverse_lookup
    @family            = Truffle::Socket.address_family(family)

    descriptor = Truffle::Socket::Foreign
      .socket(@family, Socket::SOCK_DGRAM, Socket::IPPROTO_UDP)

    Errno.handle('socket(2)') if descriptor < 0

    setup(descriptor, nil, true)
    binmode
  end

  def bind(host, port)
    addr = Truffle::Socket::Foreign.pack_sockaddr_in(
        host, port.to_i, @family, Socket::SOCK_DGRAM, 0)
    status = Truffle::Socket::Foreign.bind(Primitive.io_fd(self), addr)

    Errno.handle('bind(2)') if status < 0

    0
  end

  def connect(host, port)
    sockaddr = Truffle::Socket::Foreign.pack_sockaddr_in(
        host, port.to_i, @family, Socket::SOCK_DGRAM, 0)

    status = Truffle::Socket::Foreign.connect(Primitive.io_fd(self), sockaddr)

    Truffle::Socket::Error.connect_error('connect(2)', self) if status < 0

    0
  end

  def send(message, flags, host = nil, port = nil)
    if host and port
      addr = Socket.sockaddr_in(port.to_i, host)
    elsif host
      addr = host
    else
      addr = nil
    end

    super(message, flags, addr)
  end

  private def __recvfrom_nonblock(maxlen, flags, buffer, exception)
    self.nonblock = true

    flags = 0 if Primitive.nil?(flags)

    internal_recvfrom(maxlen, flags | Socket::MSG_DONTWAIT, buffer, exception)
  end

  def inspect
    "#<#{Primitive.class(self)}:fd #{fileno}>"
  end
end
