require File.expand_path('../../../spec_helper', __FILE__)

describe "The TOPLEVEL_BINDING constant" do
  it "includes local variables defined in the same file" do
    eval(ruby_exe(fixture(__FILE__, "toplevel_binding.rb"))).should include(:foo)
  end

  it "does not include local variables defined in a required file" do
    eval(ruby_exe(fixture(__FILE__, "toplevel_binding.rb"))).should_not include(:bar)
  end

  it "does not include local variables defined in eval" do
    eval(ruby_exe(fixture(__FILE__, "toplevel_binding.rb"))).should_not include(:baz)
  end
end
