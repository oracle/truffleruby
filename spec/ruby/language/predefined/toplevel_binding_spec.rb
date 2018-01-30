require File.expand_path('../../../spec_helper', __FILE__)

describe "The TOPLEVEL_BINDING constant" do
  it "only includes local variables defined in the main script, not in required files or eval" do
    binding_toplevel_variables = ruby_exe(fixture(__FILE__, "toplevel_binding_variables.rb"))
    binding_toplevel_variables.should == "[:required_after, [:main_script]]\n[:main_script]\n"
  end

  it "has no local variables in files required before the main script" do
    required = fixture(__FILE__, 'toplevel_binding_required_before.rb')
    out = ruby_exe("a=1; p TOPLEVEL_BINDING.local_variables.sort; b=2", options: "-r#{required}")
    out.should == "[:required_before, []]\n[:a, :b]\n"
  end

  it "is always the same object for all top levels" do
    binding_toplevel_id = ruby_exe(fixture(__FILE__, "toplevel_binding_id.rb"))
    binding_toplevel_id.should == "1\n"
  end
end
