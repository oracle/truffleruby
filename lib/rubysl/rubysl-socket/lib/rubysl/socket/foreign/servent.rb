module RubySL
  module Socket
    module Foreign
      class Servent < Rubinius::FFI::Struct
        config('rbx.platform.servent', :s_name, :s_aliases, :s_port, :s_proto)

        def name
          self[:s_name].read_string
        end

        def port
          self[:s_port]
        end
      end
    end
  end
end
