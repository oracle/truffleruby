module RubySL
  module Socket
    module Foreign
      class Msghdr < Rubinius::FFI::Struct
        config('rbx.platform.msghdr', :msg_name, :msg_namelen, :msg_iov,
               :msg_iovlen, :msg_control, :msg_controllen, :msg_flags)

        def self.with_buffers(address, io_vec)
          header = new

          header.address = address
          header.message = io_vec

          header
        end

        def address=(address)
          self[:msg_name]    = address.pointer
          self[:msg_namelen] = address.pointer.total
        end

        def message=(vec)
          self[:msg_iov]    = vec.pointer
          self[:msg_iovlen] = 1
        end

        def address_size
          self[:msg_namelen]
        end

        def flags
          self[:msg_flags]
        end

        def message_truncated?
          flags & ::Socket::MSG_TRUNC > 0
        end
      end
    end
  end
end
