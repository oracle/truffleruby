class PSD
  class Layer
    module ChannelImage
      attr_reader :image

      def parse_channel_image(header)
        image = PSD::ChannelImage.new(@file, header, self)
        @image = LazyExecute.new(image, @file)
          .now(:skip)
          .later(:parse)
          .ignore(:width, :height)
      end
    end
  end
end