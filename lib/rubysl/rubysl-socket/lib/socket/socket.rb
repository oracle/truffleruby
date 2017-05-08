class Socket < BasicSocket
  def self.ip_address_list
    ips = []

    getifaddrs.each do |ifaddr|
      ips << ifaddr.addr if ifaddr.addr
    end

    ips
  end

  def self.getaddrinfo(host, service, family = 0, socktype = 0,
                       protocol = 0, flags = 0, reverse_lookup = nil)
    if host
      host = RubySL::Socket.coerce_to_string(host)
    end

    if host && (host.empty? || host == '<any>')
      host = "0.0.0.0"
    elsif host == '<broadcast>'
      host = '255.255.255.255'
    end

    if service.kind_of?(Fixnum)
      service = service.to_s
    elsif service
      service = RubySL::Socket.coerce_to_string(service)
    end

    family    = RubySL::Socket.address_family(family)
    socktype  = RubySL::Socket.socket_type(socktype)
    addrinfos = RubySL::Socket::Foreign
      .getaddrinfo(host, service, family, socktype, protocol, flags)

    reverse_lookup = RubySL::Socket
      .convert_reverse_lookup(nil, reverse_lookup)

    addrinfos.map do |ai|
      addrinfo = []

      unpacked = RubySL::Socket::Foreign
        .unpack_sockaddr_in(ai[4], reverse_lookup)

      addrinfo << Socket::Constants::AF_TO_FAMILY[ai[1]]
      addrinfo << unpacked.pop # port

      # TODO BJF 30-Apr-2017 Why is this broken?
      # Canonical host is present (e.g. when AI_CANONNAME was used)
      # if ai[5] and !reverse_lookup
      #   unpacked[0] = ai[5]
      # end

      addrinfo.concat(unpacked) # hosts

      addrinfo << ai[1] # family
      addrinfo << ai[2] # socktype
      addrinfo << ai[3] # protocol

      addrinfo
    end
  end

  def self.getnameinfo(sockaddr, flags = 0)
    port   = nil
    host   = nil
    family = Socket::AF_UNSPEC

    if sockaddr.is_a?(Array)
      if sockaddr.size == 3
        af, port, host = sockaddr
      elsif sockaddr.size == 4
        af   = sockaddr[0]
        port = sockaddr[1]
        host = sockaddr[3] || sockaddr[2]
      else
        raise ArgumentError,
          "array size should be 3 or 4, #{sockaddr.size} given"
      end

      if af == 'AF_INET'
        family = Socket::AF_INET
      elsif af == 'AF_INET6'
        family = Socket::AF_INET6
      end

      sockaddr = RubySL::Socket::Foreign
        .pack_sockaddr_in(host, port, family, Socket::SOCK_STREAM, 0)
    end

    _, port, host, _ = RubySL::Socket::Foreign.getnameinfo(sockaddr, flags)

    [host, port]
  end

  def self.gethostname
    RubySL::Socket::Foreign.char_pointer(::Socket::NI_MAXHOST) do |pointer|
      RubySL::Socket::Foreign.gethostname(pointer, pointer.total)

      pointer.read_string
    end
  end

  def self.gethostbyname(hostname)
    addrinfos = Socket
      .getaddrinfo(hostname, nil, nil, :STREAM, nil, Socket::AI_CANONNAME)

    hostname     = addrinfos[0][2]
    family       = addrinfos[0][4]
    addresses    = []
    alternatives = RubySL::Socket.aliases_for_hostname(hostname)

    addrinfos.each do |a|
      sockaddr = Socket.sockaddr_in(0, a[3])


      if a[4] == AF_INET
        offset, type = RubySL::Socket::Foreign::SockaddrIn.layout[:sin_addr]
        size = FFI.type_size(type)  # TODO BJF 30-Apr-2017 This appears to be a bug in rubysl-socket?
        addresses << sockaddr.byteslice(offset, size)
      elsif a[4] == AF_INET6
        offset, type = RubySL::Socket::Foreign::SockaddrIn6.layout[:sin6_addr]
        size = FFI.type_size(type)  # TODO BJF 30-Apr-2017 This appears to be a bug in rubysl-socket?
        addresses << sockaddr.byteslice(offset, size)
      end
    end

    [hostname, alternatives, family, *addresses]
  end

  def self.gethostbyaddr(addr, family = nil)
    if !family and addr.bytesize == 16
      family = Socket::AF_INET6
    elsif !family
      family = Socket::AF_INET
    end

    family = RubySL::Socket.address_family(family)

    RubySL::Socket::Foreign.char_pointer(addr.bytesize) do |in_pointer|
      in_pointer.write_string(addr)

      out_pointer = RubySL::Socket::Foreign
        .gethostbyaddr(in_pointer, in_pointer.total, family)

      unless out_pointer
        raise SocketError, "No host found for address #{addr.inspect}"
      end

      struct = RubySL::Socket::Foreign::Hostent.new(out_pointer)

      [struct.hostname, struct.aliases, struct.type, *struct.addresses]
    end
  end

  def self.getservbyname(service, proto = 'tcp')
    pointer = RubySL::Socket::Foreign.getservbyname(service, proto)

    raise SocketError, "no such service #{service}/#{proto}" unless pointer

    struct = RubySL::Socket::Foreign::Servent.new(pointer)

    RubySL::Socket::Foreign.ntohs(struct.port)
  end

  def self.getservbyport(port, proto = nil)
    proto ||= 'tcp'
    pointer = RubySL::Socket::Foreign.getservbyport(port, proto)

    raise SocketError, "no such service for port #{port}/#{proto}" unless pointer

    struct = RubySL::Socket::Foreign::Servent.new(pointer)

    struct.name
  end

  def self.getifaddrs
    initial = RubySL::Socket::Foreign::Ifaddrs.new
    status  = RubySL::Socket::Foreign.getifaddrs(initial)
    ifaddrs = []
    index   = 1

    Errno.handle('getifaddrs()') if status < 0

    begin
      initial.each_address do |ifaddrs_struct|
        ifaddrs << Ifaddr.new(
          addr:      ifaddrs_struct.address_to_addrinfo,
          broadaddr: ifaddrs_struct.broadcast_to_addrinfo,
          dstaddr:   ifaddrs_struct.destination_to_addrinfo,
          netmask:   ifaddrs_struct.netmask_to_addrinfo,
          name:      ifaddrs_struct.name,
          flags:     ifaddrs_struct.flags,
          ifindex:   index
        )

        index += 1
      end

      ifaddrs
    ensure
      RubySL::Socket::Foreign.freeifaddrs(initial)
    end
  end

  def self.pack_sockaddr_in(port, host)
    RubySL::Socket::Foreign.pack_sockaddr_in(host, port)
  end

  def self.unpack_sockaddr_in(sockaddr)
    _, address, port = RubySL::Socket::Foreign
      .unpack_sockaddr_in(sockaddr, false)

    return port, address
  rescue SocketError => e
    if e.message =~ /ai_family not supported/
      raise ArgumentError, 'not an AF_INET/AF_INET6 sockaddr'
    else
      raise e
    end
  end

  def self.socketpair(family, type, protocol = 0)
    family = RubySL::Socket.address_family(family)
    type   = RubySL::Socket.socket_type(type)

    fd0, fd1 = RubySL::Socket::Foreign.socketpair(family, type, protocol)

    [for_fd(fd0), for_fd(fd1)]
  end

  class << self
    alias_method :sockaddr_in, :pack_sockaddr_in
    alias_method :pair, :socketpair
  end

  if RubySL::Socket.unix_socket_support?
    def self.pack_sockaddr_un(file)
      max_path_size =  Rubinius::Config['rbx.platform.sockaddr_un.sun_path.size'] - 1
      if file.bytesize > max_path_size
        raise ArgumentError, "too long unix socket path (#{file.bytesize} bytes given but #{max_path_size} bytes max)"
      end

      struct = RubySL::Socket::Foreign::SockaddrUn.new
      struct[:sun_family] = Socket::AF_UNIX
      struct[:sun_path] = file

      begin
        struct.to_s
      ensure
        struct.free
      end
    end

    def self.unpack_sockaddr_un(addr)
      struct = RubySL::Socket::Foreign::SockaddrUn.with_sockaddr(addr)

      begin
        struct[:sun_path].to_s
      ensure
        struct.free
      end
    end

    class << self
      alias_method :sockaddr_un, :pack_sockaddr_un
    end
  end

  def initialize(family, socket_type, protocol = 0)
    @no_reverse_lookup = self.class.do_not_reverse_lookup

    @family      = RubySL::Socket.protocol_family(family)
    @socket_type = RubySL::Socket.socket_type(socket_type)

    descriptor = RubySL::Socket::Foreign.socket(@family, @socket_type, protocol)

    Errno.handle('socket(2)') if descriptor < 0

    IO.setup(self, descriptor, nil, true)
    binmode
  end

  def bind(addr)
    if addr.is_a?(Addrinfo)
      addr = addr.to_sockaddr
    end

    err = RubySL::Socket::Foreign.bind(descriptor, addr)

    Errno.handle('bind(2)') unless err == 0

    0
  end

  def connect(sockaddr)
    if sockaddr.is_a?(Addrinfo)
      sockaddr = sockaddr.to_sockaddr
    end

    status = RubySL::Socket::Foreign.connect(descriptor, sockaddr)

    RubySL::Socket::Error.write_error('connect(2)', self) if status < 0

    0
  end

  def connect_nonblock(sockaddr)
    fcntl(Fcntl::F_SETFL, Fcntl::O_NONBLOCK)

    if sockaddr.is_a?(Addrinfo)
      sockaddr = sockaddr.to_sockaddr
    end

    status = RubySL::Socket::Foreign.connect(descriptor, sockaddr)

    RubySL::Socket::Error.write_nonblock('connect(2)') if status < 0

    0
  end

  def local_address
    sockaddr = RubySL::Socket::Foreign.getsockname(descriptor)

    Addrinfo.new(sockaddr, @family, @socket_type, 0)
  end

  def remote_address
    sockaddr = RubySL::Socket::Foreign.getpeername(descriptor)

    Addrinfo.new(sockaddr, @family, @socket_type, 0)
  end

  def recvfrom(bytes, flags = 0)
    message, addr = recvmsg(bytes, flags)

    return message, addr
  end

  def recvfrom_nonblock(bytes, flags = 0)
    message, addr = recvmsg_nonblock(bytes, flags)

    return message, addr
  end

  def listen(backlog)
    RubySL::Socket.listen(self, backlog)
  end

  def accept
    RubySL::Socket.accept(self, Socket)
  end

  def accept_nonblock
    RubySL::Socket.accept_nonblock(self, Socket)
  end

  def sysaccept
    socket, addrinfo = accept

    return socket.fileno, addrinfo
  end
end
