require_relative '../../../spec_helper'
require_relative 'shared/include'

describe "ObjectSpace::WeakMap#include?" do
  it_behaves_like :include?, :include?
end
