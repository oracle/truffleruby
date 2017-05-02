class PSD
  class Layer
    module BlendingRanges
      attr_reader :blending_ranges

      private
      
      def parse_blending_ranges
        length = @file.read_int

        # Composite gray blend. Contains 2 black values followed by 2 white values. 
        # Present but irrelevant for Lab & Grayscale.
        @blending_ranges[:grey] = {
          source: {
            black: [@file.read_byte, @file.read_byte],
            white: [@file.read_byte, @file.read_byte]
          },
          dest: {
            black: [@file.read_byte, @file.read_byte],
            white: [@file.read_byte, @file.read_byte]
          }
        }

        @blending_ranges[:num_channels] = (length - 8) / 8

        @blending_ranges[:channels] = []
        @blending_ranges[:num_channels].times do
          @blending_ranges[:channels] << {
            source: {
              black: [@file.read_byte, @file.read_byte],
              white: [@file.read_byte, @file.read_byte]
            },
            dest: {
              black: [@file.read_byte, @file.read_byte],
              white: [@file.read_byte, @file.read_byte]
            }
          }
        end
      end

      def export_blending_ranges(outfile)
        length = 4 * 2 # greys
        length += @blending_ranges[:num_channels] * 8
        outfile.write_int length

        outfile.write @blending_ranges[:grey][:source][:black].pack('CC')
        outfile.write @blending_ranges[:grey][:source][:white].pack('CC')
        outfile.write @blending_ranges[:grey][:dest][:black].pack('CC')
        outfile.write @blending_ranges[:grey][:dest][:white].pack('CC')

        @blending_ranges[:num_channels].times do |i|
          outfile.write @blending_ranges[:channels][i][:source][:black].pack('CC')
          outfile.write @blending_ranges[:channels][i][:source][:white].pack('CC')
          outfile.write @blending_ranges[:channels][i][:dest][:black].pack('CC')
          outfile.write @blending_ranges[:channels][i][:dest][:white].pack('CC')
        end

        @file.seek length + 4, IO::SEEK_CUR
      end
    end
  end
end