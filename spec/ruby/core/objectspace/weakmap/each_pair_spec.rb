require_relative '../../../spec_helper'
require_relative 'shared/members'
require_relative 'shared/each'

describe "ObjectSpace::WeakMap#each_pair" do
  it_behaves_like :members, ->(map) { a = []; map.each_pair{ |k,v| a << "#{k}#{v}" }; a }, %w[Ax By]

  before(:all) { @method_name = :each_pair }
  it_should_behave_like :each
end
