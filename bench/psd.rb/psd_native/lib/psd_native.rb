require "psd"
require "oily_png"

module PSDNative
  def self.included(base)
    base::Image.send(:include, PSDNative::ImageMode::RGB)
    base::Image.send(:include, PSDNative::ImageMode::CMYK)
    base::Image.send(:include, PSDNative::ImageMode::Greyscale)
    base::Image.send(:include, PSDNative::ImageFormat::RLE)
    base::ChannelImage.send(:include, PSDNative::ImageFormat::LayerRAW)
    base::Color.send(:include, PSDNative::Color)
    base::Util.extend PSDNative::Util

    base::Renderer::ClippingMask.class_eval do
      remove_method :apply!
    end
    base::Renderer::ClippingMask.send(:include, PSDNative::Renderer::ClippingMask)

    base::Renderer::Mask.class_eval do
      remove_method :apply!
    end
    base::Renderer::Mask.send(:include, PSDNative::Renderer::Mask)

    base::Renderer::Blender.class_eval do
      remove_method :compose!
    end
    base::Renderer::Blender.send(:include, PSDNative::Renderer::Blender)
  end
end

require "psd_native/version"
require "psd_native/psd_native"
require "psd_native/compose"

PSD.send :include, PSDNative