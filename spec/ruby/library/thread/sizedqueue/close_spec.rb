require_relative '../../../spec_helper'
require_relative '../../../shared/queue/close'

describe "Thread::SizedQueue#close" do
  it_behaves_like :queue_close, :close, -> { Thread::SizedQueue.new(10) }
end
