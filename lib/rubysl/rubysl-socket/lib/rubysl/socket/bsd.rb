# This file is only available on BSD/Darwin systems.

module RubySL
  module Socket
    module Foreign
      attach_function :_getpeereid,
        :getpeereid, [:int, :pointer, :pointer], :int

      def self.getpeereid(descriptor)
        euid = Foreign.memory_pointer(:int)
        egid = Foreign.memory_pointer(:int)

        begin
          res = _getpeereid(descriptor, euid, egid)

          if res == 0
            [euid.read_int, egid.read_int]
          else
            Errno.handle('getpeereid(3)')
          end
        ensure
          euid.free
          egid.free
        end
      end
    end
  end
end
