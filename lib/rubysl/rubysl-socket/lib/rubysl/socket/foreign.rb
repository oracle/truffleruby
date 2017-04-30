module RubySL
  module Socket
    module Foreign
      extend Rubinius::FFI::Library

      SIZEOF_INT = Rubinius::FFI.type_size(:int)

      attach_function :_bind, :bind, [:int, :pointer, :socklen_t], :int
      attach_function :_connect, :connect, [:int, :pointer, :socklen_t], :int

      attach_function :accept, [:int, :pointer, :pointer], :int
      attach_function :close, [:int], :int
      attach_function :shutdown, [:int, :int], :int
      attach_function :listen, [:int, :int], :int
      attach_function :socket, [:int, :int, :int], :int
      attach_function :send, [:int, :pointer, :size_t, :int], :ssize_t

      attach_function :sendto,
        [:int, :pointer, :size_t, :int, :pointer, :socklen_t], :ssize_t

      attach_function :recv, [:int, :pointer, :size_t, :int], :ssize_t
      attach_function :recvmsg, :recvmsg, [:int, :pointer, :int], :ssize_t
      attach_function :sendmsg, :sendmsg, [:int, :pointer, :int], :ssize_t

      attach_function :recvfrom,
        [:int, :pointer, :size_t, :int, :pointer, :pointer], :int

      attach_function :_getsockopt,
        :getsockopt, [:int, :int, :int, :pointer, :pointer], :int

      attach_function :_getaddrinfo,
        :getaddrinfo, [:string, :string, :pointer, :pointer], :int

      attach_function :gai_strerror, [:int], :string

      attach_function :setsockopt,
        [:int, :int, :int, :pointer, :socklen_t], :int

      attach_function :freeaddrinfo, [:pointer], :void

      attach_function :_getpeername,
        :getpeername, [:int, :pointer, :pointer], :int

      attach_function :_getsockname,
        :getsockname, [:int, :pointer, :pointer], :int

      attach_function :_socketpair,
        :socketpair, [:int, :int, :int, :pointer], :int

      attach_function :gethostname, [:pointer, :size_t], :int
      attach_function :getservbyname, [:string, :string], :pointer
      attach_function :getservbyport, [:int, :string], :pointer
      attach_function :gethostbyname, [:string], :pointer
      attach_function :gethostbyaddr, [:pointer, :socklen_t, :int], :pointer

      attach_function :htons, [:uint16_t], :uint16_t
      attach_function :ntohs, [:uint16_t], :uint16_t

      attach_function :inet_network, [:string], :uint32_t
      attach_function :inet_pton, [:int, :string, :pointer], :int

      attach_function :_getnameinfo,
        :getnameinfo,
        [:pointer, :socklen_t, :pointer, :socklen_t, :pointer, :socklen_t, :int],
        :int

      attach_function :getifaddrs, [:pointer], :int
      attach_function :freeifaddrs, [:pointer], :void

      def self.bind(descriptor, sockaddr)
        memory_pointer(:char, sockaddr.bytesize) do |sockaddr_p|
          sockaddr_p.write_string(sockaddr, sockaddr.bytesize)

          _bind(descriptor, sockaddr_p, sockaddr.bytesize)
        end
      end

      def self.connect(descriptor, sockaddr)
        sockaddr = Socket.coerce_to_string(sockaddr)

        memory_pointer(:char, sockaddr.bytesize) do |sockaddr_p|
          sockaddr_p.write_string(sockaddr, sockaddr.bytesize)

          _connect(descriptor, sockaddr_p, sockaddr.bytesize)
        end
      end

      def self.getsockopt(descriptor, level, optname)
        memory_pointer(256) do |val|
          memory_pointer(:socklen_t) do |length|
            length.write_int(256)

            err = _getsockopt(descriptor, level, optname, val, length)

            Errno.handle('Unable to get socket option') unless err == 0

            return val.read_string(length.read_int)
          end
        end
      end

      def self.getaddrinfo(host, service = nil, family = nil, socktype = nil,
                           protocol = nil, flags = nil)
        hints = Addrinfo.new

        hints[:ai_family]   = family || 0
        hints[:ai_socktype] = socktype || 0
        hints[:ai_protocol] = protocol || 0
        hints[:ai_flags]    = flags || 0

        res_p = memory_pointer(:pointer)
        err   = _getaddrinfo(host, service, hints.pointer, res_p)

        raise SocketError, gai_strerror(err) unless err == 0

        ptr = res_p.read_pointer

        return [] unless ptr

        res = Addrinfo.new(ptr)

        addrinfos = []

        while true
          addrinfo = []
          addrinfo << res[:ai_flags]
          addrinfo << res[:ai_family]
          addrinfo << res[:ai_socktype]
          addrinfo << res[:ai_protocol]
          addrinfo << res[:ai_addr].read_string(res[:ai_addrlen])
          addrinfo << res[:ai_canonname]

          addrinfos << addrinfo

          break unless res[:ai_next]

          res = Addrinfo.new(res[:ai_next])
        end

        return addrinfos
      ensure
        hints.free if hints

        if res_p
          ptr = res_p.read_pointer

          # Be sure to feed a legit pointer to freeaddrinfo
          freeaddrinfo(ptr) if ptr && !ptr.null?

          res_p.free
        end
      end

      def self.getaddress(host)
        addrinfos = getaddrinfo(host)

        unpack_sockaddr_in(addrinfos.first[4], false).first
      end

      def self.getnameinfo(sockaddr, flags = ::Socket::NI_NUMERICHOST | ::Socket::NI_NUMERICSERV,
                           reverse_lookup = !BasicSocket.do_not_reverse_lookup)
        name_info = []

        char_pointer(sockaddr.bytesize) do |sockaddr_p|
          char_pointer(::Socket::NI_MAXHOST) do |node|
            char_pointer(::Socket::NI_MAXSERV) do |service|
              sockaddr_p.write_string(sockaddr, sockaddr.bytesize)

              if reverse_lookup
                err = _getnameinfo(sockaddr_p, sockaddr.bytesize, node,
                                   ::Socket::NI_MAXHOST, nil, 0, 0)

                name_info[2] = node.read_string if err == 0
              end

              err = _getnameinfo(sockaddr_p, sockaddr.bytesize, node,
                                 ::Socket::NI_MAXHOST, service,
                                 ::Socket::NI_MAXSERV, flags)

              raise SocketError, gai_strerror(err) unless err == 0

              sa_family = SockaddrIn.with_sockaddr(sockaddr)[:sin_family]

              name_info[0] = ::Socket::Constants::AF_TO_FAMILY[sa_family]
              name_info[1] = service.read_string
              name_info[3] = node.read_string
            end
          end
        end

        name_info[2] = name_info[3] unless name_info[2]

        name_info
      end

      def self.getpeername(descriptor)
        memory_pointer(:char, 128) do |sockaddr_storage_p|
          memory_pointer(:socklen_t) do |len_p|
            len_p.write_int(128)

            err = _getpeername(descriptor, sockaddr_storage_p, len_p)

            Errno.handle('getpeername(2)') unless err == 0

            sockaddr_storage_p.read_string(len_p.read_int)
          end
        end
      end

      def self.getsockname(descriptor)
        memory_pointer(:char, 128) do |sockaddr_storage_p|
          memory_pointer(:socklen_t) do |len_p|
            len_p.write_int(128)
            err = _getsockname(descriptor, sockaddr_storage_p, len_p)

            Errno.handle('getsockname(2)') unless err == 0

            sockaddr_storage_p.read_string(len_p.read_int)
          end
        end
      end

      def self.pack_sockaddr_in(host, port, family = ::Socket::AF_UNSPEC,
                                type = 0, flags = 0)
        hints = Addrinfo.new

        hints[:ai_family]   = family
        hints[:ai_socktype] = type
        hints[:ai_flags]    = flags

        if host and host.empty?
          host = "0.0.0.0"
        end

        res_p = memory_pointer(:pointer)

        err = _getaddrinfo(host, port.to_s, hints.pointer, res_p)

        raise SocketError, gai_strerror(err) unless err == 0

        return [] if res_p.read_pointer.null?

        res = Addrinfo.new(res_p.read_pointer)

        return res[:ai_addr].read_string(res[:ai_addrlen])
      ensure
        hints.free if hints

        if res_p
          ptr = res_p.read_pointer

          freeaddrinfo(ptr) if ptr && !ptr.null?

          res_p.free
        end
      end

      def self.unpack_sockaddr_in(sockaddr, reverse_lookup)
        family, port, host, ip = getnameinfo(
          sockaddr,
          ::Socket::NI_NUMERICHOST | ::Socket::NI_NUMERICSERV,
          reverse_lookup
        )

        # On some systems this doesn't fail for families other than AF_INET(6)
        # so we raise manually here.
        unless family =~ /AF_INET/
          raise ArgumentError, 'not an AF_INET/AF_INET6 sockaddr'
        end

        return host, ip, port.to_i
      end

      def self.getpeereid(*)
        raise NotImplementedError,
          'getpeereid() is not supported on this platform'
      end

      def self.socketpair(family, type, protocol)
        memory_pointer(:int, 2) do |pointer|
          _socketpair(family, type, protocol, pointer)

          pointer.read_array_of_int(2)
        end
      end

      def self.char_pointer(length, &block)
        memory_pointer(:char, length, &block)
      end

      def self.memory_pointer(*args, &block)
        Rubinius::FFI::MemoryPointer.new(*args, &block)
      end

      def self.pointers_of_type(current, type)
        pointers = []
        size     = Rubinius::FFI.type_size(type)
        pointer  = current.read_pointer

        until pointer.null?
          pointers << pointer

          current = current + size
          pointer = current.read_pointer
        end

        pointers
      end

      def self.ip_to_bytes(family, address)
        size = 16

        memory_pointer(:pointer, size) do |pointer|
          status = inet_pton(family, address, pointer)

          Errno.handle('inet_pton()') if status < 1

          pointer.get_array_of_uchar(0, size)
        end
      end
    end
  end
end
