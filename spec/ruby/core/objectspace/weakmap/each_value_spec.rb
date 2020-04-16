require_relative '../../../spec_helper'
require_relative 'shared/members'
require_relative 'shared/each'

describe "ObjectSpace::WeakMap#each_value" do
  it_behaves_like :members, ->(map) { a = []; map.each_value{ |k| a << k }; a }, %w[x y]

  before(:all) { @method_name = :each_value }
  it_should_behave_like :each
end
