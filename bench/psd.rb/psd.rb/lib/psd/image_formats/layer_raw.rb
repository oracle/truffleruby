class PSD
  module ImageFormat
    module LayerRAW
      private

      # Since we're parsing on a per-channel basis, we need to modify the behavior
      # of the RAW encoding parser a bit. This version is aware of the current
      # channel data position, since layers that have RAW encoding often use RLE
      # encoded alpha channels.
      def parse_raw!
        PSD.logger.debug "Attempting to parse RAW encoded channel..."

        (@chan_pos...(@chan_pos + @ch_info[:length] - 2)).each do |i|
          @channel_data[i] = @file.read(1).bytes.to_a[0]
        end

        @chan_pos += @ch_info[:length] - 2
      end
    end
  end
end