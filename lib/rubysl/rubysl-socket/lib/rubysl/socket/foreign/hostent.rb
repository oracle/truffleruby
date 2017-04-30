module RubySL
  module Socket
    module Foreign
      class Hostent < Rubinius::FFI::Struct
        config('rbx.platform.hostent', :h_name, :h_aliases, :h_addrtype,
               :h_length, :h_addr_list)

        def hostname
          self[:h_name]
        end

        def type
          self[:h_addrtype]
        end

        def aliases
          return [] unless self[:h_aliases]

          RubySL::Socket::Foreign.pointers_of_type(self[:h_aliases], :string)
            .map(&:read_string)
        end

        def addresses
          return [] unless self[:h_addr_list]

          RubySL::Socket::Foreign.pointers_of_type(self[:h_addr_list], :string)
            .map { |pointer| pointer.read_string(self[:h_length]) }
        end

        def to_s
          pointer.read_string(size)
        end
      end
    end
  end
end
