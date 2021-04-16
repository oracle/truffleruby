# frozen_string_literal: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module FileOperations

    # Pull a constant for Dir local to File so that we don't have to depend
    # on the global Dir constant working. This sounds silly, I know, but it's a
    # little bit of defensive coding so Rubinius can run things like fakefs better.
    PrivateDir = ::Dir

    def self.expand_path(path, dir, expand_tilde)
      path = Truffle::Type.coerce_to_path(path)
      str = ''.encode path.encoding
      first = path[0]
      if first == ?~ && expand_tilde
        first_char = path[1]

        if first_char == ?/ || Primitive.nil?(first_char)
          home = ENV['HOME']
          raise ArgumentError, "couldn't find HOME environment variable when expanding '~'" if Primitive.nil? home
          raise ArgumentError, 'non-absolute home' unless home.start_with?('/')
        end

        case first_char
        when ?/
          path = home + path.byteslice(1, path.bytesize - 1)
        when nil
          if home.empty?
            raise ArgumentError, "HOME environment variable is empty expanding '~'"
          end

          return home.dup
        else
          length = Primitive.find_string(path, '/', 1) || path.bytesize
          name = path.byteslice 1, length - 1

          ptr = Truffle::POSIX.truffleposix_get_user_home(name)
          if !ptr.null?
            dir = ptr.read_string
            ptr.free
            raise ArgumentError, "user #{name} does not exist" if dir.empty?
          else
            Errno.handle
          end

          path = dir + path.byteslice(length, path.bytesize - length)
        end
      elsif first != ?/
        if dir
          dir = expand_path(dir, nil, expand_tilde)
        else
          dir = PrivateDir.pwd
        end

        path = "#{dir}/#{path}"
      end

      items = []
      start = 0
      bytesize = path.bytesize

      while (index = Primitive.find_string(path, '/', start)) || ((start < bytesize) && (index = bytesize))
        length = index - start

        if length > 0
          item = path.byteslice start, length

          if item == '..'
            items.pop
          elsif item != '.'
            items << item
          end
        end

        start = index + 1
      end

      if items.empty?
        str << '/'
      else
        items.each { |x| Primitive.string_append(str, "/#{x}") }
      end

      str
    end

    def self.exist?(path)
      Truffle::POSIX.truffleposix_stat_mode(path) > 0
    end
  end
end
