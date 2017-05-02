require 'spec_helper'

describe OilyPNG::Color do

  include OilyPNG::Color

  before(:each) do
    @white             = 0xffffffff
    @black             = 0x000000ff
    @opaque            = 0x0a6496ff
    @non_opaque        = 0x0a649664
    @fully_transparent = 0x0a649600
  end

  describe '#compose_quick' do

    it "should use the foregorund color as is when the background color is fully transparent" do
      compose_quick(@non_opaque, @fully_transparent).should == @non_opaque
    end

    it "should use the foregorund color as is when an opaque color is given as foreground color" do
      compose_quick(@opaque, @white).should == @opaque
    end

    it "should use the background color as is when a fully transparent pixel is given as foreground color" do
      compose_quick(@fully_transparent, @white).should == @white
    end

    it "should compose pixels correctly" do
      compose_quick(@non_opaque, @white).should == 0x9fc2d6ff
    end
    
    it "should compose colors exactly the same as ChunkyPNG" do
      fg, bg = rand(0xffffffff), rand(0xffffffff)
      compose_quick(fg, bg).should == ChunkyPNG::Color.compose_quick(fg, bg)
    end
  end
end