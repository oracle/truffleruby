require_relative '../../spec_helper'

describe 'TracePoint#enable' do
  # def test; end

  describe 'without a block' do
    it 'returns true if trace was enabled' do
      called = false
      trace = TracePoint.new(:line) do |tp|
        called = true
      end

      line_event = true
      called.should == false

      trace.enable
      begin
        line_event = true
        called.should == true
      ensure
        trace.disable
      end
    end

    it 'returns false if trace was disabled' do
      called = false
      trace = TracePoint.new(:line) do |tp|
        called = true
      end

      trace.enable.should == false
      begin
        line_event = true
        called.should == true
      ensure
        trace.disable
      end

      called = false
      line_event = true
      called.should == false

      trace.enable.should == false
      begin
        line_event = true
        called.should == true
      ensure
        trace.disable
      end
    end
  end

  describe 'with a block' do
    it 'enables the trace object within a block' do
      event_name = nil
      TracePoint.new(:line) do |tp|
        event_name = tp.event
      end.enable { event_name.should equal(:line) }
    end

    ruby_bug "#14057", ""..."2.5" do
      it 'can accept arguments within a block but it should not yield arguments' do
        event_name = nil
        trace = TracePoint.new(:line) { |tp| event_name = tp.event }
        trace.enable do |*args|
          event_name.should equal(:line)
          args.should == []
        end
        trace.enabled?.should == false
      end
    end

    it 'enables trace object on calling with a block if it was already enabled' do
      enabled = nil
      trace = TracePoint.new(:line) {}
      trace.enable
      begin
        trace.enable { enabled = trace.enabled? }
        enabled.should == true
      ensure
        trace.disable
      end
    end

    it 'returns the return value of the block' do
      trace = TracePoint.new(:line) {}
      trace.enable { 42 }.should == 42
    end

    it 'disables the trace object outside the block' do
      called = false
      trace = TracePoint.new(:line) { called = true }
      trace.enable {
        line_event = true
      }
      called.should == true
      trace.enabled?.should == false
    end
  end
end
