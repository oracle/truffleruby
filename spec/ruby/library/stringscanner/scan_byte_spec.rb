require_relative '../../spec_helper'
require 'strscan'

version_is StringScanner::Version, "3.1.1" do # ruby_version_is "3.4"
  describe "StringScanner#scan_byte" do
    it "scans one byte and returns it as on Integer" do
      s = StringScanner.new('abc') # "abc".bytes => [97, 98, 99]
      s.scan_byte.should == 97
      s.scan_byte.should == 98
      s.scan_byte.should == 99
    end

    it "is not multi-byte character sensitive" do
      s = StringScanner.new("あ") # "あ".bytes => [227, 129, 130]
      s.scan_byte.should == 227
      s.scan_byte.should == 129
      s.scan_byte.should == 130
    end

    it "returns nil at the end of the string" do
      s = StringScanner.new('a')
      s.scan_byte # skip one
      s.scan_byte.should == nil
      s.pos.should == 1
    end

    it "changes current position" do
      s = StringScanner.new('abc')
      s.pos.should == 0
      s.scan_byte
      s.pos.should == 1
    end

    it "sets the last match result" do
      s = StringScanner.new('abc')
      s.pos = 1
      s.scan_byte

      s.pre_match.should == "a"
      s.matched.should == "b"
      s.post_match.should == "c"
    end
  end
end
