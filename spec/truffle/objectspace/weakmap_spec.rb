require_relative '../../ruby/spec_helper'
require_relative 'fixtures/weakmap_iterators.rb'

describe "ObjectSpace::WeakMap" do

  it "can be iterated on" do

    # The last part of this spec does not pass on MRI because the garbage collector is presumably too conservative
    # and will not get rid of the references eagerly enough.

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

    # Below: does not pass on MRI.
    v2 = nil
    GC.start
    map.key?(k2).should == false
    ObjectSpaceFixtures.test_iter(map, :each, [[k1, v1]])
    ObjectSpaceFixtures.test_iter(map, :each_pair, [[k1, v1]])
    ObjectSpaceFixtures.test_iter(map, :each_key, [k1])
    ObjectSpaceFixtures.test_iter(map, :each_value, [v1])

    # Avoid unused warning on v2 assignment above.
    if v2 == nil; end
  end
end
