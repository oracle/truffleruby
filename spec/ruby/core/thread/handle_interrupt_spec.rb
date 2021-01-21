require_relative '../../spec_helper'

describe "Thread.handle_interrupt" do
  def make_handle_interrupt_thread(interrupt_config, blocking = true)
    interrupt_class = Class.new(RuntimeError)

    ScratchPad.record []

    in_handle_interrupt = Queue.new
    can_continue = Queue.new

    thread = Thread.new do
      begin
        Thread.handle_interrupt(interrupt_config) do
          begin
            in_handle_interrupt << true
            if blocking
              can_continue.pop
            else
              begin
                can_continue.pop(true)
              rescue ThreadError
                Thread.pass
                retry
              end
            end
          rescue interrupt_class
            ScratchPad << :interrupted
          end
        end
      rescue interrupt_class
        ScratchPad << :deferred
      end
    end

    in_handle_interrupt.pop
    thread.raise interrupt_class, "interrupt"
    can_continue << true
    thread.join

    ScratchPad.recorded
  end

  before :each do
    Thread.pending_interrupt?.should == false # sanity check
  end

  it "with :never defers interrupts until exiting the handle_interrupt block" do
    make_handle_interrupt_thread(RuntimeError => :never).should == [:deferred]
  end

  it "with :on_blocking defers interrupts until the next blocking call" do
    make_handle_interrupt_thread(RuntimeError => :on_blocking).should == [:interrupted]
    make_handle_interrupt_thread({ RuntimeError => :on_blocking }, false).should == [:deferred]
  end

  it "with :immediate handles interrupts immediately" do
    make_handle_interrupt_thread(RuntimeError => :immediate).should == [:interrupted]
  end

  it "with :immediate immediately runs pending interrupts" do
    Thread.handle_interrupt(RuntimeError => :never) do
      current = Thread.current
      Thread.new {
        current.raise "interrupt"
      }.join

      -> {
        Thread.handle_interrupt(RuntimeError => :immediate) {
          flunk "not reached"
        }
      }.should raise_error(RuntimeError, "interrupt")
    end
  end

  it "supports multiple pairs in the Hash" do
    make_handle_interrupt_thread(ArgumentError => :never, RuntimeError => :never).should == [:deferred]
  end
end
