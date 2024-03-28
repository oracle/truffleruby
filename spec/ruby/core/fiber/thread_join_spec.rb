require_relative '../../spec_helper'
require_relative "fixtures/classes"

describe "A non blocking fiber with a scheduler" do
  it "calls block on the scheduler when `Thread#join` is called with no limit" do
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler (scheduler)

      Fiber.schedule do
        Thread.new do
          sleep 1
        end.join
      end
    end.join
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

  it "calls block on the scheduler when `Thread#join` is called with a limit" do
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler (scheduler)

      Fiber.schedule do
        Thread.new do
          sleep 1
        end.join(3)
      end
    end.join
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

  it "calls block on the scheduler when `Thread#join` is called with a limit that expires before the thread ends" do
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler (scheduler)

      Fiber.schedule do
        t = Thread.new do
          sleep 5
        end
        t.join(1)
        t.join
      end
    end.join
    scheduler.block_calls.should >= 2
    scheduler.unblock_calls.should < scheduler.block_calls
  end

  it "calls block on the scheduler when `Thread#join` is called with a limit that may expire before the thread ends" do
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler (scheduler)

      100.times do |n|
        Fiber.schedule do
          t = Thread.new do
            Thread.current.name = "Thread #{n}."
            sleep 3
          end
          t.join(3)
          t.join
        end
      end
    end.join
    scheduler.block_calls.should >= 100
    scheduler.unblock_calls.should <= scheduler.block_calls
  end
end
