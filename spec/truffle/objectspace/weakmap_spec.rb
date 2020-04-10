require_relative '../../ruby/spec_helper'

describe "ObjectSpace::WeakMap" do

  it "can be iterated on" do

    # The last part of this spec does not pass on MRI because the garbage collector is presumably too conservative
    # and will not get rid of the references eagerly enough.

    map = ObjectSpace::WeakMap.new
    k1 = "a".upcase; k2 = "b".upcase
    v1 = "x".upcase; v2 = "y".upcase

    # NOTE: We must avoid using lambda, because lambda capture seems to prevent GC of the local variables when running the
    # spec. So define helper methods on the iso object instead.
    # (This was true on MRI, but not TruffleRuby, still let's keep this for future-proofing.)
    iso = Struct.new(:arr, :map).new([], map)

    def iso.collector(*x)
      arr << (x.length > 1 ? x : x[0])
    end

    def iso.sorter(x)
      x === String ? x : x[0]
    end

    def iso.test_iter(method, result)
      map.send(method, &method(:collector)).should == map
      arr.sort_by(&method(:sorter)).should == result
      self.arr = [] # at the end to not retain refs!
    end

    iso.test_iter(:each, [])
    iso.test_iter(:each_pair, [])
    iso.test_iter(:each_key, [])
    iso.test_iter(:each_value, [])

    map[k1] = v1
    iso.test_iter(:each, [[k1, v1]])
    iso.test_iter(:each_pair, [[k1, v1]])
    iso.test_iter(:each_key, [k1])
    iso.test_iter(:each_value, [v1])

    map[k2] = v2
    map.key?(k2).should == true
    iso.test_iter(:each, [[k1, v1], [k2, v2]])
    iso.test_iter(:each_pair, [[k1, v1], [k2, v2]])
    iso.test_iter(:each_key, [k1, k2])
    iso.test_iter(:each_value, [v1, v2])

    # Below: does not pass on MRI.

    v2 = nil
    GC.start
    map.key?(k2).should == false
    iso.test_iter(:each, [[k1, v1]])
    iso.test_iter(:each_pair, [[k1, v1]])
    iso.test_iter(:each_key, [k1])
    iso.test_iter(:each_value, [v1])

    # Avoid unused warning on v2 assignment above.
    if v2 == nil; end
  end
end
