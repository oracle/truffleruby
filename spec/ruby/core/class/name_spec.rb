require_relative '../../spec_helper'
require_relative 'fixtures/classes'

describe "Class#name" do
  it "returns the name as a string for a normal class" do
    CoreClassSpecs::Record.name.should == "CoreClassSpecs::Record"
  end

  it "returns nil for a singleton class object" do
    CoreClassSpecs::Record.singleton_class.name.should be_nil
  end
end
