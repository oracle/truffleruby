require_relative '../spec_helper'

describe "The -r command line option" do
  before :each do
    @script = fixture __FILE__, "require.rb"
    @test_file = fixture __FILE__, "test_file"
  end

  it "requires the specified file" do
    out = ruby_exe(@script, options: "-r #{@test_file}")
    out.should include("REQUIRED")
    out.should include(@test_file + ".rb")
  end

  it "requires the file before parsing the main script" do
    out = ruby_exe(fixture(__FILE__, "bad_syntax.rb"), options: "-r #{@test_file}", args: "2>&1")
    $?.should_not.success?
    out.should include("REQUIRED")
    out.should include("syntax error")
  end
end
