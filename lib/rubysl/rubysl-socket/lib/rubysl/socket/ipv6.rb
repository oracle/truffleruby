module RubySL
  module Socket
    module IPv6
      # The IPv6 loopback address as produced by inet_pton(INET6, "::1")
      LOOPBACK = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]

      # The bytes used for an unspecified IPv6 address.
      UNSPECIFIED = [0] * 16

      # The first 10 bytes of an IPv4 compatible IPv6 address.
      COMPAT_PREFIX = [0] * 10

      def self.ipv4_embedded?(bytes)
        ipv4_mapped?(bytes) || ipv4_compatible?(bytes)
      end

      def self.ipv4_mapped?(bytes)
        prefix = bytes.first(10)
        follow = bytes[10..11]

        prefix == COMPAT_PREFIX &&
          follow[0] == 255 &&
          follow[1] == 255 &&
          (bytes[-4] > 0 || bytes[-3] > 0 || bytes[-2] > 0)
      end

      def self.ipv4_compatible?(bytes)
        prefix = bytes.first(10)
        follow = bytes[10..11]

        prefix == COMPAT_PREFIX &&
          follow[0] == 0 &&
          follow[1] == 0 &&
          (bytes[-4] > 0 || bytes[-3] > 0 || bytes[-2] > 0)
      end
    end
  end
end
