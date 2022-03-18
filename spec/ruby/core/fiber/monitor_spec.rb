require_relative '../../spec_helper'
require_relative "fixtures/classes"
require 'monitor'

describe "A non blocking fiber with a scheduler" do
  it "calls `block` on the scheduler when attempting to lock a locked monitor and unblock when the monitor is available" do
    monitor = Monitor.new
    locked_by_fiber = false
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      monitor.mon_enter
      Fiber.schedule do
        monitor.mon_enter
        locked_by_fiber = true
        monitor.mon_exit
      end
      monitor.mon_exit
    end.join

    monitor.mon_locked?.should == false
    locked_by_fiber.should == true
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

  it "calls `block` on the scheduler when attempting to synchronize on a locked monitor and unblock when the monitor is available" do
    monitor = Monitor.new
    locked_by_fiber = false
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      monitor.mon_enter
      Fiber.schedule do
        monitor.synchronize do
          locked_by_fiber = true
        end
      end
      monitor.mon_exit
    end.join

    monitor.mon_locked?.should == false
    locked_by_fiber.should == true
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

  it "does not call block when synchronizing on a monitor that is already owned by the fiber" do
    monitor = Monitor.new
    locked_by_fiber = false
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      monitor.mon_enter
      Fiber.schedule do
        monitor.synchronize do
          monitor.synchronize do
            locked_by_fiber = true
          end
        end
      end
      monitor.mon_exit
    end.join

    monitor.mon_locked?.should == false
    locked_by_fiber.should == true
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

end
