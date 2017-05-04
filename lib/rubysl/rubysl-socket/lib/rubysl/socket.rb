module RubySL
  module Socket
    def self.bsd_support?
      Rubinius.bsd? || Rubinius.darwin?
    end

    def self.linux_support?
      Rubinius.linux?
    end

    def self.unix_socket_support?
      ::Socket::Constants.const_defined?(:AF_UNIX)
    end

    def self.aliases_for_hostname(hostname)
      pointer = Foreign.gethostbyname(hostname)

      Foreign::Hostent.new(pointer).aliases
    end

    def self.sockaddr_class_for_socket(socket)
      if socket.is_a?(::UNIXSocket)
        return Foreign::SockaddrUn
      end

      # Socket created using for example Socket.unix('foo')
      if socket.is_a?(::Socket) and
        socket.instance_variable_get(:@family) == ::Socket::AF_UNIX
        return Foreign::SockaddrUn
      end

      case address_info(:getsockname, socket)[0]
      when 'AF_INET6'
        Foreign::SockaddrIn6
      when 'AF_UNIX'
        Foreign::SockaddrUn
      else
        Foreign::SockaddrIn
      end
    end

    def self.accept(source, new_class)
      raise IOError, 'socket has been closed' if source.closed?

      sockaddr = sockaddr_class_for_socket(source).new

      begin
        fd = RubySL::Socket::Foreign.memory_pointer(:int) do |size_p|
          size_p.write_int(sockaddr.size)

          RubySL::Socket::Foreign
            .accept(source.descriptor, sockaddr.pointer, size_p)
        end

        Error.read_error('accept(2)', source) if fd < 0

        socket = new_class.allocate

        ::IO.setup(socket, fd, nil, true)
        socket.binmode

        socktype = source.getsockopt(:SOCKET, :TYPE).int
        addrinfo = Addrinfo.new(sockaddr.to_s, sockaddr.family, socktype)

        return socket, addrinfo
      ensure
        sockaddr.free
      end
    end

    def self.accept_nonblock(source, new_class)
      source.fcntl(::Fcntl::F_SETFL, ::Fcntl::O_NONBLOCK)

      accept(source, new_class)
    end

    def self.listen(source, backlog)
      backlog = Rubinius::Type.coerce_to(backlog, Fixnum, :to_int)
      err     = Foreign.listen(source.descriptor, backlog)

      Error.read_error('listen(2)', source) if err < 0

      0
    end

    def self.family_for_sockaddr_in(sockaddr)
      case sockaddr.bytesize
      when Foreign::SockaddrIn6.size
        ::Socket::AF_INET6
      when Foreign::SockaddrIn.size
        ::Socket::AF_INET
      # UNIX socket addresses can have a variable size as sometimes any trailing
      # null bytes are stripped (e.g. when calling UNIXServer#getsockname).
      else
        ::Socket::AF_UNIX
      end
    end

    def self.constant_pairs
      Rubinius::FFI.config_hash('socket').reject { |name, value| value.empty? }
    end

    def self.coerce_to_string(object)
      if object.is_a?(String) or object.is_a?(Symbol)
        object.to_s
      elsif object.respond_to?(:to_str)
        Rubinius::Type.coerce_to(object, String, :to_str)
      else
        raise TypeError, "no implicit conversion of #{object.inspect} into Integer"
      end
    end

    def self.family_prefix?(family)
      family.start_with?('AF_') || family.start_with?('PF_')
    end

    def self.prefix_with(name, prefix)
      unless name.start_with?(prefix)
        name = "#{prefix}#{name}"
      end

      name
    end

    def self.prefixed_socket_constant(name, prefix, &block)
      prefixed = prefix_with(name, prefix)

      socket_constant(prefixed, &block)
    end

    def self.socket_constant(name)
      if ::Socket.const_defined?(name)
        ::Socket.const_get(name)
      else
        raise SocketError, yield
      end
    end

    def self.address_family(family)
      case family
      when Symbol, String
        f = family.to_s

        unless family_prefix?(f)
          f = 'AF_' + f
        end

        if ::Socket.const_defined?(f)
          ::Socket.const_get(f)
        else
          raise SocketError, "unknown socket domain: #{family}"
        end
      when Integer
        family
      when NilClass
        ::Socket::AF_UNSPEC
      else
        if family.respond_to?(:to_str)
          address_family(Rubinius::Type.coerce_to(family, String, :to_str))
        else
          raise SocketError, "unknown socket domain: #{family}"
        end
      end
    end

    def self.address_family_name(family_int)
      # Both AF_LOCAL and AF_UNIX use value 1. CRuby seems to prefer AF_UNIX
      # over AF_LOCAL.
      if family_int == ::Socket::AF_UNIX && family_int == ::Socket::AF_LOCAL
        return 'AF_UNIX'
      end

      ::Socket.constants.grep(/^AF_/).each do |name|
        return name.to_s if ::Socket.const_get(name) == family_int
      end

      'AF_UNSPEC'
    end

    def self.protocol_family_name(family_int)
      # Both PF_LOCAL and PF_UNIX use value 1. CRuby seems to prefer PF_UNIX
      # over PF_LOCAL.
      if family_int == ::Socket::PF_UNIX && family_int == ::Socket::PF_LOCAL
        return 'PF_UNIX'
      end

      ::Socket.constants.grep(/^PF_/).each do |name|
        return name.to_s if ::Socket.const_get(name) == family_int
      end

      'PF_UNSPEC'
    end

    def self.protocol_name(family_int)
      ::Socket.constants.grep(/^IPPROTO_/).each do |name|
        return name.to_s if ::Socket.const_get(name) == family_int
      end

      'IPPROTO_IP'
    end

    def self.socket_type_name(socktype)
      ::Socket.constants.grep(/^SOCK_/).each do |name|
        return name.to_s if ::Socket.const_get(name) == socktype
      end

      nil
    end

    def self.protocol_family(family)
      case family
      when Symbol, String
        f = family.to_s

        unless family_prefix?(f)
          f = 'PF_' + f
        end

        if ::Socket.const_defined?(f)
          ::Socket.const_get(f)
        else
          raise SocketError, "unknown socket domain: #{family}"
        end
      when Integer
        family
      when NilClass
        ::Socket::PF_UNSPEC
      else
        if family.respond_to?(:to_str)
          protocol_family(Rubinius::Type.coerce_to(family, String, :to_str))
        else
          raise SocketError, "unknown socket domain: #{family}"
        end
      end
    end

    def self.socket_type(type)
      case type
      when Symbol, String
        t = type.to_s

        if t[0..4] != 'SOCK_'
          t = "SOCK_#{t}"
        end

        if ::Socket.const_defined?(t)
          ::Socket.const_get(t)
        else
          raise SocketError, "unknown socket type: #{type}"
        end
      when Integer
        type
      when NilClass
        0
      else
        if type.respond_to?(:to_str)
          socket_type(Rubinius::Type.coerce_to(type, String, :to_str))
        else
          raise SocketError, "unknown socket type: #{type}"
        end
      end
    end

    def self.convert_reverse_lookup(socket = nil, reverse_lookup = nil)
      if reverse_lookup.nil?
        if socket
          reverse_lookup = !socket.do_not_reverse_lookup
        else
          reverse_lookup = !BasicSocket.do_not_reverse_lookup
        end

      elsif reverse_lookup == :hostname
        reverse_lookup = true

      elsif reverse_lookup == :numeric
        reverse_lookup = false

      elsif reverse_lookup != true and reverse_lookup != false
        raise ArgumentError,
          "invalid reverse_lookup flag: #{reverse_lookup.inspect}"
      end

      reverse_lookup
    end

    def self.address_info(method, socket, reverse_lookup = nil)
      sockaddr = Foreign.__send__(method, socket.descriptor)

      reverse_lookup = convert_reverse_lookup(socket, reverse_lookup)

      options = ::Socket::Constants::NI_NUMERICHOST |
        ::Socket::Constants::NI_NUMERICSERV

      family, port, host, ip = Foreign
        .getnameinfo(sockaddr, options, reverse_lookup)

      [family, port.to_i, host, ip]
    end

    def self.shutdown_option(how)
      case how
      when String, Symbol
        prefixed_socket_constant(how.to_s, 'SHUT_') do
          "unknown shutdown argument: #{how}"
        end
      when Fixnum
        if how == ::Socket::SHUT_RD or
          how == ::Socket::SHUT_WR or
          how == ::Socket::SHUT_RDWR
          how
        else
          raise ArgumentError,
            'argument should be :SHUT_RD, :SHUT_WR, or :SHUT_RDWR'
        end
      else
        if how.respond_to?(:to_str)
          shutdown_option(coerce_to_string(how))
        else
          raise TypeError,
            "no implicit conversion of #{how.class} into Integer"
        end
      end
    end
  end
end
