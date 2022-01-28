# frozen_string_literal: true

# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
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
          elsif Primitive.object_kind_of?(arg, String)
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
            str = Primitive.rb_any_to_s(arg) unless Primitive.object_kind_of?(str, String)
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

      buffer, readables_pointer, writables_pointer, errorables_pointer =
          Truffle::FFI::Pool.stack_alloc(readables.size * SIZEOF_INT, writables.size * SIZEOF_INT, errorables.size * SIZEOF_INT)
      begin
        to_fds(readable_ios, readables_pointer)
        to_fds(writable_ios, writables_pointer)
        to_fds(errorable_ios, errorables_pointer)

        begin
          primitive_result = Primitive.thread_run_blocking_nfi_system_call(Truffle::POSIX::SELECT, [
              readables.size, readables_pointer,
              writables.size, writables_pointer,
              errorables.size, errorables_pointer,
              remaining_timeout
          ])

          result =
            if primitive_result < 0
              errno = Errno.errno
              if errno == Errno::EINTR::Errno
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
                Errno.handle_errno(errno)
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
        Truffle::FFI::Pool.stack_free(buffer)
      end

    end

    # The constants used to express a mode for the opening of files are
    # different to the fmode constants used to express the mode of an
    # opened file used by C extensions. Thus we will need to translate
    # from the o_mode to the fmode and vice versa.
    def self.translate_omode_to_fmode(o_mode)
      fmode = 0
      if (o_mode & WRONLY != 0)
        fmode |= FMODE_WRITABLE
      elsif (o_mode & RDWR != 0)
        fmode |= FMODE_READWRITE
      else
        fmode |= FMODE_READABLE
      end

      if (o_mode & CREAT != 0)
        fmode |= FMODE_CREATE
      end

      if (o_mode & TRUNC != 0)
        fmode |= FMODE_TRUNC
      end

      if (o_mode & APPEND != 0)
        fmode |= FMODE_APPEND
      end

      if (o_mode & BINARY != 0)
        fmode |= FMODE_BINMODE
      end
      fmode
    end

    def self.translate_fmode_to_omode(f_mode)
      omode = 0
      if f_mode & FMODE_READWRITE == FMODE_READWRITE
        omode |= RDWR
      elsif f_mode & FMODE_READABLE != 0
        omode |= RDONLY
      else
        omode |= WRONLY
      end

      if (f_mode & FMODE_CREATE != 0)
        omode |= CREAT
      end

      if (f_mode & FMODE_TRUNC != 0)
        omode |= TRUNC
      end

      if (f_mode & FMODE_APPEND != 0)
        omode |= APPEND
      end

      if (f_mode & FMODE_BINMODE != 0)
        omode |= BINARY
      end
      omode
    end

    def self.parse_mode(mode)
      return mode if Primitive.object_kind_of? mode, Integer

      mode = StringValue(mode)

      ret = CLOEXEC

      case mode[0]
      when ?r
        ret |= RDONLY
      when ?w
        ret |= WRONLY | CREAT | TRUNC
      when ?a
        ret |= WRONLY | CREAT | APPEND
      else
        raise ArgumentError, "invalid mode -- #{mode}"
      end

      return ret if mode.length == 1

      case mode[1]
      when ?+
        ret &= ~(RDONLY | WRONLY)
        ret |= RDWR
      when ?b
        ret |= BINARY
      when ?t
        ret &= ~BINARY
      when ?:
        return ret
      else
        raise ArgumentError, "invalid mode -- #{mode}"
      end

      return ret if mode.length == 2

      case mode[2]
      when ?+
        ret &= ~(RDONLY | WRONLY)
        ret |= RDWR
      when ?b
        ret |= BINARY
      when ?t
        ret &= ~BINARY
      when ?:
        return ret
      else
        raise ArgumentError, "invalid mode -- #{mode}"
      end

      ret
    end

  end
end
