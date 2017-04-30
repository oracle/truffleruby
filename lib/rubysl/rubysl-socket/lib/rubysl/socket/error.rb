module RubySL
  module Socket
    module Error
      def self.write_error(message, socket)
        if nonblocking?(socket)
          write_nonblock(message)
        else
          Errno.handle(message)
        end
      end

      def self.read_error(message, socket)
        if nonblocking?(socket)
          read_nonblock(message)
        else
          Errno.handle(message)
        end
      end

      # Handles an error for a non-blocking read operation.
      def self.read_nonblock(message)
        wrap_read_nonblock { Errno.handle(message) }
      end

      # Handles an error for a non-blocking write operation.
      def self.write_nonblock(message)
        wrap_write_nonblock { Errno.handle(message) }
      end

      def self.wrap_read_nonblock
        yield
      rescue Errno::EAGAIN => err
        raise_wrapped_error(err, ::IO::EAGAINWaitReadable)
      rescue Errno::EWOULDBLOCK => err
        raise_wrapped_error(err, ::IO::EWOULDBLOCKWaitReadable)
      rescue Errno::EINPROGRESS => err
        raise_wrapped_error(err, ::IO::EINPROGRESSWaitReadable)
      end

      def self.wrap_write_nonblock
        yield
      rescue Errno::EAGAIN => err
        raise_wrapped_error(err, ::IO::EAGAINWaitWritable)
      rescue Errno::EWOULDBLOCK => err
        raise_wrapped_error(err, ::IO::EWOULDBLOCKWaitWritable)
      rescue Errno::EINPROGRESS => err
        raise_wrapped_error(err, ::IO::EINPROGRESSWaitWritable)
      end

      # Wraps the error given in `original` in an instance of `error_class`.
      #
      # This can be used to wrap e.g. an Errno::EAGAIN error in an
      # ::IO::EAGAINWaitReadable instance.
      def self.raise_wrapped_error(original, error_class)
        error = error_class.new(original.message)

        error.set_backtrace(original.backtrace)

        raise error
      end

      def self.nonblocking?(socket)
        socket.fcntl(::Fcntl::F_GETFL) & ::Fcntl::O_NONBLOCK > 0
      end
    end
  end
end
