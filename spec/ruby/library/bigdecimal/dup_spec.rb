require_relative '../../spec_helper'
require 'bigdecimal'

describe "BigDecimal#dup" do
  before :each do
    @obj = BigDecimal("1.2345")
  end

  ruby_version_is "" ... "2.5" do
    it "copies the BigDecimal's value to a newly allocated object" do
      copy = @obj.dup

      copy.should_not equal(@obj)
      copy.should == @obj
    end
  end

  ruby_version_is "2.5" do
    it "returns self" do
      @obj.dup.should equal(@obj)
    end
  end
end
