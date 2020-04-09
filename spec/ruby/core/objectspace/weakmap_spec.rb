require_relative '../../spec_helper'
require_relative 'fixtures/weakmap_iterators.rb'

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

  it "matches using identity semantics" do
    map = ObjectSpace::WeakMap.new
    x1 = "x"
    map[x1] = "y"
    map[x1].should == "y"
    map.key?(x1).should == true
    map.include?(x1).should == true
    map.member?(x1).should == true

    x2 = "X".downcase
    map[x2].should == nil
    map.key?(x2).should == false
    map.include?(x2).should == false
    map.member?(x2).should == false
  end

  it "displays object pointers in #inspect output" do
    map = ObjectSpace::WeakMap.new
    k1 = BasicObject.new # important to test with BasicObject (without Kernel) here to test edge cases
    v1 = BasicObject.new
    k2 = Object.new; k3 = Object.new
    v2 = Object.new; v3 = Object.new

    map.inspect.should =~ /\A\#<ObjectSpace::WeakMap:0x\h+>\z/

    map[k1] = v1
    map.inspect.should =~ /\A\#<ObjectSpace::WeakMap:0x\h+: \#<BasicObject:0x\h+> => \#<BasicObject:0x\h+>>\z/

    map = ObjectSpace::WeakMap.new
    map[k2] = v2
    map[k3] = v3
    map.inspect.should =~ /\A\#<ObjectSpace::WeakMap:0x\h+: \#<Object:0x\h+> => \#<Object:0x\h+>, \#<Object:0x\h+> => \#<Object:0x\h+>>\z/
  end

  it "can be iterated on" do
    map = ObjectSpace::WeakMap.new
    k1 = "a".upcase; k2 = "b".upcase
    v1 = "x".upcase; v2 = "y".upcase

    ObjectSpaceFixtures.test_iter(map, :each, [])
    ObjectSpaceFixtures.test_iter(map, :each_pair, [])
    ObjectSpaceFixtures.test_iter(map, :each_key, [])
    ObjectSpaceFixtures.test_iter(map, :each_value, [])

    map[k1] = v1
    ObjectSpaceFixtures.test_iter(map, :each, [[k1, v1]])
    ObjectSpaceFixtures.test_iter(map, :each_pair, [[k1, v1]])
    ObjectSpaceFixtures.test_iter(map, :each_key, [k1])
    ObjectSpaceFixtures.test_iter(map, :each_value, [v1])

    map[k2] = v2
    map.key?(k2).should == true
    ObjectSpaceFixtures.test_iter(map, :each, [[k1, v1], [k2, v2]])
    ObjectSpaceFixtures.test_iter(map, :each_pair, [[k1, v1], [k2, v2]])
    ObjectSpaceFixtures.test_iter(map, :each_key, [k1, k2])
    ObjectSpaceFixtures.test_iter(map, :each_value, [v1, v2])

    # Ideally, we'd test that the iteration methods behave proplery after GC here, but the GC is presumably to
    # conservative on MRI to get this to work reliably.
  end

  it "has iterator methods that must take a block, except when empty" do
    map = ObjectSpace::WeakMap.new

    map.each.should == map
    map.each_pair.should == map
    map.each_key.should == map
    map.each_value.should == map

    ref = "x"
    map["a"] = ref

    -> { map.each }.should raise_error(LocalJumpError)
    -> { map.each_pair }.should raise_error(LocalJumpError)
    -> { map.each_key }.should raise_error(LocalJumpError)
    -> { map.each_value }.should raise_error(LocalJumpError)
  end
end