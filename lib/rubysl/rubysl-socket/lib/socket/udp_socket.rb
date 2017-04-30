class UDPSocket < IPSocket
  def initialize(family = Socket::AF_INET)
    @no_reverse_lookup = self.class.do_not_reverse_lookup
    @family            = RubySL::Socket.address_family(family)

    descriptor = RubySL::Socket::Foreign
      .socket(@family, Socket::SOCK_DGRAM, Socket::IPPROTO_UDP)

    Errno.handle('socket(2)') if descriptor < 0

    IO.setup(self, descriptor, nil, true)
    binmode
  end

  def bind(host, port)
    addr   = Socket.sockaddr_in(port.to_i, host)
    status = RubySL::Socket::Foreign.bind(descriptor, addr)

    Errno.handle('bind(2)') if status < 0

    0
  end

  def connect(host, port)
    sockaddr = RubySL::Socket::Foreign
      .pack_sockaddr_in(host, port.to_i, @family, Socket::SOCK_DGRAM, 0)

    status = RubySL::Socket::Foreign.connect(descriptor, sockaddr)

    RubySL::Socket::Error.write_error('connect(2)', self) if status < 0

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

  def recvfrom_nonblock(maxlen, flags = 0)
    fcntl(Fcntl::F_SETFL, Fcntl::O_NONBLOCK)

    flags = 0 if flags.nil?

    flags |= Socket::MSG_DONTWAIT

    recvfrom(maxlen, flags)
  end

  def inspect
    "#<#{self.class}:fd #{fileno}>"
  end

  def local_address
    address  = addr
    sockaddr = Socket.pack_sockaddr_in(address[1], address[3])

    Addrinfo.new(sockaddr, address[0], :DGRAM)
  end

  def remote_address
    address  = peeraddr
    sockaddr = Socket.pack_sockaddr_in(address[1], address[3])

    Addrinfo.new(sockaddr, address[0], :DGRAM)
  end
end
