require_relative '../../../spec_helper'
require_relative '../../../shared/queue/closed'

describe "Thread::Queue#closed?" do
  it_behaves_like :queue_closed?, :closed?, -> { Thread::Queue.new }
end
