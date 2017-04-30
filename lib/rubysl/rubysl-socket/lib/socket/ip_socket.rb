class IPSocket < BasicSocket
  undef_method :getpeereid

  def self.getaddress(host)
    RubySL::Socket::Foreign.getaddress(host)
  end

  def addr(reverse_lookup = nil)
    RubySL::Socket.address_info(:getsockname, self, reverse_lookup)
  end

  def peeraddr(reverse_lookup=nil)
    RubySL::Socket.address_info(:getpeername, self, reverse_lookup)
  end

  def recvfrom(maxlen, flags = 0)
    flags = 0 if flags.nil?

    message, addr = recvmsg(maxlen, flags)

    aname    = RubySL::Socket.address_family_name(addr.afamily)
    hostname = addr.ip_address

    # We're re-using recvmsg which doesn't return the reverse hostname, thus
    # we'll do an extra lookup in case this is needed.
    unless do_not_reverse_lookup
      addrinfos = Socket.getaddrinfo(addr.ip_address, nil, addr.afamily,
                                     addr.socktype, addr.protocol, 0, true)

      unless addrinfos.empty?
        hostname = addrinfos[0][2]
      end
    end

    return message, [aname, addr.ip_port, hostname, addr.ip_address]
  end
end
