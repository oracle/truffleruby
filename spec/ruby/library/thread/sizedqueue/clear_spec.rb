require_relative '../../../spec_helper'
require_relative '../../../shared/queue/clear'

describe "Thread::SizedQueue#clear" do
  it_behaves_like :queue_clear, :clear, -> { Thread::SizedQueue.new(10) }
end
