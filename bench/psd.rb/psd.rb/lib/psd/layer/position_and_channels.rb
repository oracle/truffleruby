class PSD
  class Layer
    module PositionAndChannels
      attr_reader :top, :left, :bottom, :right, :cols, :rows
      attr_reader :channels, :channels_info

      alias :width :cols
      alias :height :rows

      private

      def parse_position_and_channels
        start_section(:info)

        @top = @file.read_int
        @left = @file.read_int
        @bottom = @file.read_int
        @right = @file.read_int
        @channels = @file.read_short

        @rows = @bottom - @top
        @cols = @right - @left

        @channels.times do
          channel_id = @file.read_short
          channel_length = @file.read_int

          @channels_info << {id: channel_id, length: channel_length}
        end

        end_section(:info)
      end

      def export_position_and_channels(outfile)
        [@top, @left, @bottom, @right].each { |val| outfile.write_int(val) }
        outfile.write_short(@channels)

        @channels_info.each do |channel_info|
          outfile.write_short channel_info[:id]
          outfile.write_int channel_info[:length]
        end

        @file.seek end_of_section(:info)
      end
    end
  end
end