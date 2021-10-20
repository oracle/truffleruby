require_relative '../../ruby/spec_helper'

describe "File.utime" do
  before :each do
    @atime = Time.now
    @mtime = Time.now
    @file1 = tmp("specs_file_utime1")
    touch @file1
  end

  after :each do
    rm_r @file1
  end

  # not in ruby/spec as some file systems have lower resolution, see ruby/spec@bd3e667270
  it "sets nanosecond precision" do
    t = Time.utc(2007, 11, 1, 15, 25, 0, 123456.789r)
    File.utime(t, t, @file1)
    File.atime(@file1).nsec.should == 123456789
    File.mtime(@file1).nsec.should == 123456789
  end
end
