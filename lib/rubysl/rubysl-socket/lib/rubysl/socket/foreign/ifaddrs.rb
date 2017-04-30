module RubySL
  module Socket
    module Foreign
      # Class representing an "ifaddrs" C structure.
      #
      # The memory used by this structure should be free'd using
      # `RubySL::Socket::Foreign.freeifaddrs` _only_ as following:
      #
      #     RubySL::Socket::Foreign.freeifaddrs(initial_ifaddrs_struct)
      #
      # To ensure Rubinius doesn't accidentally free invalid pointers all
      # pointers (including the pointer of "self") stored in this class have the
      # "autorelease" option set to false.
      class Ifaddrs < Rubinius::FFI::Struct
        config("rbx.platform.ifaddrs", :ifa_next, :ifa_name, :ifa_flags,
               :ifa_addr, :ifa_netmask, :ifa_broadaddr, :ifa_dstaddr)

        POINTERS = [
          :address, :next, :broadcast_address, :destination_address,
          :netmask_address
        ]

        def initialize(*args)
          super

          POINTERS.each do |name|
            pointer = __send__(name)

            pointer.autorelease = false if pointer
          end

          pointer.autorelease = false
        end

        def name
          self[:ifa_name]
        end

        def flags
          self[:ifa_flags]
        end

        def data
          @data ||= self[:ifa_data]
        end

        def next
          @next ||= self[:ifa_next]
        end

        def address
          @address ||= self[:ifa_addr]
        end

        def broadcast_address
          @broadcast_address ||= self[:ifa_broadaddr]
        end

        def destination_address
          @destination_address ||= self[:ifa_dstaddr]
        end

        def netmask_address
          @netmask_address ||= self[:ifa_netmask]
        end

        def each_address
          next_pointer = self.next

          while next_pointer
            struct = self.class.new(next_pointer)

            yield struct

            next_pointer = struct.next
          end
        end

        def broadcast?
          flags & ::Socket::IFF_BROADCAST > 0
        end

        def point_to_point?
          flags & ::Socket::IFF_POINTOPOINT > 0
        end

        def address_to_addrinfo
          return unless address

          sockaddr = Sockaddr.new(address)

          if sockaddr.family == ::Socket::AF_INET
            ::Addrinfo.new(SockaddrIn.new(address).to_s)
          elsif sockaddr.family == ::Socket::AF_INET6
            ::Addrinfo.new(SockaddrIn6.new(address).to_s)
          else
            nil
          end
        end

        def broadcast_to_addrinfo
          return if !broadcast? || !broadcast_address

          ::Addrinfo.raw_with_family(Sockaddr.new(broadcast_address).family)
        end

        def destination_to_addrinfo
          return if !point_to_point? || !destination_address

          ::Addrinfo.raw_with_family(Sockaddr.new(destination_address).family)
        end

        def netmask_to_addrinfo
          return unless netmask_address

          sockaddr = Sockaddr.new(netmask_address)

          if sockaddr.family == ::Socket::AF_INET
            ::Addrinfo.new(SockaddrIn.new(netmask_address).to_s)
          elsif sockaddr.family == ::Socket::AF_INET6
            ::Addrinfo.new(SockaddrIn6.new(netmask_address).to_s)
          else
            ::Addrinfo.raw_with_family(sockaddr.family)
          end
        end
      end
    end
  end
end
