class TCPServer < TCPSocket
  def initialize(host, service = nil)
    @no_reverse_lookup = self.class.do_not_reverse_lookup

    if host.is_a?(Fixnum) and service.nil?
      service = host
      host    = nil
    end

    if host.is_a?(String) and service.nil?
      begin
        service = Integer(host)
      rescue ArgumentError
        raise SocketError, "invalid port number: #{host}"
      end

      host = nil
    end

    unless service.is_a?(Fixnum)
      service = RubySL::Socket.coerce_to_string(service)
    end

    if host
      host = RubySL::Socket.coerce_to_string(host)
    else
      host = ''
    end

    remote_addrs = Socket
      .getaddrinfo(host, service, :UNSPEC, :STREAM, 0, Socket::AI_PASSIVE)

    remote_addrs.each do |addrinfo|
      _, port, address, _, family, socktype, protocol = addrinfo

      descriptor = RubySL::Socket::Foreign.socket(family, socktype, protocol)

      next if descriptor < 0

      status = RubySL::Socket::Foreign
        .bind(descriptor, Socket.sockaddr_in(port, address))

      if status < 0
        RubySL::Socket::Foreign.close(descriptor)

        Errno.handle('bind(2)')
      else
        IO.setup(self, descriptor, nil, true)
        binmode
        setsockopt(:SOCKET, :REUSEADDR, true)

        break
      end
    end

    listen(5)
  end

  def listen(backlog)
    RubySL::Socket.listen(self, backlog)
  end

  def accept
    socket, _ = RubySL::Socket.accept(self, TCPSocket)

    socket
  end

  def accept_nonblock
    socket, _ = RubySL::Socket.accept_nonblock(self, TCPSocket)

    socket
  end

  def sysaccept
    accept.fileno
  end
end
