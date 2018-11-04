require_relative '../../../spec_helper'
require_relative '../../../shared/queue/deque'

describe "Thread::Queue#shift" do
  it_behaves_like :queue_deq, :shift, -> { Thread::Queue.new }
end
