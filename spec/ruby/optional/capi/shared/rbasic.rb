describe :rbasic, shared: true do

  before :all do
    specs = CApiRBasicSpecs.new
    @taint = specs.taint_flag
    @freeze = specs.freeze_flag
  end

  it "reports the appropriate FREEZE and TAINT flags for the object when reading" do
    obj, _ = @data.call
    @specs.get_flags(obj).should == 0
    obj.taint
    @specs.get_flags(obj).should == @taint
    obj.untaint
    @specs.get_flags(obj).should == 0
    obj.freeze
    @specs.get_flags(obj).should == @freeze

    obj, _ = @data.call
    obj.taint
    obj.freeze
    @specs.get_flags(obj).should == @freeze | @taint
  end

  it "supports setting the FREEZE and TAINT flags" do
    obj, _ = @data.call
    @specs.set_flags(obj, @taint).should == @taint
    obj.tainted?.should == true
    @specs.set_flags(obj, 0).should == 0
    obj.tainted?.should == false
    @specs.set_flags(obj, @freeze).should == @freeze
    obj.frozen?.should == true

    @specs.get_flags(obj).should == @freeze

    obj, _ = @data.call
    @specs.set_flags(obj, @freeze | @taint).should == @freeze | @taint
    obj.tainted?.should == true
    obj.frozen?.should == true
  end

  it "supports user flags" do
    obj, _ = @data.call
    @specs.get_flags(obj) == 0
    @specs.set_flags(obj, 1 << 14 | 1 << 16).should == 1 << 14 | 1 << 16
    obj.taint
    @specs.get_flags(obj).should == @taint | 1 << 14 | 1 << 16
    obj.untaint
    @specs.get_flags(obj).should == 1 << 14 | 1 << 16
    @specs.set_flags(obj, 0).should == 0
  end

  it "supports copying the flags from one object over to the other" do
    obj1, obj2 = @data.call
    @specs.set_flags(obj1, @taint | 1 << 14 | 1 << 16)
    @specs.copy_flags(obj2, obj1)
    @specs.get_flags(obj2).should == @taint | 1 << 14 | 1 << 16
    @specs.set_flags(obj1, 0)
    @specs.copy_flags(obj2, obj1)
    @specs.get_flags(obj2).should == 0
  end

  it "supports retrieving the (meta)class" do
    obj, _ = @data.call
    @specs.get_klass(obj).should == obj.class
    meta = (class << obj; self; end)
    @specs.get_klass(obj).should == meta
  end
end