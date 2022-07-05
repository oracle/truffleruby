require_relative '../../spec_helper'

describe "Range#clone" do
  it "duplicates the range" do
    copy = (1..3).clone
    copy.begin.should == 1
    copy.end.should == 3
    copy.should_not.exclude_end?

    copy = ("a"..."z").clone
    copy.begin.should == "a"
    copy.end.should == "z"
    copy.should.exclude_end?
  end

  it "maintains the frozen state" do
    (1..2).clone.frozen?.should == (1..2).frozen?
    (1..).clone.frozen?.should == (1..).frozen?
    Range.new(1, 2).clone.frozen?.should == Range.new(1, 2).frozen?
    Class.new(Range).new(1, 2).clone.frozen?.should == Class.new(Range).new(1, 2).frozen?
  end
end
