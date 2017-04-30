module RubySL
  module Socket
    module Foreign
      class Addrinfo < Rubinius::FFI::Struct
        config("rbx.platform.addrinfo", :ai_flags, :ai_family, :ai_socktype,
               :ai_protocol, :ai_addrlen, :ai_addr, :ai_canonname, :ai_next)
      end
    end
  end
end
