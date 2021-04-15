require_relative '../spec_helper'

describe "The Kernel class" do
  it "inspects an object without `#class` method" do
    obj = Object.new
    class << obj
      undef_method :class
    end
    obj.inspect.should be_kind_of(String)
  end
end
