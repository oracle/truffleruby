class Socket < BasicSocket
  class AncillaryData
    attr_reader :family, :level, :type

    def self.int(family, level, type, integer)
      new(family, level, type, [integer].pack('I'))
    end

    def self.unix_rights(*ios)
      descriptors = ios.map do |io|
        unless io.is_a?(IO)
          raise TypeError, "IO expected, got #{io.class} instead"
        end

        io.fileno
      end

      instance = new(:UNIX, :SOCKET, :RIGHTS, descriptors.pack('I*'))

      # MRI sets this using a hidden instance variable ("unix_rights"). Because
      # you can't set hidden instance variables from within Ruby we'll have to
      # use a regular instance variable. Lets hope people don't mess with it.
      instance.instance_variable_set(:@unix_rights, ios)

      instance
    end

    def self.ip_pktinfo(addr, ifindex, spec_dst = nil)
      spec_dst ||= addr

      instance = new(:INET, :IP, :PKTINFO, '')
      pkt_info = [
        Addrinfo.ip(addr.ip_address),
        ifindex,
        Addrinfo.ip(spec_dst.ip_address)
      ]

      instance.instance_variable_set(:@ip_pktinfo, pkt_info)

      instance
    end

    def self.ipv6_pktinfo(addr, ifindex)
      instance = new(:INET6, :IPV6, :PKTINFO, '')
      pkt_info = [Addrinfo.ip(addr.ip_address), ifindex]

      instance.instance_variable_set(:@ipv6_pktinfo, pkt_info)

      instance
    end

    def initialize(family, level, type, data)
      @family = RubySL::Socket.address_family(family)
      @data   = RubySL::Socket.coerce_to_string(data)
      @level  = RubySL::Socket::AncillaryData.level(level)
      @type   = RubySL::Socket::AncillaryData.type(@family, @level, type)
    end

    def cmsg_is?(level, type)
      level = RubySL::Socket::AncillaryData.level(level)
      type  = RubySL::Socket::AncillaryData.type(@family, level, type)

      @level == level && @type == type
    end

    def int
      unpacked = @data.unpack('I')[0]

      unless unpacked
        raise TypeError, 'data could not be unpacked into a Fixnum'
      end

      unpacked
    end

    def unix_rights
      if @level != Socket::SOL_SOCKET or @type != Socket::SCM_RIGHTS
        raise TypeError, 'SCM_RIGHTS ancillary data expected'
      end

      @unix_rights
    end

    def data
      if @ip_pktinfo or @ipv6_pktinfo
        raise NotImplementedError,
          'AncillaryData#data is not supported as its output depends on ' \
          'MRI specific internals, use #ip_pktinfo or #ipv6_pktinfo instead'
      else
        @data
      end
    end

    def ip_pktinfo
      addr, ifindex, spec = @ip_pktinfo

      [addr.dup, ifindex, spec.dup]
    end

    def ipv6_pktinfo
      addr, ifindex = @ipv6_pktinfo

      [addr.dup, ifindex]
    end

    def ipv6_pktinfo_addr
      ipv6_pktinfo[0]
    end

    def ipv6_pktinfo_ifindex
      ipv6_pktinfo[1]
    end
  end
end
