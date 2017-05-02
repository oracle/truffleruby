class PSD
  module ImageMode
    module CMYK
      private

      def combine_cmyk_channel
        (0...@num_pixels).step(pixel_step) do |i|
          c = m = y = k = 0
          a = 255

          @channels_info.each_with_index do |chan, index|
            next if chan[:id] == -2

            val = @channel_data[i + (@channel_length * index)]

            case chan[:id]
            when -1 then a = val
            when 0 then c = val
            when 1 then m = val
            when 2 then y = val
            when 3 then k = val
            end
          end

          rgb = PSD::Color.cmyk_to_rgb(255 - c, 255 - m, 255 - y, 255 - k)
          @pixel_data.push ChunkyPNG::Color.rgba(*rgb.values, a)
        end
      end
    end
  end
end