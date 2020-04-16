describe :include?, shared: true do
  it "recognizes keys in use" do
    map = ObjectSpace::WeakMap.new
    key1, key2 = %w[a b].map &:upcase
    ref1, ref2 = %w[x y]

    map[key1] = ref1
    map.send(@method, key1).should == true
    map[key1] = ref1
    map.send(@method, key1).should == true
    map[key2] = ref2
    map.send(@method, key2).should == true
  end

  it "matches using identity seqantics" do
    map = ObjectSpace::WeakMap.new
    key1, key2 = %w[a a].map &:upcase
    ref = "x"
    map[key1] = ref
    map.send(@method, key2).should == false
  end
end

