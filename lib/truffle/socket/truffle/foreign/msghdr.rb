# Copyright (c) 2013, Brian Shirai
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
#    list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. Neither the name of the library nor the names of its contributors may be
#    used to endorse or promote products derived from this software without
#    specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
# OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

module Truffle
  module Socket
    module Foreign
      class Msghdr < ::FFI::Struct
        Truffle::Socket.config(self, 'platform.msghdr', :msg_name, :msg_namelen, :msg_iov,
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
