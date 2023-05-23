# frozen_string_literal: true

# Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module IOOperations

    POLLIN = Truffle::Config['platform.poll.POLLIN']
    POLLPRI = Truffle::Config['platform.poll.POLLPRI']
    POLLOUT = Truffle::Config['platform.poll.POLLOUT']

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
          if Primitive.nil?(arg)
            str = ''
          elsif Primitive.is_a?(arg, String)
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
            str = Primitive.rb_any_to_s(arg) unless Primitive.is_a?(str, String)
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

    def self.write_transcoding(string, external_encoding)
      if external_encoding && external_encoding != string.encoding && external_encoding != Encoding::BINARY &&
          !(string.ascii_only? && external_encoding.ascii_compatible?)
        string.encode(external_encoding)
      else
        string
      end
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

    CLASS_NEW = Class.instance_method(:new)

    def self.create_pipe(read_class, write_class, external = nil, internal = nil)
      fds = Truffle::FFI::MemoryPointer.new(:int, 2) do |ptr|
        res = Truffle::POSIX.pipe(ptr)
        Errno.handle if res == -1
        ptr.read_array_of_int(2)
      end

      lhs = pipe_end_setup(CLASS_NEW.bind_call(read_class, fds[0], IO::RDONLY))
      rhs = pipe_end_setup(CLASS_NEW.bind_call(write_class, fds[1], IO::WRONLY))

      lhs.set_encoding external || Encoding.default_external,
                       internal || Encoding.default_internal

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
        begin
          to_fds(readable_ios, readables_pointer)
          to_fds(writable_ios, writables_pointer)
          to_fds(errorable_ios, errorables_pointer)

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

    # This method will return an event mask if poll returned without error.
    # The event mask is > 0 when an event occurred within the timeout, 0 if the timeout expired.
    # Raises an exception for an errno.
    def self.poll(io, event_mask, timeout)
      if (event_mask & POLLIN) != 0
        return 1 unless io.__send__(:buffer_empty?)
      end

      if timeout
        raise TypeError, 'Timeout must be numeric' unless Primitive.is_a? timeout, Numeric
        raise ArgumentError, 'timeout must be positive' if timeout < 0

        # Milliseconds, rounded down
        timeout_ms = Primitive.rb_to_int((timeout * 1_000).to_i)
        return poll_milliseconds(io, event_mask, timeout_ms)
      end

      begin
        remaining_timeout = -1
        primitive_result = Truffle::POSIX.truffleposix_poll(Primitive.io_fd(io), event_mask, remaining_timeout)
        result =
          if primitive_result < 0
            errno = Errno.errno
            if errno == Errno::EINTR::Errno
              :retry
            else
              Errno.handle_errno(errno)
            end
          else
            primitive_result
          end
      end while result == :retry

      result
    end

    #
    # Polling on an IO even with a (mandatory) provided millisecond timeout.
    #
    def self.poll_milliseconds(io, event_mask, timeout_milliseconds)
      if (event_mask & POLLIN) != 0
        return 1 unless io.__send__(:buffer_empty?)
      end

      raise TypeError, 'Timeout must be integer' unless Primitive.is_a? timeout_milliseconds, Integer
      raise ArgumentError, 'timeout must be positive' if timeout_milliseconds < 0

      while timeout_milliseconds > 2147483000 # INT_MAX rounded (down) to seconds.
        timeout_milliseconds -= 2147483000
        ret = poll_milliseconds(io, event_mask, 2147483)
        return ret unless ret == 0
      end

      remaining_timeout = timeout_milliseconds
      start = Process.clock_gettime(Process::CLOCK_MONOTONIC, :millisecond)
      deadline = start + timeout_milliseconds

      begin
        primitive_result = Truffle::POSIX.truffleposix_poll(Primitive.io_fd(io), event_mask, remaining_timeout)
        result =
          if primitive_result < 0
            errno = Errno.errno
            if errno == Errno::EINTR::Errno
              # Update timeout
              now = Process.clock_gettime(Process::CLOCK_MONOTONIC, :millisecond)
              if now >= deadline
                0 # timeout
              else
                remaining_timeout = deadline - now
                :retry
              end
            else
              Errno.handle_errno(errno)
            end
          else
            primitive_result
          end
      end while result == :retry

      result
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
      return mode if Primitive.is_a? mode, Integer

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

    BOM = /\Abom\|/i

    def self.parse_external_enc(io, external)
      if BOM.match?(external)
        external = external[4..-1]
        io.__send__(:strip_bom) || Encoding.find(external)
      else
        Encoding.find(external)
      end
    end

    # MRI: parse_mode_enc
    # Parses string as "external" or "external:internal" or "external:-"
    def self.parse_mode_enc(io, mode, string)
      external, internal = string.split(':', 2)
      external = parse_external_enc(io, external)

      if internal
        if internal == '-' # Special case - "-" => no transcoding
          internal = nil
        else
          internal = Encoding.find(internal)
        end
        if mode.nobits?(FMODE_SETENC_BY_BOM) && internal == external
          internal = nil
        end
      end

      rb_io_ext_int_to_encs(mode, external, internal)
    end

    # MRI: rb_io_ext_int_to_encs
    def self.rb_io_ext_int_to_encs(mode, external, internal)
      default_ext = false
      if Primitive.nil?(external)
        external = Encoding.default_external
        default_ext = true
      end

      if external == Encoding::BINARY
        # If external is BINARY, no transcoding
        internal = nil
      elsif Primitive.nil?(internal)
        internal = Encoding.default_internal
      end

      if Primitive.nil?(internal) or
          (mode.nobits?(FMODE_SETENC_BY_BOM) && internal == external)
        # No internal encoding => use external + no transcoding
        external = (default_ext && internal != external) ? nil : external
        internal = nil
      end

      [external, internal]
    end

    def self.normalize_options(mode, perm, options, default_mode = nil)
      autoclose = true

      if mode
        mode = (Truffle::Type.try_convert(mode, Integer, :to_int) or
          Truffle::Type.coerce_to(mode, String, :to_str))
      end

      if options
        if optmode = options[:mode]
          optmode = (Truffle::Type.try_convert(optmode, Integer, :to_int) or
            Truffle::Type.coerce_to(optmode, String, :to_str))
        end

        if mode && optmode
          raise ArgumentError, 'mode specified twice'
        end

        mode ||= optmode
        mode ||= default_mode

        if flags = options[:flags]
          flags = Truffle::Type.rb_convert_type(flags, Integer, :to_int)

          if Primitive.nil?(mode)
            mode = flags
          elsif Primitive.is_a?(mode, Integer)
            mode |= flags
          else # it's a String
            mode = Truffle::IOOperations.parse_mode(mode)
            mode |= flags
          end
        end

        if optperm = options[:perm]
          optperm = Truffle::Type.try_convert(optperm, Integer, :to_int)
        end

        if perm
          raise ArgumentError, 'perm specified twice' if optperm
        else
          perm = optperm
        end

        autoclose = Primitive.as_boolean(options[:autoclose]) if options.key?(:autoclose)
      end

      mode ||= default_mode

      if Primitive.is_a?(mode, String)
        mode, external, internal = mode.split(':', 3)
        raise ArgumentError, 'invalid access mode' unless mode

        binary = true  if mode.include?(?b)
        binary = false if mode.include?(?t)
      elsif mode
        binary = true  if (mode & BINARY) != 0
      end

      if options
        if options[:textmode] and options[:binmode]
          raise ArgumentError, 'both textmode and binmode specified'
        end

        if Primitive.nil? binary
          binary = options[:binmode]
        elsif options.key?(:textmode) or options.key?(:binmode)
          raise ArgumentError, 'text/binary mode specified twice'
        end

        if !external and !internal
          external = options[:external_encoding]
          internal = options[:internal_encoding]
        elsif options[:external_encoding] or options[:internal_encoding] or options[:encoding]
          raise ArgumentError, 'encoding specified twice'
        end

        if !external and !internal
          encoding = options[:encoding]

          if Primitive.is_a?(encoding, Encoding)
            external = encoding
          elsif !Primitive.nil?(encoding)
            encoding = StringValue(encoding)
            external, internal = encoding.split(':', 2)
          end
        end
      end
      external = Encoding::BINARY if binary and !external and !internal
      perm ||= 0666
      [mode, binary, external, internal, autoclose, perm]
    end
  end
end
