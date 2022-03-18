require 'fiber'
require 'set'
require 'socket'

begin
  require 'io/nonblock'
rescue LoadError
  # Ignore.
end

module FiberSpecs

  class EmptyScheduler

    def io_wait(io, events, duration)
      Fiber.yield
      return true
    end

    def kernel_sleep(timeout)
      block(:sleep, timeout)
    end

    def block(blocker, timeout = nil)
      Fiber.yield
    end

    def unblock(blocker, fiber)
    end

    def fiber(&block)
      fiber = Fiber.new(blocking: false, &block)

      fiber.resume

      return fiber
    end
  end

  class BlockUnblockScheduler < EmptyScheduler

    def initialize(&block)
      super
      @ready = Set.new
      @waiting = {}
      @blocking = 0
      @lock = Mutex.new
      @closed = false
      @block_calls = 0
      @unblock_calls = 0
    end

    attr_reader :block_calls
    attr_reader :unblock_calls

    def resume_execution(fiber)
      fiber.resume
    end

    def current_time
      Process.clock_gettime(Process::CLOCK_MONOTONIC)
    end

    def check_not_done
      @lock.synchronize do
        @blocking.positive?
      end
    end

    def find_ready_fibers(ready_set)
      waiting = {}

      @lock.synchronize do
        waiting, @waiting = @waiting, {} unless @waiting.empty?
      end
      time = current_time

      waiting.each do |fiber, timeout|
        if timeout <= time
          @lock.synchronize do
            ready_set << fiber
          end
        else
          @lock.synchronize do
            @waiting[fiber] = timeout
          end
        end
      end
    end

    def run
      while check_not_done
        ready = Set.new
        @lock.synchronize do
          ready = @ready.dup unless @ready.empty?
        end

        ready.each do |fiber|
          resume_execution(fiber)
        end

        find_ready_fibers(@ready)
      end
    end

    def close
      raise "scheduler already closed" if @closed
      self.run
    ensure
      @closed = true
      self.freeze
    end

    def kernel_sleep(timeout)
      block(:sleep, timeout)
    end

    def block(blocker, timeout = nil)
      @block_calls += 1
      perform_with_lock do
        @blocking += 1
      end
      begin
        if timeout
          perform_with_lock do
            @waiting[Fiber.current] = current_time + timeout
          end
          begin
            Fiber.yield
          ensure
            # Remove from @waiting in the case #unblock was called before the timeout expired:
            perform_with_lock do
              @waiting.delete(Fiber.current)
            end
          end
        else
          Fiber.yield
        end
      ensure
        perform_with_lock do
          @blocking -= 1
          @ready.delete(Fiber.current)
        end
      end
    end

    def unblock(blocker, fiber)
      @lock.synchronize do
        @unblock_calls += 1
        @ready << fiber
      end
    end

    def perform_with_lock
      Thread.pass while !@lock.owned? && !@lock.try_lock
      begin
        yield
      ensure
        @lock.unlock
      end
    end
  end

  class IOScheduler < BlockUnblockScheduler

    def initialize(&block)
      super
      @read_waiting = {}
      @write_waiting = {}
    end

    def find_ready_fibers(ready_set)
      super

      read_waiting = {}
      write_waiting = {}

      @lock.synchronize do
        read_waiting, @read_waiting = @read_waiting, {}
        write_waiting, @write_waiting = @write_waiting, {}
      end

      if !read_waiting.empty? or !write_waiting.empty?
        readers = []
        writers = []
        read_waiting.each do |fiber, io|
          readers << io
        end
        write_waiting.each do |fiber, io|
          writers << io
        end
        readable, writable = IO.select(readers, writers, [], 0)

        @lock.synchronize do
          read_waiting.each do |fiber, io|
            if readable && readable.include?(io)
              ready_set << fiber
            else
              @read_waiting[fiber] = io
            end
          end

          write_waiting.each do |fiber, io|
            if writable && writable.include?(io)
              ready_set << fiber
            else
              @write_waiting[fiber] = io
            end
          end
        end
      end
    end

    def io_wait(io, events, duration)

      @read_waiting[Fiber.current] = io if events & IO::READABLE != 0
      @write_waiting[Fiber.current] = io if events & IO::WRITABLE != 0

      block(io, duration)

      return true
    end
  end

  class NewFiberToRaise
    def self.raise(*args)
      fiber = Fiber.new { Fiber.yield }
      fiber.resume
      fiber.raise(*args)
    end
  end

  class CustomError < StandardError; end
end
