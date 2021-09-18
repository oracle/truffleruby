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

class TCPSocket < IPSocket
  def self.gethostbyname(hostname)
    addrinfos = Socket
      .getaddrinfo(hostname, nil, nil, :STREAM, nil, Socket::AI_CANONNAME)

    hostname     = addrinfos[0][2]
    family       = addrinfos[0][4]
    addresses    = addrinfos.map { |a| a[3] }
    alternatives = []

    Truffle::Socket.aliases_for_hostname(hostname).each do |name|
      alternatives << name unless name == hostname
    end

    [hostname, alternatives, family, *addresses]
  end

  def initialize(host, service, local_host = nil, local_service = nil)
    @no_reverse_lookup = self.class.do_not_reverse_lookup

    if host
      host = Truffle::Socket.coerce_to_string(host)
    end

    if service.is_a?(Integer)
      service = service.to_s
    else
      service = Truffle::Socket.coerce_to_string(service)
    end

    local_addrinfo = nil

    # When a local address and/or service/port are given we should bind the
    # socket to said address (besides also connecting to the remote address).
    if local_host or local_service
      if local_host
        local_host = Truffle::Socket.coerce_to_string(local_host)
      end

      if local_service.is_a?(Integer)
        local_service = local_service.to_s
      elsif local_service
        local_service = Truffle::Socket.coerce_to_string(local_service)
      end

      local_addrinfo = Socket
        .getaddrinfo(local_host, local_service, :UNSPEC, :STREAM)
    end

    descriptor     = nil
    connect_status = 0

    # Because we don't know exactly what address family to bind to we'll just
    # grab all the available ones and try every one of them, bailing out on the
    # first address that we can connect to.
    #
    # This code is loosely based on the behaviour of CRuby's
    # "init_inetsock_internal()" function as of Ruby 2.2.
    Socket.getaddrinfo(host, service, :UNSPEC, :STREAM).each do |addrinfo|
      _, port, address, _, family, socktype, protocol = addrinfo

      descriptor = Truffle::Socket::Foreign.socket(family, socktype, protocol)

      next if descriptor < 0

      # If any local address details were given we should bind to one that
      # matches the remote address connected to above.
      if local_addrinfo
        local_info = local_addrinfo.find do |addr|
          addr[4] == family && addr[5] == socktype
        end

        if local_info
          status = Truffle::Socket::Foreign
            .bind(descriptor, Socket.sockaddr_in(local_info[1], local_info[2]))

          Errno.handle('bind(2)') if status < 0
        end
      end

      connect_status = Truffle::Socket::Foreign
        .connect(descriptor, Socket.sockaddr_in(port, address))

      break if connect_status >= 0
    end

    if connect_status < 0
      Truffle::Socket::Foreign.close(descriptor)

      Errno.handle('connect(2)')
    else
      IO.setup(self, descriptor, nil, true)
      binmode
    end
  end
end
