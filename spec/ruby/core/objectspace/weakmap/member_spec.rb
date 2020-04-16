require_relative '../../../spec_helper'
require_relative 'shared/include'

describe "ObjectSpace::WeakMap#member?" do
  it_behaves_like :include?, :member?
end
