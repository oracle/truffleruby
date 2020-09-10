# frozen_string_literal: true

# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module IOOperations
    def self.print(io, args, last_line_storage)
      if args.empty?
        raise 'last_line_binding is required' if Primitive.nil? last_line_storage
        io.write Primitive.io_last_line_get(last_line_storage).to_s
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
            # might be a Foreign String we need to convert
            str = arg.to_str
          elsif (ary = Truffle::Type.rb_check_convert_type(arg, Array, :to_ary))
            recursive = Truffle::ThreadOperations.detect_recursion(arg) do
              ary.each { |a| puts(io, a) }
            end

            if recursive
              str = '[...]'
            else
              str = nil
            end
          else
            str = arg.to_s
            str = Truffle::Type.rb_any_to_s(arg) unless Primitive.object_kind_of?(str, String)
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

    SIZEOF_INT = FFI::Pointer.find_type_size(:int)

    def self.to_fds(ios, pointer)
      ios.each_with_index do |io, i|
        pointer.put_int(i * SIZEOF_INT, io.fileno)
      end
    end

    def self.mark_ready(objects, pointer)
      ready = []
      pointer.read_array_of_int(objects.size).each_with_index do |fd, i|
        ready << objects[i] if fd >= 0
      end
      ready
    end

    def self.select(readables, readable_ios, writables, writable_ios, errorables, errorable_ios, timeout, remaining_timeout)
      if timeout
        start = Process.clock_gettime(Process::CLOCK_MONOTONIC, :microsecond)
      end

      readables_pointer, writables_pointer, errorables_pointer =
          Truffle::FFI::Pool.stack_alloc(:int, readables.size, :int, writables.size, :int, errorables.size)

      begin
        to_fds(readable_ios, readables_pointer)
        to_fds(writable_ios, writables_pointer)
        to_fds(errorable_ios, errorables_pointer)

        begin
          primitive_result = Primitive.thread_run_blocking_nfi_system_call -> do
            Truffle::POSIX.truffleposix_select(readables.size, readables_pointer,
                                               writables.size, writables_pointer,
                                               errorables.size, errorables_pointer,
                                               remaining_timeout)
          end

          result = if primitive_result < 0
                     if Errno.errno == Errno::EINTR::Errno
                       if timeout
                         # Update timeout
                         now = Process.clock_gettime(Process::CLOCK_MONOTONIC, :microsecond)
                         waited = now - start
                         if waited >= timeout
                           nil # timeout
                         else
                           remaining_timeout = timeout - waited
                           :retry
                         end
                       else
                         :retry
                       end
                     else
                       Errno.handle
                     end
                   else
                     primitive_result
                   end
        end while result == :retry

        if result == 0
          nil # timeout
        else
          [mark_ready(readables, readables_pointer),
           mark_ready(writables, writables_pointer),
           mark_ready(errorables, errorables_pointer)]
        end
      ensure
        Truffle::FFI::Pool.stack_free(readables_pointer)
      end

    end

  end
end
