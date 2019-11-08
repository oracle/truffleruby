require_relative '../../spec_helper'

describe "MatchData#to_s" do
  it "returns the entire matched string" do
    /(.)(.)(\d+)(\d)/.match("THX1138.").to_s.should == "HX1138"
  end

  it "raises TypeError when uninitialized" do
    match_data = MatchData.allocate
    -> { match_data.to_s }.should raise_error(TypeError)
  end
end
