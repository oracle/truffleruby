require_relative '../../../spec_helper'

describe :matchdata_eql, shared: true do
  it "returns true if both operands have equal target strings, patterns, and match positions" do
    a = 'haystack'.match(/hay/)
    b = 'haystack'.match(/hay/)
    a.send(@method, b).should be_true
  end

  it "returns false if the operands have different target strings" do
    a = 'hay'.match(/hay/)
    b = 'haystack'.match(/hay/)
    a.send(@method, b).should be_false
  end

  it "returns false if the operands have different patterns" do
    a = 'haystack'.match(/h.y/)
    b = 'haystack'.match(/hay/)
    a.send(@method, b).should be_false
  end

  it "returns false if the argument is not a MatchData object" do
    a = 'haystack'.match(/hay/)
    a.send(@method, Object.new).should be_false
  end

  it "returns false if arguments are different and both are uninitialized" do
    a = MatchData.allocate
    b = MatchData.allocate
    a.send(@method, b).should be_false
  end

  it "returns true if arguments are the identical and uninitialized" do
    match_data = MatchData.allocate
    match_data.send(@method, match_data).should be_true
  end
end
