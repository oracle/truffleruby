require_relative '../../spec_helper'

describe 'TracePoint#lineno' do
  it 'returns the line number of the event' do
    lineno = nil
    TracePoint.new(:line) { |tp|
      lineno = tp.lineno
    }.enable do
      line_event = true
    end
    lineno.should == __LINE__ - 2
  end

  it 'raises RuntimeError if accessed from outside' do
    tp = TracePoint.new(:line) { |tp| }
    -> { tp.lineno }.should raise_error(RuntimeError, 'access from outside')
  end
end
