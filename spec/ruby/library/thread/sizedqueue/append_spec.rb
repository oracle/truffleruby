require_relative '../../../spec_helper'
require_relative '../../../shared/queue/enque'
require_relative '../../../shared/sizedqueue/enque'

describe "Thread::SizedQueue#<<" do
  it_behaves_like :queue_enq, :<<, -> { Thread::SizedQueue.new(10) }
end

describe "Thread::SizedQueue#<<" do
  it_behaves_like :sizedqueue_enq, :<<, ->(n) { Thread::SizedQueue.new(n) }
end
