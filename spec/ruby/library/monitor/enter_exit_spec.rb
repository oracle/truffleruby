require_relative '../../spec_helper'
require 'monitor'

describe "Monitor#enter" do
  it "acquires the monitor" do
    monitor = Monitor.new
    10.times do
      locked = false

      thread = Thread.new do
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
      thread.wakeup
      thread.join
      monitor.mon_locked?.should == false
    end
  end
end
