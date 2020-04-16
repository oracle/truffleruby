# truffleruby_primitives: true

require_relative '../../ruby/spec_helper'
require_relative 'fixtures/weakmap_iterators'

describe "ObjectSpace::WeakMap" do

  it "gets rid of unreferenced objects" do
    map = ObjectSpace::WeakMap.new
    key = "a".upcase
    ref = "x"
    map[key] = ref
    ref = nil

    Primitive.gc_force

    map[key].should == nil
    map.key?(key).should == false
    map.include?(key).should == false
    map.member?(key).should == false
    map.values.should == []
    map.keys.should == []
    map.size.should == 0
    map.length.should == 0
  end

  it "has iterators methods that exclude unreferenced objects" do

    # This spec does not pass on MRI because the garbage collector is presumably too conservative and will not get rid
    # of the references eagerly enough.

    map = ObjectSpace::WeakMap.new
    k1 = "a".upcase; k2 = "b".upcase
    v1 = "x".upcase; v2 = "y".upcase
    map[k1] = v1
    map[k2] = v2
    v2 = nil

    Primitive.gc_force

    map.key?(k2).should == false
    ObjectSpaceFixtures.test_iter(map, :each, [[k1, v1]])
    ObjectSpaceFixtures.test_iter(map, :each_pair, [[k1, v1]])
    ObjectSpaceFixtures.test_iter(map, :each_key, [k1])
    ObjectSpaceFixtures.test_iter(map, :each_value, [v1])

    # Avoid unused warning on v2 assignment above.
    if v2 == nil; end
  end
end
