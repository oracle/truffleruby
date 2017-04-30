module RubySL
  module Socket
    module Foreign
      class Linger < Rubinius::FFI::Struct
        config('rbx.platform.linger', :l_onoff, :l_linger)

        def self.from_string(string)
          linger = new

          linger.pointer.write_string(string, string.bytesize)

          linger
        end

        def on_off
          self[:l_onoff]
        end

        def linger
          self[:l_linger].to_i
        end

        def on_off=(value)
          if value.is_a?(Integer)
            self[:l_onoff] = value
          else
            self[:l_onoff] = value ? 1 : 0
          end
        end

        def linger=(value)
          self[:l_linger] = value
        end

        def to_s
          pointer.read_string(pointer.total)
        end
      end
    end
  end
end
