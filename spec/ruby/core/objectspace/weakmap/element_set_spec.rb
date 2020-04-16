require_relative '../../../spec_helper'

describe "ObjectSpace::WeakMap#[]=" do
  it "is correct" do
    map = ObjectSpace::WeakMap.new
    key1, key2 = %w[a b].map &:upcase
    ref1, ref2 = %w[x y]
    (map[key1] = ref1).should == ref1
    map[key1].should == ref1
    (map[key1] = ref1).should == ref1
    map[key1].should == ref1
    (map[key2] = ref2).should == ref2
    map[key1].should == ref1
    map[key2].should == ref2
  end

  it "does not accept primitive or frozen keys or values" do
    map = ObjectSpace::WeakMap.new
    x = Object.new
    -> { map[true] = x }.should raise_error(ArgumentError)
    -> { map[true] = x }.should raise_error(ArgumentError)
    -> { map[false] = x }.should raise_error(ArgumentError)
    -> { map[nil] = x }.should raise_error(ArgumentError)
    -> { map[42] = x }.should raise_error(ArgumentError)
    -> { map[:foo] =  x}.should raise_error(ArgumentError)
    -> { map[x] = true }.should raise_error(ArgumentError)
    -> { map[x] = false }.should raise_error(ArgumentError)
    -> { map[x] = nil }.should raise_error(ArgumentError)
    -> { map[x] = 42 }.should raise_error(ArgumentError)
    -> { map[x] = :foo }.should raise_error(ArgumentError)

    y = Object.new.freeze
    -> { map[x] = y}.should raise_error(FrozenError)
    -> { map[y] = x}.should raise_error(FrozenError)
  end
end
