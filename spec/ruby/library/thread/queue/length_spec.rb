require_relative '../../../spec_helper'
require_relative '../../../shared/queue/length'

describe "Thread::Queue#length" do
  it_behaves_like :queue_length, :length, -> { Thread::Queue.new }
end
