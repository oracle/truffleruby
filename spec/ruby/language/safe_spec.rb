require_relative '../spec_helper'

describe "The $SAFE variable" do
  it "is 0 by default" do
    $SAFE.should == 0
    proc {
      $SAFE.should == 0
    }.call
  end
  
  it "can be set to 0" do
    proc {
      $SAFE = 0
      $SAFE.should == 0
    }.call
  end
  
  it "can be set to 1" do
    proc {
      $SAFE = 1
      $SAFE.should == 1
    }.call
  end
  
  [2, 3, 4].each do |n|
    it "cannot be set to #{n}" do
      lambda {
        proc {
          $SAFE = n
        }.call
      }.should raise_error(ArgumentError, /\$SAFE=2 to 4 are obsolete/)
    end
  end
  
  it "cannot be set to values above 4" do
      lambda {
        proc {
          $SAFE = 100
        }.call
      }.should raise_error(ArgumentError, /\$SAFE=2 to 4 are obsolete/)
  end
  
  it "cannot be manually lowered" do
    proc {
      $SAFE = 1
      lambda {
        $SAFE = 0
      }.should raise_error(SecurityError, /tried to downgrade safe level from 1 to 0/)
    }.call
  end
  
  it "is automatically lowered when leaving a proc" do
    $SAFE.should == 0
    proc {
      $SAFE = 1
    }.call
    $SAFE.should == 0
  end
  
  it "is automatically lowered when leaving a lambda" do
    $SAFE.should == 0
    lambda {
      $SAFE = 1
    }.call
    $SAFE.should == 0
  end
  
  it "can be read when default from Thread#safe_level" do
    Thread.current.safe_level.should == 0
  end
  
  it "can be read when modified from Thread#safe_level" do
    proc {
      $SAFE = 1
      Thread.current.safe_level.should == 1
    }.call
  end
end
