describe :rbasic, shared: true do

  specs = CApiRBasicSpecs.new
  RUBY_FL_TAINT     = specs.taint_flag
  RUBY_FL_FREEZE    = specs.freeze_flag

  it "reports the appropriate FREEZE and TAINT flags for the object when reading" do
    obj, _ = @object.data
    @object.get_flags(obj).should == 0
    obj.taint
    @object.get_flags(obj).should == RUBY_FL_TAINT
    obj.untaint
    @object.get_flags(obj).should == 0
    obj.freeze
    @object.get_flags(obj).should == RUBY_FL_FREEZE

    obj, _ = @object.data
    obj.taint
    obj.freeze
    @object.get_flags(obj).should == RUBY_FL_FREEZE | RUBY_FL_TAINT
  end

  it "supports setting the FREEZE and TAINT flags" do
    obj, _ = @object.data
    @object.set_flags(obj, RUBY_FL_TAINT).should == RUBY_FL_TAINT
    obj.tainted?.should == true
    @object.set_flags(obj, 0).should == 0
    obj.tainted?.should == false
    @object.set_flags(obj, RUBY_FL_FREEZE).should == RUBY_FL_FREEZE
    obj.frozen?.should == true

    @object.get_flags(obj).should == RUBY_FL_FREEZE

    obj, _ = @object.data
    @object.set_flags(obj, RUBY_FL_FREEZE | RUBY_FL_TAINT).should == RUBY_FL_FREEZE | RUBY_FL_TAINT
    obj.tainted?.should == true
    obj.frozen?.should == true
  end

  it "supports user flags" do
    obj, _ = @object.data
    @object.get_flags(obj) == 0
    @object.set_flags(obj, 1 << 14 | 1 << 16).should == 1 << 14 | 1 << 16
    obj.taint
    @object.get_flags(obj).should == RUBY_FL_TAINT | 1 << 14 | 1 << 16
    obj.untaint
    @object.get_flags(obj).should == 1 << 14 | 1 << 16
    @object.set_flags(obj, 0).should == 0
  end

  it "supports copying the flags from one object over to the other" do
    obj1, obj2 = @object.data
    @object.set_flags(obj1, RUBY_FL_TAINT | 1 << 14 | 1 << 16)
    @object.copy_flags(obj2, obj1)
    @object.get_flags(obj2).should == RUBY_FL_TAINT | 1 << 14 | 1 << 16
    @object.set_flags(obj1, 0)
    @object.copy_flags(obj2, obj1)
    @object.get_flags(obj2).should == 0
  end
end