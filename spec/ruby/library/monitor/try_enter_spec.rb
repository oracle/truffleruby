require_relative '../../spec_helper'
require 'monitor'

describe "Monitor#try_enter" do
  it "will acquire a monitor not held by another thread" do
    monitor = Monitor.new
    10.times do

      thread = Thread.new do
        begin
          val = monitor.try_enter
        ensure
          monitor.exit if val
        end
        val
      end

      thread.join
      thread.value.should == true
    end
  end

  it "will not acquire a monitor already held by another thread" do
    monitor = Monitor.new
    10.times do
      locked = false

      thread1 = Thread.new do
        begin
          monitor.enter
          locked = true
          sleep # wait for wakeup.
        ensure
          monitor.exit
        end
      end

      Thread.pass until locked
      monitor.mon_locked?.should == true

      thread2 = Thread.new do
        begin
          val = monitor.try_enter
        ensure
          monitor.exit if val
        end
        val
      end

      thread2.join
      thread2.value.should == false

      thread1.wakeup
      thread1.join
      monitor.mon_locked?.should == false
    end
  end
end
