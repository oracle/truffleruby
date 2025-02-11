require_relative '../../spec_helper'
require 'strscan'

describe "StringScanner#check_until" do
  before do
    @s = StringScanner.new("This is a test")
  end

  it "returns the same value of #scan_until, but don't advances the scan pointer" do
    @s.check_until(/a/).should == "This is a"
    @s.pos.should == 0
    @s.check_until(/test/).should == "This is a test"
  end

  it "sets the last match result" do
    @s.check_until(/a/)
    @s.pre_match.should == "This is "
    @s.matched.should == "a"
    @s.post_match.should == " test"
  end

  version_is StringScanner::Version, ""..."3.1.1" do # ruby_version_is ""..."3.4"
    it "raises TypeError if given a String" do
      -> {
        @s.check_until('T')
      }.should raise_error(TypeError, 'wrong argument type String (expected Regexp)')
    end
  end

  version_is StringScanner::Version, "3.1.1" do # ruby_version_is "3.4"
    it "searches a substring in the rest part of a string if given a String" do
      @s.check_until("a").should == "This is a"
      @s.pos.should == 0
    end

    # https://github.com/ruby/strscan/issues/131
    version_is StringScanner::Version, "3.1.1"..."3.1.3" do # ruby_version_is "3.4.1"
      it "sets the last match result if given a String" do
        @s.check_until("a")
        @s.pre_match.should == ""
        @s.matched.should == "This is a"
        @s.post_match.should == " test"
      end
    end
  end
end
