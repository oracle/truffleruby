# truffleruby_primitives: true

# Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class IO
  def nread
    # TODO CS 14-Apr-18 provide a proper implementation
    0
  end

  def ready?
    ensure_open_and_readable
    Truffle::IOOperations.poll(self, IO::READABLE, 0) > 0
  end

  def wait_readable(timeout = nil)
    ensure_open_and_readable
    Truffle::IOOperations.poll(self, IO::READABLE, timeout) > 0 ? self : nil
  end

  def wait_writable(timeout = nil)
    ensure_open_and_writable
    Truffle::IOOperations.poll(self, IO::WRITABLE, timeout) > 0 ? self : nil
  end

  def wait_priority(timeout = nil)
    ensure_open_and_readable
    Truffle::IOOperations.poll(self, IO::PRIORITY, timeout) > 0 ? self : nil
  end


  # call-seq:
  #   io.wait(events, timeout) -> event mask, false or nil
  #   io.wait(timeout = nil, mode = :read) -> self, true, or false
  #
  # Waits until the IO becomes ready for the specified events and returns the
  # subset of events that become ready, or a falsy value when times out.
  #
  # The events can be a bit mask of +IO::READABLE+, +IO::WRITABLE+ or
  # +IO::PRIORITY+.
  #
  # Returns a truthy value immediately when buffered data is available.
  #
  # Optional parameter +mode+ is one of +:read+, +:write+, or
  # +:read_write+.
  def wait(*args)
    ensure_open

    if args.size != 2 || Primitive.object_kind_of?(args[0], Symbol) || Primitive.object_kind_of?(args[1], Symbol)
      # Slow/messy path:
      timeout = :undef
      events = 0
      args.each do |arg|
        if Primitive.object_kind_of?(arg, Symbol)
          events |= case arg
                    when :r, :read, :readable then IO::READABLE
                    when :w, :write, :writable then IO::WRITABLE
                    when :rw, :read_write, :readable_writable then IO::READABLE | IO::WRITABLE
                    else
                      raise ArgumentError, "unsupported mode: #{mode.inspect}"
                    end

        elsif timeout == :undef
          timeout = arg
        else
          raise ArgumentError, 'timeout given more than once'
        end
      end

      timeout = nil if timeout == :undef

      events = IO::READABLE if events == 0

      res = Truffle::IOOperations.poll(self, events, timeout)
      res == 0 ? nil : self
    else
      # args.size == 2 and neither are symbols
      # This is the fast path and the new interface:
      events, timeout = *args
      raise ArgumentError, 'Events must be positive integer!' if events <= 0
      res = Truffle::IOOperations.poll(self, events, timeout)
      # return events as bit mask
      res == 0 ? nil : res
    end
  end
end
