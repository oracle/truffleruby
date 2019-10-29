require_relative '../../spec_helper'
require_relative 'fixtures/classes'

describe "Struct-based class#clone" do

  it "duplicates members" do
    klass = Struct.new(:foo, :bar)
    instance = klass.new(14, 2)
    duped = instance.clone
    duped.foo.should == 14
    duped.bar.should == 2
  end

end
