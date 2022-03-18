require_relative '../../spec_helper'
require_relative "fixtures/classes"

describe "A non blocking fiber with a scheduler" do
  it "calls `block` on the scheduler when attempting to pop from an empty sized queue" do
    queue = SizedQueue.new(1)
    popped_value = nil
    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      Fiber.schedule do
        popped_value = queue.pop
      end
      queue.push :foo
    end.join

    popped_value.should == :foo
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == 1
  end

  it "calls `block` on the scheduler when attempting to push to full sized queue" do
    queue = SizedQueue.new(1)
    queue.push(:foo)

    popped_value_1 = nil
    popped_value_2 = nil

    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      Fiber.schedule do
        queue.push(:bar)
      end

      Fiber.schedule do
        popped_value_1 = queue.pop
        popped_value_2 = queue.pop
      end
    end.join

    popped_value_1.should == :foo
    popped_value_2.should == :bar
    scheduler.block_calls.should == 2
    scheduler.unblock_calls.should == scheduler.block_calls
  end

  it "calls `unblock` on the scheduler when the queue's capacity is increased" do
    queue = SizedQueue.new(1)
    queue.push(:foo)

    popped_value_1 = nil
    popped_value_2 = nil

    scheduler = FiberSpecs::BlockUnblockScheduler.new
    Thread.new do
      Fiber.set_scheduler scheduler

      Fiber.schedule do
        queue.push(:bar)
      end

      Fiber.schedule do
        queue.max = 2
      end
    end.join

    queue.pop.should == :foo
    queue.pop.should == :bar
    scheduler.block_calls.should == 1
    scheduler.unblock_calls.should == scheduler.block_calls
  end
end
