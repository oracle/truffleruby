require 'spec_helper'

describe OilyPNG::Resampling do

  include OilyPNG::Resampling

  describe '#steps' do
    it "should generate the steps from 4 to 8 as [0,0,1,1,2,2,3,3]" do
      steps(4,8).should == [0,0,1,1,2,2,3,3]
    end

    it "should generate the steps the same as ChunkyPNG" do
      image = ChunkyPNG::Image.new(1,1)
      steps(2,8).should == image.send(:steps,2,8)
      steps(2,11).should == image.send(:steps,2,11)
      steps(11,5).should == image.send(:steps,11,5)
    end
  end

  describe '#resample_nearest_neighbor!' do
    before(:all) { @reference = ChunkyPNG::Canvas.from_file(resource_file('nonsquare.png'))}

    it "should resample [0,1,2,3] to 4x4 properly" do
      OilyPNG::Canvas.new(2,2,[0,1,2,3]).resample_nearest_neighbor(4,4).should == OilyPNG::Canvas.new(4,4,[0,0,1,1,0,0,1,1,2,2,3,3,2,2,3,3])
    end

    it "should resample [0,1,2,3] to 99x45 as ChunkyPNG does" do
      ChunkyPNG::Canvas.new(2,2,[0,1,2,3]).resample_nearest_neighbor(99,45).should == OilyPNG::Canvas.new(2,2,[0,1,2,3]).resample_nearest_neighbor(99,45)
    end

    it "should resample an image to 10x20 as ChunkyPNG does" do
      @reference.resample_nearest_neighbor(10,20).should == OilyPNG::Canvas.from_canvas(@reference).resample_nearest_neighbor(10,20)
    end

    it "should resample an image to 11x19 as ChunkyPNG does" do
      @reference.resample_nearest_neighbor(11,19).should == OilyPNG::Canvas.from_canvas(@reference).resample_nearest_neighbor(11,19)
    end
  end

  describe '#resample_bilinear!' do
    before(:all) { @reference = ChunkyPNG::Canvas.from_file(resource_file('nonsquare.png'))}

    it "should resample an image to 10x20 as ChunkyPNG does" do
      @reference.resample_bilinear(10,20).should == OilyPNG::Canvas.from_canvas(@reference).resample_bilinear(10,20)
    end

    it "should resample an image to 11x19 as ChunkyPNG does" do
      @reference.resample_bilinear(11,19).should == OilyPNG::Canvas.from_canvas(@reference).resample_bilinear(11,19)
    end
  end
end
