class PSD
  class Layer
    module Mask
      attr_reader :mask
      
      private

      def parse_mask_data
        @mask_begin = @file.tell
        @mask = PSD::Mask.read(@file)
        @mask_end = @file.tell
      end

      def export_mask_data(outfile)
        outfile.write @file.read(@mask_end - @mask_begin + 4)
      end
    end
  end
end