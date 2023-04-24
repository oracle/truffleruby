# truffleruby_primitives: true

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
    module IPv6
      # The IPv6 loopback address as produced by inet_pton(INET6, "::1")
      LOOPBACK = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]

      # The bytes used for an unspecified IPv6 address.
      UNSPECIFIED = [0] * 16

      # The first 10 bytes of an IPv4 compatible IPv6 address.
      COMPAT_PREFIX = [0] * 10

      def self.ipv4_embedded?(bytes)
        ipv4_mapped?(bytes) || ipv4_compatible?(bytes)
      end

      def self.ipv4_mapped?(bytes)
        prefix = bytes.first(10)
        follow = bytes[10..11]

        prefix == COMPAT_PREFIX &&
          follow[0] == 255 &&
          follow[1] == 255 &&
          (bytes[-4] > 0 || bytes[-3] > 0 || bytes[-2] > 0)
      end

      def self.ipv4_compatible?(bytes)
        prefix = bytes.first(10)
        follow = bytes[10..11]

        prefix == COMPAT_PREFIX &&
          follow[0] == 0 &&
          follow[1] == 0 &&
          (bytes[-4] > 0 || bytes[-3] > 0 || bytes[-2] > 0)
      end
    end
  end
end
