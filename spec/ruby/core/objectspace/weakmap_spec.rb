require_relative '../../spec_helper'

describe "ObjectSpace::WeakMap" do

  it "includes Enumerable" do
    ObjectSpace::WeakMap.include?(Enumerable).should == true
  end

  it "gets rid of unreferenced objects" do
    map = ObjectSpace::WeakMap.new
    key = "a".upcase
    ref = "x".upcase
    (map[key] = ref).should == ref
    map[key].should == ref
    map.key?(key).should == true
    map.include?(key).should == true
    map.member?(key).should == true
    map.size.should == 1
    map.length.should == 1

    ref = nil
    GC.start
    map[key].should == nil
    map.key?(key).should == false
    map.include?(key).should == false
    map.member?(key).should == false
    map.size.should == 0
    map.length.should == 0
  end
end