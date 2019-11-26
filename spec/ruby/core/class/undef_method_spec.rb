require_relative '../../spec_helper'

describe "Class#undef_method" do
  before :each do
    @class = Class.new
  end

  it "raises a NameError when passed a missing name" do
    -> { @class.send :undef_method, :not_exist }.should raise_error(NameError, /undefined method `not_exist' for class/) { |e|
      # a NameError and not a NoMethodError
      e.class.should == NameError
    }
  end
end
