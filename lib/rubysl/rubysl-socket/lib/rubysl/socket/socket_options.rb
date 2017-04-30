module RubySL
  module Socket
    module SocketOptions
      def self.socket_level(level, family = nil)
        if level.is_a?(Symbol) or level.is_a?(String)
          if ::Socket.const_defined?(level)
            ::Socket.const_get(level)
          else
            if family and is_ip_family?(family)
              ip_level_to_int(level)
            elsif level.to_s == 'SOCKET'
              ::Socket::SOL_SOCKET
            else
              constant("IPPROTO", level)
            end
          end
        elsif level.respond_to?(:to_str)
          socket_level(Socket.coerce_to_string(level), family)
        else
          level
        end
      end

      def self.socket_option(level, optname)
        case optname
        when Symbol, String
          if ::Socket.const_defined?(optname)
            ::Socket.const_get(optname)
          else
            case level
            when ::Socket::SOL_SOCKET
              constant("SO", optname)
            when ::Socket::IPPROTO_IP
              constant("IP", optname)
            when ::Socket::IPPROTO_TCP
              constant("TCP", optname)
            when ::Socket::IPPROTO_UDP
              constant("UDP", optname)
            when ::Socket.const_defined?(:IPPROTO_IPV6) && ::Socket::IPPROTO_IPV6
              constant("IPV6", optname)
            else
              raise SocketError,
                "Unsupported socket level option name: #{optname}"
            end
          end
        else
          if optname.respond_to?(:to_str)
            socket_option(level, Socket.coerce_to_string(optname))
          else
            optname
          end
        end
      end

      def self.is_ip_family?(family)
        family == "AF_INET" || family == "AF_INET6"
      end

      def self.ip_level_to_int(level)
        prefixes = ["IPPROTO", "SOL", "IPV6"]

        prefixes.each do |prefix|
          const = "#{prefix}_#{level}"

          if ::Socket.const_defined?(const)
            return ::Socket.const_get(const)
          end
        end

        nil
      end

      def self.constant(prefix, suffix)
        const = "#{prefix}_#{suffix}"

        if ::Socket.const_defined?(const)
          ::Socket.const_get(const)
        else
          raise SocketError, "Undefined socket constant: #{const}"
        end
      end
    end
  end
end
