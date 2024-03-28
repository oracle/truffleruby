require_relative '../../spec_helper'
require_relative "fixtures/classes"

describe "Fiber.scheduler" do
  it "returns nil on the root fiber" do
    Fiber.scheduler.should == nil
  end

  it "returns the scheduler for the current thread, even when set on another fiber" do
    scheduler = FiberSpecs::EmptyScheduler.new

    fiber = Fiber.new { Fiber.scheduler.should == scheduler }
    Fiber.scheduler.should == nil
    Fiber.set_scheduler(scheduler)
    Fiber.scheduler.should == scheduler
    fiber.resume
    Fiber.set_scheduler(nil)
  end

  it "executes the fiber method on the same fiber as the caller of `Fiber::schedule" do
    thread = Thread.current
    root_fiber = Fiber.current
    scheduler = Class.new (FiberSpecs::EmptyScheduler) do
      define_method(:fiber) do |&block|
        Thread.current.should == thread
        Fiber.current.should == root_fiber
        fiber = Fiber.new(blocking: false, &block)

        fiber.resume

        return fiber
      end
    end.new

    Fiber.set_scheduler(scheduler)
    Fiber.schedule { }
    Fiber.set_scheduler(nil)
  end

  it "executes the close method on the root fiber when the thread exits" do
    thread = nil
    lock = Mutex.new
    root_fiber = nil
    scheduler = Class.new(FiberSpecs::EmptyScheduler) do
      define_method(:close) do
        Thread.current.should == thread
        Fiber.current.should == root_fiber
      end
    end.new

    lock.lock
    thread = Thread.new do
      lock.lock
      root_fiber = Fiber.current
      lock.unlock
      Fiber.set_scheduler scheduler
    end

    lock.unlock
    thread.join
  end

  it "executes the close method on the main thread when that thread exits" do
    scheduler_fixtures = fixture(__FILE__, "classes.rb")
    cmd = <<~RUBY
    require #{scheduler_fixtures.inspect}
    scheduler = Class.new(FiberSpecs::EmptyScheduler) { def close; puts 'Closing!'; end }.new
    Fiber.set_scheduler(scheduler)
    RUBY
    ruby_exe(cmd).should == "Closing!\n"
  end

  it "has its `kerenl_sleep` method called when a `sleep` is called in a non-blocking fiber" do
    sleep_count = 0
    scheduler = Class.new(FiberSpecs::EmptyScheduler) do

      def fiber(&block)
        fiber = Fiber.new(blocking: false, &block)

        fiber.resume

        return fiber
      end

      define_method(:kernel_sleep) do |duration|
        sleep_count = sleep_count + 1

        Fiber.yield

        return true
      end
    end.new

    thread = Thread.new do
      Fiber.set_scheduler scheduler
      Fiber.schedule { sleep 2 }
    end.join

    sleep_count.should == 1
  end

  it "executes the close method before removing the scheduler on the thread" do
    thread = nil
    root_fiber = nil
    sleep_count = 0
    scheduler = Class.new(FiberSpecs::EmptyScheduler) do

      define_method(:close) do
        sleep 2
      end

      define_method(:kernel_sleep) do |duration|
        sleep_count = sleep_count + 1

        Fiber.yield

        return true
      end
    end.new

    thread = Thread.new do
      root_fiber = Fiber.current
      Fiber.set_scheduler scheduler
      Fiber.schedule { Fiber.set_scheduler nil }
    end

    thread.join
    sleep_count.should == 1
  end

  it "calls hooks from within hooks" do
    thread = nil
    root_fiber = nil
    sleep_count = 0
    scheduler = Class.new(FiberSpecs::EmptyScheduler) do

      define_method(:kernel_sleep) do |duration|
        sleep_count = sleep_count + 1

        sleep(duration) if sleep_count < 2

        Fiber.yield

        return true
      end
    end.new

    Thread.new do
      root_fiber = Fiber.current
      Fiber.set_scheduler scheduler
      Fiber.schedule { sleep (2) }
    end.join

    sleep_count.should == 2
  end

  it "throwing an exception from close will stop the scheduler being removed" do
    thread = nil
    root_fiber = nil
    scheduler = Class.new(FiberSpecs::EmptyScheduler) do

      def self.close
        raise RuntimeError, "Eeek!" unless Fiber.blocking?
      end
    end.new

    Thread.new do
      root_fiber = Fiber.current
      Fiber.set_scheduler scheduler
      Fiber.schedule do
        begin
          Fiber.set_scheduler nil;
        ensure
          Fiber.scheduler.should == scheduler
        end
      rescue
      end
    end.join
  end

end
