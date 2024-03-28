require_relative '../../spec_helper'
require_relative "fixtures/classes"

describe "A non blocking fiber with a scheduler" do
  it "calls `block` on the scheduler when attempting to lock a locked mutex and unblock when the mutex is available" do
    mutex = Mutex.new
    locked_by_fiber = false
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      mutex.lock
      Fiber.schedule do
        mutex.lock
        locked_by_fiber = true
        mutex.unlock
      end
      mutex.unlock
    end.join

    mutex.locked?.should == false
    locked_by_fiber.should == true
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

  it "calls `block` on the scheduler when attempting to synchronize on a locked mutex and unblock when the mutex is available" do
    mutex = Mutex.new
    locked_by_fiber = false
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      mutex.lock
      Fiber.schedule do
        mutex.synchronize do
          locked_by_fiber = true
        end
      end
      mutex.unlock
    end.join

    mutex.locked?.should == false
    locked_by_fiber.should == true
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

  ruby_bug "", ""..."3.3" do
    it "raises a ThreadError if the lock is acquired in the block handler" do
      mutex = Mutex.new
      error_count = 0
      scheduler = Class.new(FiberSpecs::BlockUnblockScheduler) do
        define_method(:block) do |*args|
          super(*args)
          while !mutex.try_lock
          end
        end
      end.new
      Thread.new do
        Fiber.set_scheduler scheduler

        mutex.lock
        Fiber.schedule do
          begin
            mutex.lock
            mutex.unlock
          rescue ThreadError
            error_count += 1
          end
        end
        mutex.unlock
      end.join
      error_count.should == 1
    end
  end
end
