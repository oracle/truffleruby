class PSD
  module ImageFormat
    # Parses a RAW uncompressed image
    module RAW
      private

      def parse_raw!
        @channel_data = @file.read(@length).bytes.to_a
      end
    end
  end
end