module RubySL
  module Socket
    module Foreign
      class SockaddrUn < Rubinius::FFI::Struct
        config('rbx.platform.sockaddr_un', :sun_family, :sun_path)

        def self.with_sockaddr(addr)
          if addr.bytesize > size
            raise ArgumentError,
              "UNIX socket path is too long (max: #{size} bytes)"
          end

          pointer = Foreign.memory_pointer(size)
          pointer.write_string(addr, addr.bytesize)

          new(pointer)
        end

        def family
          self[:sun_family]
        end

        def to_s
          pointer.read_string(pointer.total)
        end
      end
    end
  end
end
