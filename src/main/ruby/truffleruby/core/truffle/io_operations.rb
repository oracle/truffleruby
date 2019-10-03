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
    Truffle::Graal.always_split(method(:last_line))

    def self.set_last_line(value, a_binding)
      Truffle::KernelOperations.frame_local_variable_set(:$_, a_binding, value)
    end
    Truffle::Graal.always_split(method(:set_last_line))

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
          elsif (ary = Truffle::Type.rb_check_convert_type(arg, Array, :to_ary))
            Thread.recursion_guard arg do
              ary.each { |a| puts(io, a) }
            end
            str = nil
          else
            str = arg.to_s
            str = Truffle::Type.rb_any_to_s(arg) unless Truffle::Type.object_kind_of?(str, String)
          end

          if str
            # Truffle: write the string + record separator (\n) atomically so multi-threaded #puts is bearable
            if str.encoding.ascii_compatible?
              unless str.end_with?(DEFAULT_RECORD_SEPARATOR)
                str += DEFAULT_RECORD_SEPARATOR
              end
            else
              rs = DEFAULT_RECORD_SEPARATOR.encode(str.encoding)
              unless str.end_with?(rs)
                str += rs
              end
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
        r = Truffle::POSIX.dup3(old_fd, new_fd, File::CLOEXEC)
        Errno.handle if r == -1

      else
        # Dupe and set close-on-exec in two operations if it can't be done atomically.
        r = Truffle::POSIX.dup2(old_fd, new_fd)
        Errno.handle if r == -1

        flags = Truffle::POSIX.fcntl(new_fd, File::F_GETFD, 0)
        Errno.handle if flags < 0

        if (flags & File::FD_CLOEXEC) == 0
          Truffle::POSIX.fcntl(new_fd, File::F_SETFD, flags | File::FD_CLOEXEC)
        end
      end
    end

    def self.pipe_end_setup(io)
      io.close_on_exec = true
      io.sync = true
      io.instance_variable_set :@pipe, true
      io
    end

    def self.create_pipe(read_class, write_class, external = nil, internal = nil, options = nil)
      fds = Truffle::FFI::MemoryPointer.new(:int, 2) do |ptr|
        res = Truffle::POSIX.pipe(ptr)
        Errno.handle if res == -1
        ptr.read_array_of_int(2)
      end

      lhs = pipe_end_setup(read_class.new(fds[0], IO::RDONLY))
      rhs = pipe_end_setup(write_class.new(fds[1], IO::WRONLY))

      lhs.set_encoding external || Encoding.default_external,
                       internal || Encoding.default_internal, options

      [lhs, rhs]
    end
  end
end
