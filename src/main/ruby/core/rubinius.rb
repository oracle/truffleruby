# Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Copyright (c) 2007-2015, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Only part of Rubinius' rubinius.rb

module Rubinius

  # Used by Rubinius::FFI
  L64 = true
  CPU = 'jvm'
  SIZEOF_LONG = 8 # bytes
  WORDSIZE = 64   # bits

  HOST_OS = Truffle::System.host_os

  IS_LINUX = HOST_OS == 'linux'
  IS_SOLARIS = HOST_OS == 'solaris'
  IS_DARWIN = HOST_OS == 'darwin'
  IS_BSD = HOST_OS == 'freebsd' || HOST_OS == 'netbsd' || HOST_OS == 'openbsd'
  IS_WINDOWS = HOST_OS == 'mswin32'

  def self.linux?
    IS_LINUX
  end

  def self.darwin?
    IS_DARWIN
  end

  def self.solaris?
    IS_SOLARIS
  end

  def self.bsd?
    IS_BSD
  end

  def self.windows?
    IS_WINDOWS
  end

  def self.mathn_loaded?
    false
  end

  module FFI
    class DynamicLibrary
    end
  end

  # jnr-posix hard codes this value
  PATH_MAX = 1024

  module Unsafe
    def self.set_class(obj, cls)
      Truffle.primitive :vm_set_class

      if obj.kind_of? ImmediateValue
        raise TypeError, 'Can not change the class of an immediate'
      end

      raise ArgumentError, "Class #{cls} is not compatible with #{obj.inspect}"
    end
  end

  def self.synchronize(object, &block)
    Truffle::System.synchronized(object, &block)
  end
end

class PrimitiveFailure < Exception # rubocop:disable Lint/InheritException
end
