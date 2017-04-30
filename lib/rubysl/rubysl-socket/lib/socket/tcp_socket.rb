class TCPSocket < IPSocket
  def self.gethostbyname(hostname)
    addrinfos = Socket
      .getaddrinfo(hostname, nil, nil, :STREAM, nil, Socket::AI_CANONNAME)

    hostname     = addrinfos[0][2]
    family       = addrinfos[0][4]
    addresses    = addrinfos.map { |a| a[3] }
    alternatives = []

    RubySL::Socket.aliases_for_hostname(hostname).each do |name|
      alternatives << name unless name == hostname
    end

    [hostname, alternatives, family, *addresses]
  end

  def initialize(host, service, local_host = nil, local_service = nil)
    @no_reverse_lookup = self.class.do_not_reverse_lookup

    if host
      host = RubySL::Socket.coerce_to_string(host)
    end

    if service.is_a?(Fixnum)
      service = service.to_s
    else
      service = RubySL::Socket.coerce_to_string(service)
    end

    local_addrinfo = nil

    # When a local address and/or service/port are given we should bind the
    # socket to said address (besides also connecting to the remote address).
    if local_host or local_service
      if local_host
        local_host = RubySL::Socket.coerce_to_string(local_host)
      end

      if local_service.is_a?(Fixnum)
        local_service = local_service.to_s
      elsif local_service
        local_service = RubySL::Socket.coerce_to_string(local_service)
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

      descriptor = RubySL::Socket::Foreign.socket(family, socktype, protocol)

      next if descriptor < 0

      # If any local address details were given we should bind to one that
      # matches the remote address connected to above.
      if local_addrinfo
        local_info = local_addrinfo.find do |addr|
          addr[4] == family && addr[5] == socktype
        end

        if local_info
          status = RubySL::Socket::Foreign
            .bind(descriptor, Socket.sockaddr_in(local_info[1], local_info[2]))

          Errno.handle('bind(2)') if status < 0
        end
      end

      connect_status = RubySL::Socket::Foreign
        .connect(descriptor, Socket.sockaddr_in(port, address))

      break if connect_status >= 0
    end

    if connect_status < 0
      RubySL::Socket::Foreign.close(descriptor)

      Errno.handle('connect(2)')
    else
      IO.setup(self, descriptor, nil, true)
      binmode
    end
  end

  def local_address
    address  = addr
    sockaddr = Socket.pack_sockaddr_in(address[1], address[3])

    Addrinfo.new(sockaddr, address[0], :STREAM)
  end

  def remote_address
    address  = peeraddr
    sockaddr = Socket.pack_sockaddr_in(address[1], address[3])

    Addrinfo.new(sockaddr, address[0], :STREAM)
  end
end
