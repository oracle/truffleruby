module ObjectSpaceFixtures

  @arr = []

  def self.array
    @arr
  end

  def self.collector(*x)
    @arr << (x.length > 1 ? x : x[0])
  end

  def self.sorter(x)
    x === String ? x : x[0]
  end

  def self.test_iter(map, method, result)
    map.send(method, &method(:collector)).should == map
    @arr.sort_by(&method(:sorter)).should == result
    @arr = [] # at the end to not retain refs!
  end
end
