module RubySL
  module Socket
    module Foreign
      class Iovec < Rubinius::FFI::Struct
        config('rbx.platform.iovec', :iov_base, :iov_len)

        def self.with_buffer(buffer)
          vec = new

          vec[:iov_base] = buffer
          vec[:iov_len]  = buffer.total

          vec
        end
      end
    end
  end
end
