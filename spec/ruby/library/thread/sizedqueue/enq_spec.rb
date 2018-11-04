require_relative '../../../spec_helper'
require_relative '../../../shared/queue/enque'
require_relative '../../../shared/sizedqueue/enque'

describe "Thread::SizedQueue#enq" do
  it_behaves_like :queue_enq, :enq, -> { Thread::SizedQueue.new(10) }
end

describe "Thread::SizedQueue#enq" do
  it_behaves_like :sizedqueue_enq, :enq, ->(n) { Thread::SizedQueue.new(n) }
end
