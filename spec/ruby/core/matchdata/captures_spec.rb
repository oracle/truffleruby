require_relative '../../spec_helper'

describe "MatchData#captures" do
  it "raises TypeError when uninitialized" do
    match_data = MatchData.allocate
    -> { match_data.captures }.should raise_error(TypeError)
  end

  it "returns an array of the match captures" do
    /(.)(.)(\d+)(\d)/.match("THX1138.").captures.should == ["H","X","113","8"]
  end
end
