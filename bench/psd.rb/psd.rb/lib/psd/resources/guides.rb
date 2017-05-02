class PSD
  class Resource
    class Section
      class Guides < Section
        def self.id; 1032; end
        def self.name; :guides; end

        def parse
          # Descriptor version
          @file.seek 4, IO::SEEK_CUR

          # Future implementation of document-specific grids
          @file.seek 8, IO::SEEK_CUR

          num_guides = @file.read_int

          @data = []

          num_guides.times do
            location = @file.read_int / 32
            direction = @file.read_byte == 0 ? "vertical" : "horizontal"

            @data.push({ :location => location, :direction => direction })
          end

          @resource.data = self
        end

        def to_a
          @data
        end
      end
    end
  end
end