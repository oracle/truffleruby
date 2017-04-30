module RubySL
  module Socket
    module Foreign
      class SockaddrIn6 < Rubinius::FFI::Struct
        config("rbx.platform.sockaddr_in6", :sin6_family, :sin6_port,
               :sin6_flowinfo, :sin6_addr, :sin6_scope_id)

        def self.with_sockaddr(addr)
          pointer = Foreign.memory_pointer(addr.bytesize)
          pointer.write_string(addr, addr.bytesize)

          new(pointer)
        end

        def family
          self[:sin6_family]
        end

        def to_s
          pointer.read_string(self.class.size)
        end
      end
    end
  end
end
