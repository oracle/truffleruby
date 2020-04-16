require_relative '../../../spec_helper'
require_relative 'shared/members'
require_relative 'shared/each'

describe "ObjectSpace::WeakMap#each_key" do
  it_behaves_like :members, ->(map) { a = []; map.each_key{ |k| a << k }; a }, %w[A B]

  before(:all) { @method_name = :each_key }
  it_should_behave_like :each
end
