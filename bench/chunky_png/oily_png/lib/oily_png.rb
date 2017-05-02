require 'chunky_png'

module OilyPNG
  def self.included(base)
    base::Canvas.send(:extend, OilyPNG::PNGDecoding)
    base::Canvas.send(:include, OilyPNG::PNGEncoding)


    base::Color.send(:include, OilyPNG::Color)
    
    base::Color.extend OilyPNG::Color
    base::Canvas.send(:include, OilyPNG::Resampling)
  end
end

require 'oily_png/version'
require 'oily_png/oily_png'
require 'oily_png/canvas'

# Include mixin into ChunkyPNG
ChunkyPNG.send(:include, OilyPNG)
