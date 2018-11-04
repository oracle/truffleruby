require_relative '../../../spec_helper'
require_relative '../../../shared/queue/clear'

describe "Thread::Queue#clear" do
  it_behaves_like :queue_clear, :clear, -> { Thread::Queue.new }
end
