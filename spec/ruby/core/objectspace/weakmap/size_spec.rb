require_relative '../../../spec_helper'
require_relative 'shared/size'

describe "ObjectSpace::WeakMap#size" do
  it_behaves_like :size, :size
end
