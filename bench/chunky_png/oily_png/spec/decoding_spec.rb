require 'spec_helper'

describe OilyPNG::PNGDecoding do

  it "should call ChunkyPNG::Color.pixel_bytesize in the pure ruby version" do
    ChunkyPNG::Color.should_receive(:pixel_bytesize).and_return(3)
    ChunkyPNG::Canvas.from_file(resource_file('square.png'))
  end

  it "should not call ChunkyPNG::Color.pixel_bytesize in the native version" do
    ChunkyPNG::Color.should_not_receive(:pixel_bytesize)
    OilyPNG::Canvas.from_file(resource_file('square.png'))
  end
  
  context 'decoding different filtering methods' do
    before(:all) { @reference = ChunkyPNG::Canvas.from_file(resource_file('nonsquare.png'))}
    
    it "should decode NONE filtering exactly the same as ChunkyPNG" do
      filtered_data = @reference.to_blob(:filtering => ChunkyPNG::FILTER_NONE)
      ChunkyPNG::Canvas.from_blob(filtered_data).should == OilyPNG::Canvas.from_blob(filtered_data)
    end
    
    it "should decode SUB filtering exactly the same as ChunkyPNG" do
      filtered_data = @reference.to_blob(:filtering => ChunkyPNG::FILTER_SUB)
      ChunkyPNG::Canvas.from_blob(filtered_data).should == OilyPNG::Canvas.from_blob(filtered_data)
    end
    
    it "should decode UP filtering exactly the same as ChunkyPNG" do
      filtered_data = @reference.to_blob(:filtering => ChunkyPNG::FILTER_UP)
      ChunkyPNG::Canvas.from_blob(filtered_data).should == OilyPNG::Canvas.from_blob(filtered_data)
    end
    
    it "should decode AVERAGE filtering exactly the same as ChunkyPNG" do
      filtered_data = @reference.to_blob(:filtering => ChunkyPNG::FILTER_AVERAGE)
      ChunkyPNG::Canvas.from_blob(filtered_data).should == OilyPNG::Canvas.from_blob(filtered_data)
    end
    
    it "should decode PAETH filtering exactly the same as ChunkyPNG" do
      filtered_data = @reference.to_blob(:filtering => ChunkyPNG::FILTER_PAETH)
      ChunkyPNG::Canvas.from_blob(filtered_data).should == OilyPNG::Canvas.from_blob(filtered_data)
    end
  end
  
  context 'decoding compatibility with ChunkyPNG' do
    resource_files.each do |file|
      it "should #{File.basename(file)} the same as ChunkyPNG" do
        OilyPNG::Canvas.from_file(file).pixels.should == ChunkyPNG::Canvas.from_file(file).pixels
      end
    end
  end
end
