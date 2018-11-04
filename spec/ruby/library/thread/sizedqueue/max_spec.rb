require_relative '../../../spec_helper'
require_relative '../../../shared/sizedqueue/max'

describe "Thread::SizedQueue#max" do
  it_behaves_like :sizedqueue_max, :max, ->(n) { Thread::SizedQueue.new(n) }
end

describe "Thread::SizedQueue#max=" do
  it_behaves_like :sizedqueue_max=, :max=, ->(n) { Thread::SizedQueue.new(n) }
end
