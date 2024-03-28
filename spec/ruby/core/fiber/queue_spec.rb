require_relative '../../spec_helper'
require_relative "fixtures/classes"

describe "A non blocking fiber with a scheduler" do
  it "calls `block` on the scheduler when attempting to pop from an empty queue" do
    queue = Queue.new
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
end
