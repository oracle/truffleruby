require_relative '../../../spec_helper'
require_relative '../../../shared/queue/close'

describe "Thread::Queue#close" do
  it_behaves_like :queue_close, :close, -> { Thread::Queue.new }
end
