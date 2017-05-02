class PSD
  class Layer
    module Name
      # Gets the name of this layer. If the PSD file is from an even remotely
      # recent version of Photoshop, this data is stored as extra layer info and
      # as a UTF-16 name. Otherwise, it's stored in a legacy block.
      def name
        if @adjustments.has_key?(:name)
          return @adjustments[:name].data
        end

        return @legacy_name
      end

      private

      # The old school layer names are encoded in MacRoman format,
      # not UTF-8. Luckily Ruby kicks ass at character conversion.
      def parse_legacy_layer_name
        @legacy_name_start = @file.tell

        len = Util.pad4 @file.read(1).bytes.to_a[0]
        @legacy_name = @file.read_string(len)

        @legacy_name_end = @file.tell
      end

      def export_legacy_layer_name(outfile)
        outfile.write @file.read(@legacy_name_end - @legacy_name_start)
      end
    end
  end
end