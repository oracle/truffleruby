module RubySL
  module Socket
    module AncillaryData
      LEVEL_PREFIXES = {
        ::Socket::SOL_SOCKET   => %w{SCM_ UNIX},
        ::Socket::IPPROTO_IP   => %w{IP_ IP},
        ::Socket::IPPROTO_IPV6 => %w{IPV6_ IPV6},
        ::Socket::IPPROTO_TCP  => %w{TCP_ TCP},
        ::Socket::IPPROTO_UDP  => %w{UDP_ UDP}
      }

      def self.level(raw_level)
        if raw_level.is_a?(Fixnum)
          raw_level
        else
          level = Socket.coerce_to_string(raw_level)

          if level == 'SOL_SOCKET' or level == 'SOCKET'
            ::Socket::SOL_SOCKET

          # Translates "TCP" into "IPPROTO_TCP", "UDP" into "IPPROTO_UDP", etc.
          else
            Socket.prefixed_socket_constant(level, 'IPPROTO_') do
              "unknown protocol level: #{level}"
            end
          end
        end
      end

      def self.type(family, level, raw_type)
        if raw_type.is_a?(Fixnum)
          raw_type
        else
          type = Socket.coerce_to_string(raw_type)

          if family == ::Socket::AF_INET or family == ::Socket::AF_INET6
            prefix, label = LEVEL_PREFIXES[level]
          else
            prefix, label = LEVEL_PREFIXES[::Socket::SOL_SOCKET]
          end

          # Translates "RIGHTS" into "SCM_RIGHTS", "CORK" into "TCP_CORK" (when
          # the level is IPPROTO_TCP), etc.
          if prefix and label
            Socket.prefixed_socket_constant(type, prefix) do
              "Unknown #{label} control message: #{type}"
            end
          else
            raise TypeError,
              "no implicit conversion of #{type.class} into Integer"
          end
        end
      end
    end
  end
end
