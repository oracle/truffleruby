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
    # We don't do this using FFI structs because directory
    # functionality is needed before them and we don't want to
    # duplicate code from the FFI gem.
    DIRENT_NAME_OFFSET = Truffle::Config['platform.dirent.d_name.offset']
    DIRENT_TYPE_OFFSET = Truffle::Config['platform.dirent.d_type.offset']

    AT_SYMLINK_NOFOLLOW = Truffle::Config['platform.file.AT_SYMLINK_NOFOLLOW']
    DT_DIR = Truffle::Config['platform.file.DT_DIR']
    DT_UNKNOWN  = Truffle::Config['platform.file.DT_UNKNOWN']

    def self.readdir(dir)
      dir.__send__(:ensure_open)
      dirptr = Primitive.object_ivar_get(dir, :@ptr)
      dirent = Truffle::POSIX.truffleposix_readdir(dirptr)
      if !dirent.null?
        str = fix_entry_encoding(dir, dirent.get_string(DIRENT_NAME_OFFSET))
        type = (dirent + DIRENT_TYPE_OFFSET).read_uchar
        [str, type]
      else
        Errno.handle unless Errno.errno == 0
        nil
      end
    end

    def self.readdir_name(dir)
      dir.__send__(:ensure_open)
      dirptr = Primitive.object_ivar_get(dir, :@ptr)
      entry = Truffle::POSIX.truffleposix_readdir_name(dirptr)
      Errno.handle unless entry
      return if entry.empty?
      fix_entry_encoding(dir, entry)
    end

    def self.fix_entry_encoding(dir,str)
      if str
        str = str.force_encoding(Primitive.object_ivar_get(dir, :@encoding))

        if Encoding.default_external == Encoding::US_ASCII && !str.valid_encoding?
          str.force_encoding Encoding::ASCII_8BIT
        else
          enc = Encoding.default_internal
          begin
            str = str.encode(enc) if enc
          rescue
            # If the attempt to convert fails we'll return the string as is, just like MRI.
          end
        end
      end
      str
    end
  end
end
