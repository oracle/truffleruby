require 'chunky_png'
require 'oily_png/oily_png'

module OilyPNG
  class Canvas < ChunkyPNG::Canvas
    extend OilyPNG::PNGDecoding
    include OilyPNG::PNGEncoding
    include OilyPNG::Operations
    include OilyPNG::Resampling
  end
  
  module Color
    extend self
  end
end