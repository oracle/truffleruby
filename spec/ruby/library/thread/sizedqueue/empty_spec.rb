require_relative '../../../spec_helper'
require_relative '../../../shared/queue/empty'

describe "Thread::SizedQueue#empty?" do
  it_behaves_like :queue_empty?, :empty?, -> { Thread::SizedQueue.new(10) }
end
