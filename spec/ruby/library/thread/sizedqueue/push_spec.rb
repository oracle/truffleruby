require_relative '../../../spec_helper'
require_relative '../../../shared/queue/enque'
require_relative '../../../shared/sizedqueue/enque'

describe "Thread::SizedQueue#push" do
  it_behaves_like :queue_enq, :push, -> { SizedQueue.new(10) }
end

describe "Thread::SizedQueue#push" do
  it_behaves_like :sizedqueue_enq, :push, ->(n) { Thread::SizedQueue.new(n) }
end
