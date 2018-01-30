require File.expand_path('../../../spec_helper', __FILE__)

describe "The TOPLEVEL_BINDING constant" do
  before :all do
    @binding_toplevel_variables = eval(ruby_exe(fixture(__FILE__, "toplevel_binding_variables.rb")))
    @binding_toplevel_id = eval(ruby_exe(fixture(__FILE__, "toplevel_binding_id.rb")))
  end
  
  it "includes local variables defined in the same file" do
    @binding_toplevel_variables.should include(:foo)
  end

  it "does not include local variables defined in a required file" do
    @binding_toplevel_variables.should_not include(:bar)
  end

  it "does not include local variables defined in eval" do
    @binding_toplevel_variables.should_not include(:baz)
  end

  it "is always the same object for all top levels" do
    @binding_toplevel_id.uniq.size.should == 1
  end
end
