# This file is only available on Linux systems.

module RubySL
  module Socket
    module Foreign
      def self.getpeereid(descriptor)
        data = Foreign
          .getsockopt(descriptor, ::Socket::SOL_SOCKET, ::Socket::SO_PEERCRED)

        _, euid, egid = data.unpack('iii')

        [euid, egid]
      end
    end
  end
end
