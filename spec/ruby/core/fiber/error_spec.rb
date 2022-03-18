require_relative '../../spec_helper'
require_relative "fixtures/classes"

describe "A fiber scheduler" do
  it "raises a ThreadError if a dead fiber is resumed by the scheduler" do
    mutex = Mutex.new
    scheduler = Class.new(FiberSpecs::BlockUnblockScheduler) do
      def resume_execution(fiber)
        fiber.resume
        fiber.resume
      end
    end.new
    -> {
      Thread.new do
        Thread.current.report_on_exception = false
        Fiber.set_scheduler scheduler

        mutex.lock
        Fiber.schedule do
          mutex.lock
          mutex.unlock
        end
        mutex.unlock
      end.join
    }.should raise_error(FiberError)
  end

  ruby_bug "", ""..."3.3" do
    it "errors raised in unblock are raised as normal" do
      mutex = Mutex.new
      scheduler = Class.new(FiberSpecs::EmptyScheduler) do
        def unblock(blocker, fiber)
          raise RuntimeError, "Evil"
        end
      end.new
      -> {
        Thread.new do
          Fiber.set_scheduler scheduler

          mutex.lock
          Fiber.schedule do
            mutex.lock
            mutex.unlock
          end
          Fiber.schedule do
            mutex.lock
            mutex.unlock
          end
          mutex.unlock
        end.join
      }.should raise_error(RuntimeError, "Evil")
    end
  end
end
