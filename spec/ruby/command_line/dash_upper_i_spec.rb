require_relative '../spec_helper'

describe "The -I command line option" do
  before :each do
    @script = fixture __FILE__, "loadpath.rb"
  end

  it "adds the path to the load path ($:)" do
    ruby_exe(@script, options: "-I fixtures").should include("fixtures")
  end

  it "adds the path at the front of $LOAD_PATH" do
    ruby_exe(@script, options: "-I fixtures").lines[0].should include("fixtures")
  end

  it "adds the path expanded from CWD to $LOAD_PATH" do
    ruby_exe(@script, options: "-I fixtures").lines[0].should == "#{Dir.pwd}/fixtures\n"
  end

  it "expands a path from CWD even if it does not exist" do
    ruby_exe(@script, options: "-I not_exist/not_exist").lines[0].should == "#{Dir.pwd}/not_exist/not_exist\n"
  end
end

describe "The -I command line option" do
  before :each do
    @script = fixture __FILE__, "loadpath.rb"
    @fixtures = File.dirname(@script)
    @symlink = tmp("loadpath_symlink")
    File.symlink(@fixtures, @symlink)
  end

  after :each do
    rm_r @symlink
  end

  it "does not expand symlinks" do
    ruby_exe(@script, options: "-I #{@symlink}").lines[0].should == "#{@symlink}\n"
  end
end
