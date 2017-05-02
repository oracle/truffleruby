class PSD
  class Layer
    module Exporting
      # Export the layer to file. May or may not work.
      def export(outfile)
        export_position_and_channels(outfile)

        @blend_mode.write(outfile)
        @file.seek(@blend_mode.num_bytes, IO::SEEK_CUR)

        export_mask_data(outfile)
        export_blending_ranges(outfile)
        export_legacy_layer_name(outfile)
        export_extra_data(outfile)

        outfile.write @file.read(end_of_section - @file.tell)
      end

      def write_vector_mask(outfile)
        outfile.write @file.read(8)
        # outfile.write_int 3
        # outfile.write_int @vector_tag

        @path_components.each{ |pc| pc.write(outfile); @file.seek(26, IO::SEEK_CUR) }
      end
    end
  end
end