require_relative '../../spec_helper'
require_relative 'fixtures/classes'

require 'io/nonblock'

require "fiber"

describe "A non blockign fiber with a scheduler" do
  it "calls io_wait on the scheduler when attempting to read from a non-blocking IO that has no data" do
    s1, s2 = IO.pipe
    s1.nonblock?.should be_true
    s2.nonblock?.should be_true
    message = ''

    scheduler = FiberSpecs::IOScheduler.new

    Thread.new do
      Fiber.set_scheduler scheduler
      Fiber.schedule do
        message = s1.read(20)
        s1.close
      end

      Fiber.schedule do
        s2.write('Hello world')
        s2.close
      end
    end.join

    message.should == 'Hello world'
    scheduler.block_calls.should == 1
  end

  it "calls io_wait on the scheduler when attempting to write to a non-blocking IO that is has no capacity" do
    s1, s2 = UNIXSocket.pair
    s1.nonblock?.should be_true
    s2.nonblock?.should be_true
    message = ''

    scheduler = FiberSpecs::IOScheduler.new

    begin
      while true
        s2.write_nonblock("Hello world")
      end
    rescue IO::WaitWritable
      # We're now full so can proceed with the test.
    end

    begin
      Thread.new do
        Fiber.set_scheduler scheduler

        Fiber.schedule do
          s2.write('Hello world')
        end

        Fiber.schedule do
          message = s1.read(11)
          begin
            while true
              s1.read_nonblock(11)
            end
          rescue IO::WaitReadable
            # We're done now
          end
        end
      end.join
    ensure
      s1.close
      s2.close
    end

    message.should == 'Hello world'
    scheduler.block_calls.should == 1
  end
end
