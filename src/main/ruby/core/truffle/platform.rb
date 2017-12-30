# Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle

  # Used by Truffle::FFI
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

  # jnr-posix hard codes this value
  PATH_MAX = 1024

end
