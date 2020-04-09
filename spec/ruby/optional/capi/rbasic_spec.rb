require_relative 'spec_helper'

load_extension("rbasic")

describe "RBasic support" do

  specs = CApiRBasicSpecs.new
  RUBY_FL_TAINT     = specs.taint_flag
  RUBY_FL_FREEZE    = specs.freeze_flag

  it "reports the appropriate FREEZE and TAINT flags for the object when reading" do
    str = "hello"
    specs.get_flags(str).should == 0
    str.taint
    specs.get_flags(str).should == RUBY_FL_TAINT
    str.untaint
    specs.get_flags(str).should == 0
    str.freeze
    specs.get_flags(str).should == RUBY_FL_FREEZE

    str = "hello"
    str.taint
    str.freeze
    specs.get_flags(str).should == RUBY_FL_FREEZE | RUBY_FL_TAINT
  end

  it "supports setting the FREEZE and TAINT flags" do
    str = "hello"
    specs.set_flags(str, RUBY_FL_TAINT).should == RUBY_FL_TAINT
    str.tainted?.should == true
    specs.set_flags(str, 0).should == 0
    str.tainted?.should == false
    specs.set_flags(str, RUBY_FL_FREEZE).should == RUBY_FL_FREEZE
    str.frozen?.should == true

    specs.get_flags(str).should == RUBY_FL_FREEZE

    str = "hello"
    specs.set_flags(str, RUBY_FL_FREEZE | RUBY_FL_TAINT).should == RUBY_FL_FREEZE | RUBY_FL_TAINT
    str.tainted?.should == true
    str.frozen?.should == true
  end

  it "supports user flags" do
    obj = Object.new
    specs.get_flags(obj) == 0
    specs.set_flags(obj, 1 << 14 | 1 << 16).should == 1 << 14 | 1 << 16
    obj.taint
    specs.get_flags(obj).should == RUBY_FL_TAINT | 1 << 14 | 1 << 16
    obj.untaint
    specs.get_flags(obj).should == 1 << 14 | 1 << 16
    specs.set_flags(obj, 0).should == 0
  end

  it "supports copying the flags from one object over to the other" do
    obj1 = Object.new
    obj2 = Object.new
    specs.set_flags(obj1, RUBY_FL_TAINT | 1 << 14 | 1 << 16)
    specs.copy_flags(obj2, obj1)
    specs.get_flags(obj2).should == RUBY_FL_TAINT | 1 << 14 | 1 << 16
    specs.set_flags(obj1, 0)
    specs.copy_flags(obj2, obj1)
    specs.get_flags(obj2).should == 0
  end
end