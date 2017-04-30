module RubySL
  module Socket
    module Foreign
      class Sockaddr < Rubinius::FFI::Struct
        config("rbx.platform.sockaddr", :sa_data, :sa_family)

        def data
          self[:sa_data]
        end

        def family
          self[:sa_family]
        end

        def to_s
          pointer.read_string(self.class.size)
        end
      end
    end
  end
end
