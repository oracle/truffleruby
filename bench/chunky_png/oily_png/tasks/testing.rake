task(:verify, :png_file) do |task, args|
  require 'rubygems'
  require 'bundler/setup'
  require 'chunky_png'
  require 'oily_png/oily_png_ext'

  class OilyPNG::Canvas < ChunkyPNG::Canvas
    extend OilyPNG::PNGDecoding
    include OilyPNG::PNGEncoding
  end
  
  file = args[:png_file] || ENV['PNG_FILE']
  raise "Please specify a valid PNG file to verify!" unless File.exist?(file.to_s)
  
  decoding_reference = ChunkyPNG::Canvas.from_file(file)
  decoding_oily_png  = OilyPNG::Canvas.from_file(file)
  
  if decoding_reference == decoding_oily_png
    puts "Decoding test succeeded!"
  else
    puts "Decoding test FAILED!"
  end
  
  oily_png = OilyPNG::Canvas.from_canvas(decoding_reference)
  
  [ChunkyPNG::FILTER_NONE, ChunkyPNG::FILTER_SUB, ChunkyPNG::FILTER_UP, ChunkyPNG::FILTER_AVERAGE, ChunkyPNG::FILTER_PAETH].each do |filter_method|
    
    encoding_reference = decoding_reference.to_blob(:filtering => filter_method, :color_mode => ChunkyPNG::COLOR_TRUECOLOR_ALPHA)
    encoding_oily_png  = oily_png.to_blob(:filtering => filter_method, :color_mode => ChunkyPNG::COLOR_TRUECOLOR_ALPHA)
    
    if encoding_reference == encoding_oily_png
      puts "Encoding test succeeded for filter method #{filter_method}!"
    else
      puts "Encoding test FAILED for filter method #{filter_method}!"
    end
  end
  
  [ChunkyPNG::COLOR_GRAYSCALE, ChunkyPNG::COLOR_GRAYSCALE_ALPHA, ChunkyPNG::COLOR_INDEXED, ChunkyPNG::COLOR_TRUECOLOR, ChunkyPNG::COLOR_TRUECOLOR_ALPHA].each do |color_mode|
    
    encoding_reference = decoding_reference.to_blob(:filtering => ChunkyPNG::FILTER_NONE, :color_mode => color_mode)
    encoding_oily_png  = oily_png.to_blob(:filtering => ChunkyPNG::FILTER_NONE, :color_mode => color_mode)
    
    if encoding_reference == encoding_oily_png
      puts "Encoding test succeeded for color mode #{color_mode}!"
    else
      puts "Decoding test FAILED for color mode #{color_mode}!"
    end
  end
end
