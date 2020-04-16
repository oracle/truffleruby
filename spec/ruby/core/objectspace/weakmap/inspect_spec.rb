require_relative '../../../spec_helper'

describe "ObjectSpace::WeakMap#inspect" do
  it "displays object pointers in output" do
    map = ObjectSpace::WeakMap.new
    # important to test with BasicObject (without Kernel) here to test edge cases
    key1, key2 = [BasicObject.new, Object.new]
    ref1, ref2 = [BasicObject.new, Object.new]
    map.inspect.should =~ /\A\#<ObjectSpace::WeakMap:0x\h+>\z/
    map[key1] = ref1
    map.inspect.should =~ /\A\#<ObjectSpace::WeakMap:0x\h+: \#<BasicObject:0x\h+> => \#<BasicObject:0x\h+>>\z/
    map[key1] = ref1
    map.inspect.should =~ /\A\#<ObjectSpace::WeakMap:0x\h+: \#<BasicObject:0x\h+> => \#<BasicObject:0x\h+>>\z/
    map[key2] = ref2
    str = map.inspect
    match1 = str =~ /\A\#<ObjectSpace::WeakMap:0x\h+: \#<BasicObject:0x\h+> => \#<BasicObject:0x\h+>, \#<Object:0x\h+> => \#<Object:0x\h+>>\z/
    match2 = str =~ /\A\#<ObjectSpace::WeakMap:0x\h+: \#<Object:0x\h+> => \#<Object:0x\h+>, \#<BasicObject:0x\h+> => \#<BasicObject:0x\h+>>\z/
    (match1 == 0 || match2 == 0).should == true
  end
end
