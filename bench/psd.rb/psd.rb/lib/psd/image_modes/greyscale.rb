class PSD
  module ImageMode
    module Greyscale
      private

      def combine_greyscale_channel
        if channels == 2
          (0...@num_pixels).step(pixel_step) do |i|
            grey = @channel_data[i]
            alpha = @channel_data[@channel_length + i]

            @pixel_data.push ChunkyPNG::Color.grayscale_alpha(grey, alpha)
          end
        else
          (0...@num_pixels).step(pixel_step) do |i|
            @pixel_data.push ChunkyPNG::Color.grayscale(@channel_data[i])
          end
        end
      end
    end
  end
end