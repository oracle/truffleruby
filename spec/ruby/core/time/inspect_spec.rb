require_relative '../../spec_helper'
require_relative 'shared/inspect'

describe "Time#inspect" do
  it_behaves_like :inspect, :inspect

  ruby_version_is "2.7" do
    it "preserves microseconds" do
      t = Time.utc(2007, 11, 1, 15, 25, 0, 123456)
      t.inspect.should == "2007-11-01 15:25:00.123456 UTC"
    end

    it "omits trailing zeros from microseconds" do
      t = Time.utc(2007, 11, 1, 15, 25, 0, 100000)
      t.inspect.should == "2007-11-01 15:25:00.1 UTC"
    end

    it "preserves nanoseconds" do
      t = Time.utc(2007, 11, 1, 15, 25, 0, 123456.789r)
      t.inspect.should == "2007-11-01 15:25:00.123456789 UTC"
    end
  end
end
