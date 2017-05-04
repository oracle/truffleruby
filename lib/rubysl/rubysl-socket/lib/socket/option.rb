class Socket < BasicSocket
  class Option
    attr_reader :family, :level, :optname, :data

    def self.bool(family, level, optname, bool)
      new(family, level, optname, [bool ? 1 : 0].pack('i'))
    end

    def self.int(family, level, optname, integer)
      new(family, level, optname, [integer].pack('i'))
    end

    def self.linger(onoff, secs)
      linger = RubySL::Socket::Foreign::Linger.new

      begin
        linger.on_off = onoff
        linger.linger = secs

        new(:UNSPEC, :SOCKET, :LINGER, linger.to_s)
      ensure
        linger.free
      end
    end

    def initialize(family, level, optname, data)
      @family  = RubySL::Socket.address_family(family)
      @level   = RubySL::Socket::SocketOptions.socket_level(level, @family)
      @optname = RubySL::Socket::SocketOptions.socket_option(@level, optname)
      @data    = data
    end

    def unpack(template)
      @data.unpack template
    end

    def inspect
      "#<#{self.class}: #@family_name #@level_name #@opt_name #{@data.inspect}>"
    end

    def bool
      expected_size = RubySL::Socket::Foreign::SIZEOF_INT

      unless @data.bytesize == expected_size
        raise TypeError,
          "invalid size, expected #{expected_size} but got #{@data.bytesize}"
      end

      if @data.unpack('i')[0] > 0
        true
      else
        false
      end
    end

    def int
      expected_size = RubySL::Socket::Foreign::SIZEOF_INT

      unless @data.bytesize == expected_size
        raise TypeError,
          "invalid size, expected #{expected_size} but got #{@data.bytesize}"
      end

      @data.unpack('i')[0]
    end

    def linger
      if @level != Socket::SOL_SOCKET || @optname != Socket::SO_LINGER
        raise TypeError, 'linger socket option expected'
      end

      expected_size = RubySL::Socket::Foreign::Linger.size

      if @data.bytesize != expected_size
        raise TypeError,
          "invalid size, expected #{expected_size} but got #{@data.bytesize}"
      end

      linger = RubySL::Socket::Foreign::Linger.from_string(@data)
      onoff  = nil

      case linger.on_off
      when 0
        onoff = false
      when 1
        onoff = true
      else
        onoff = linger.on_off.to_i
      end

      [onoff, linger.linger]
    end

    alias :to_s :data
  end
end
