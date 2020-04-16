require_relative '../../../spec_helper'
require_relative 'shared/members'
require_relative 'shared/each'

describe "ObjectSpace::WeakMap#each" do
  it_behaves_like :members, ->(map) { a = []; map.each{ |k,v| a << "#{k}#{v}" }; a }, %w[Ax By]

  before(:all) { @method_name = :each }
  it_should_behave_like :each
end
