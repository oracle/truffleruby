# frozen_string_literal: true

# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module DirOperations
    # We don't do this using FFI structs because directory functionality is needed before them.
    DIRENT_SIZE = Truffle::Config['platform.dirent.sizeof']
    DIRENT_NAME_SIZE = Truffle::Config['platform.dirent.d_name.size']
    DIRENT_NAME_OFFSET = Truffle::Config['platform.dirent.d_name.offset']
    DIRENT_TYPE_OFFSET = Truffle::Config['platform.dirent.d_type.offset']

    AT_SYMLINK_NOFOLLOW = Truffle::Config['platform.file.AT_SYMLINK_NOFOLLOW']
    DT_DIR = Truffle::Config['platform.file.DT_DIR']
    DT_UNKNOWN  = Truffle::Config['platform.file.DT_UNKNOWN']
    BUFFER_SIZE = DIRENT_SIZE + Truffle::FFI::Pointer::SIZE

    def self.readdir(dir)
      dir.__send__(:ensure_open)
      dirptr = dir.instance_variable_get(:@ptr)
      buffer = Primitive.io_thread_buffer_allocate(BUFFER_SIZE)
      begin
        dirent = buffer + Truffle::FFI::Pointer::SIZE
        result = Truffle::POSIX.readdir_r(dirptr, dirent, buffer);
        raise Errno::EBADF unless result == 0;
        if !buffer.read_pointer.null?
          str = dirent.get_string(DIRENT_NAME_OFFSET, DIRENT_NAME_SIZE)
          type = (dirent + DIRENT_TYPE_OFFSET).read_uchar
          [str, type]
        else
          nil
        end
      ensure
        Primitive.io_thread_buffer_free(buffer)
      end
    end
  end
end
