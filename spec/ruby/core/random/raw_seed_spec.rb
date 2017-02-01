# -*- encoding: binary -*-
require File.expand_path('../../../spec_helper', __FILE__)

describe "Random.raw_seed" do
  it "returns a String" do
    Random.raw_seed(1).should be_an_instance_of(String)
  end

  it "returns a String of the length given as argument" do
    Random.raw_seed(15).length.should == 15
  end

  it "raises an ArgumentError on a negative size" do
    lambda {
      Random.raw_seed(-1)
    }.should raise_error(ArgumentError)
  end

  it "returns an ASCII-8BIT String" do
    Random.raw_seed(15).encoding.should == Encoding::ASCII_8BIT
  end

  it "returns a random binary String" do
    Random.raw_seed(12).should_not == Random.raw_seed(12)
  end
end
