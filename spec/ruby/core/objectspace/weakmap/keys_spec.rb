require_relative '../../../spec_helper'
require_relative 'shared/members'

describe "ObjectSpace::WeakMap#keys" do
  it_behaves_like :members, ->(map) { map.keys }, %w[A B]
end
