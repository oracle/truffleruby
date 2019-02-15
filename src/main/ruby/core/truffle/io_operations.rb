# frozen_string_literal: true

# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module IOOperations
    def self.last_line(a_binding)
      Truffle::KernelOperations.frame_local_variable_get(:$_, a_binding)
    end

    def self.set_last_line(value, a_binding)
      Truffle::KernelOperations.frame_local_variable_set(:$_, a_binding, value)
    end

    def self.print(io, args, last_line_binding)
      if args.empty?
        raise 'last_line_binding is required' if last_line_binding.nil?
        io.write Truffle::IOOperations.last_line(last_line_binding).to_s
      else
        args.each { |o| io.write o.to_s }
      end

      io.write $\.to_s
      nil
    end

    def self.puts(io, *args)
      if args.empty?
        io.write DEFAULT_RECORD_SEPARATOR
      else
        args.each do |arg|
          if arg.equal? nil
            str = ''
          elsif arg.kind_of?(String)
            str = arg
          elsif Thread.guarding? arg
            str = '[...]'
          elsif (ary = Truffle::Type.coerce_to_or_nil(arg, Array, :to_ary))
            Thread.recursion_guard arg do
              ary.each { |a| puts(io, a) }
            end
            next
          else
            str = arg.to_s
            str = Truffle::Type.rb_any_to_s(arg) unless Truffle::Type.object_kind_of?(str, String)
          end

          if str
            # Truffle: write the string + record separator (\n) atomically so multithreaded #puts is bearable
            unless str.end_with?(DEFAULT_RECORD_SEPARATOR)
              str += DEFAULT_RECORD_SEPARATOR
            end
            io.write str
          end
        end
      end

      nil
    end

    def self.dup2_with_cloexec(old_fd, new_fd)
      if new_fd < 3
        # STDIO should not be made close-on-exec. `dup2` clears the close-on-exec bit for the destination FD.
        r = Truffle::POSIX.dup2(old_fd, new_fd)
        Errno.handle if r == -1

      elsif Truffle::POSIX.respond_to?(:dup3)
        # Atomically dupe and set close-on-exec if supported by the platform.
        r = Truffle::POSIX.dup3(old_fd, new_fd, Fcntl::O_CLOEXEC)
        Errno.handle if r == -1

      else
        # Dupe and set close-on-exec in two operations if it can't be done atomically.
        r = Truffle::POSIX.dup2(old_fd, new_fd)
        Errno.handle if r == -1

        flags = Truffle::POSIX.fcntl(new_fd, Fcntl::F_GETFD, 0)
        Errno.handle if flags < 0

        if (flags & Fcntl::FD_CLOEXEC) == 0
          Truffle::POSIX.fcntl(new_fd, Fcntl::F_SETFD, flags | Fcntl::FD_CLOEXEC)
        end
      end
    end

    Truffle::Graal.always_split(method(:last_line))
    Truffle::Graal.always_split(method(:set_last_line))
  end
end
