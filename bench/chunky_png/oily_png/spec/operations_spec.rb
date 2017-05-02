require 'spec_helper'

describe OilyPNG::Operations do
  
  describe '#compose!' do
    subject { oily_reference_canvas('operations') } 

    it "should compose two images without offset exactly the same way as ChunkyPNG" do
      subcanvas = ChunkyPNG::Canvas.new(4, 8, ChunkyPNG::Color.rgba(0, 0, 0, 75))
      subject.compose!(subcanvas)
      ChunkyPNG::Canvas.from_canvas(subject).should == reference_canvas('operations').compose(subcanvas)
    end

    it "should compose two images with offset exactly the same way as ChunkyPNG" do
      subject.compose!(ChunkyPNG::Canvas.new(4, 8, ChunkyPNG::Color.rgba(0, 0, 0, 75)), 8, 4) 
      subject.should == oily_reference_canvas('composited')
    end

    it "should return itself" do
      subject.compose!(OilyPNG::Canvas.new(1,1)).should equal(subject)
    end

    it "should raise an exception when the pixels to compose fall outside the image" do
      # For now this raises a runtime error, but it should probably raise a ChunkyPNG::OutOfBounds error
      lambda { subject.compose!(OilyPNG::Canvas.new(1,1), 16, 16) }.should raise_error
    end
  end
  
  describe '#replace!' do
    subject { oily_reference_canvas('operations') } 

    it "should compose two images without offset exactly the same way as ChunkyPNG" do
      subcanvas = ChunkyPNG::Canvas.new(3, 2, ChunkyPNG::Color.rgb(200, 255, 0))
      subject.replace!(subcanvas)
      ChunkyPNG::Canvas.from_canvas(subject).should == reference_canvas('operations').replace(subcanvas)
    end

    it "should compose two images with offset exactly the same way as ChunkyPNG" do
      subject.replace!(ChunkyPNG::Canvas.new(3, 2, ChunkyPNG::Color.rgb(200, 255, 0)), 5, 4) 
      subject.should == oily_reference_canvas('replaced')
    end

    it "should return itself" do
      subject.replace!(OilyPNG::Canvas.new(1,1)).should equal(subject)
    end

    it "should raise an exception when the pixels to compose fall outside the image" do
      # For now this raises a runtime error, but it should probably raise a ChunkyPNG::OutOfBounds error
      lambda { subject.replace!(OilyPNG::Canvas.new(1,1), 16, 16) }.should raise_error
    end
  end
end
