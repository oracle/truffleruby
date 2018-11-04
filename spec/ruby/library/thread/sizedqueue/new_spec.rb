require_relative '../../../spec_helper'
require_relative '../../../shared/sizedqueue/new'

describe "Thread::SizedQueue.new" do
  it_behaves_like :sizedqueue_new, :new, ->(*n) { Thread::SizedQueue.new(*n) }
end
