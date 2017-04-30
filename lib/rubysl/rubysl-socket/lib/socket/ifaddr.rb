class Socket < BasicSocket
  class Ifaddr < Data
    attr_reader :addr, :broadaddr, :dstaddr, :flags, :ifindex, :name, :netmask

    def initialize(addr: nil, broadaddr: nil, dstaddr: nil, flags: nil, ifindex: nil, name: nil, netmask: nil)
      @addr      = addr
      @broadaddr = broadaddr
      @dstaddr   = dstaddr
      @flags     = flags
      @ifindex   = ifindex
      @name      = name
      @netmask   = netmask
    end

    def inspect
      out = "#<Socket::Ifaddr #{name}"

      if addr
        out << " #{addr.inspect_sockaddr}"
      end

      if netmask
        out << " netmask=#{netmask.inspect_sockaddr}"
      end

      out + '>'
    end
  end
end
