require 'spec_helper'

describe OilyPNG::PNGEncoding do

  context 'encoding different color settings without palette' do
    before do
      @canvas      = ChunkyPNG::Canvas.from_file(resource_file('gray.png'))
      @oily_canvas = OilyPNG::Canvas.from_canvas(@canvas)
    end
    
    it "should encode an image using 8-bit grayscale correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 8, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 8, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
    
    it "should encode an image using 4-bit grayscale correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 4, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 4, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
    
    it "should encode an image using 2-bit grayscale correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 2, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 2, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
    
    it "should encode an image using 1-bit grayscale correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 1, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE, 1, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
    
    it "should encode an image using 8-bit grayscale alpha correctly" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE_ALPHA, 8, ChunkyPNG::FILTER_NONE)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_GRAYSCALE_ALPHA, 8, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end

    it "should encode an image using 8-bit truecolor correctly" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_NONE)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
    
    it "should encode an image using 8-bit truecolor alpha correctly" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR_ALPHA, 8, ChunkyPNG::FILTER_NONE)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR_ALPHA, 8, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
  end

  context 'encoding with paletted images using different bitrates' do
    before do
      @canvas      = ChunkyPNG::Canvas.from_file(resource_file('gray.png'))
      @oily_canvas = OilyPNG::Canvas.from_canvas(@canvas)
      
      @canvas.encoding_palette = @canvas.palette
      @canvas.encoding_palette.to_plte_chunk
      
      @oily_canvas.encoding_palette = @oily_canvas.palette
      @oily_canvas.encoding_palette.to_plte_chunk
    end
    
    it "should encode an image using 8-bit indexed colors correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 8, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 8, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end

    it "should encode an image using 4-bit indexed colors correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 4, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 4, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end

    it "should encode an image using 2-bit indexed colors correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 2, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 2, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end

    it "should encode an image using 1-bit indexed colors correctly" do
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 1, ChunkyPNG::FILTER_NONE)
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_INDEXED, 1, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
  end
  
  context 'encoding different filters' do
    before do 
      @canvas      = ChunkyPNG::Canvas.from_file(resource_file('nonsquare.png'))
      @oily_canvas = OilyPNG::Canvas.from_canvas(@canvas)
    end
    
    it "should encode correctly with no filtering" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_NONE)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_NONE)
      stream1.should == stream2
    end
    
    it "should encode correctly with sub filtering" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_SUB)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_SUB)
      stream1.should == stream2
    end
    
    it "should encode correctly with up filtering" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_UP)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_UP)
      stream1.should == stream2
    end
    
    it "should encode correctly with average filtering" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_AVERAGE)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_AVERAGE)
      stream1.should == stream2
    end
    
    it "should encode correctly with paeth filtering" do
      @oily_canvas.send(:encode_png_image_pass_to_stream, stream1 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_PAETH)
      @canvas.send(:encode_png_image_pass_to_stream,      stream2 = ChunkyPNG::Datastream.empty_bytearray, ChunkyPNG::COLOR_TRUECOLOR, 8, ChunkyPNG::FILTER_PAETH)
      stream1.should == stream2
    end
  end
  
  it "should encode an interlaced image correctly" do
    canvas = ChunkyPNG::Canvas.from_file(resource_file('interlaced.png'))
    data = OilyPNG::Canvas.from_canvas(canvas).to_blob(:interlace => true)
    ds = ChunkyPNG::Datastream.from_blob(data)
    ds.header_chunk.interlace.should == ChunkyPNG::INTERLACING_ADAM7
    ChunkyPNG::Canvas.from_datastream(ds).should == canvas
  end
end
