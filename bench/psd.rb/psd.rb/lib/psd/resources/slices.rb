class PSD
  class Resource
    class Section
      class Slices < Section
        attr_reader :data, :version

        def self.id; 1050; end
        def self.name; :slices; end

        def parse
          @version = @file.read_int

          case @version
          when 6 then parse_legacy
          when 7, 8 then
            descriptor_version = @file.read_int
            @data = Descriptor.new(@file).parse
          end

          normalize_data!

          @resource.data = self
        end

        def to_a
          return [] if @data.nil?
          @data[:slices]
        end

        private

        def parse_legacy
          @data = {}

          @data[:bounds] = {}.tap do |bounds|
            bounds[:top] = @file.read_int
            bounds[:left] = @file.read_int
            bounds[:bottom] = @file.read_int
            bounds[:right] = @file.read_int
          end

          @data[:name] = @file.read_unicode_string

          @data[:slices] = []
          slices_count = @file.read_int
          slices_count.times do
            @data[:slices] << {}.tap do |slice|
              slice[:id] = @file.read_int
              slice[:group_id] = @file.read_int
              slice[:origin] = @file.read_int

              slice[:associated_layer_id] = (slice[:origin] == 1 ? @file.read_int : nil)

              slice[:name] = @file.read_unicode_string
              slice[:type] = @file.read_int

              slice[:bounds] = {}.tap do |bounds|
                bounds[:left] = @file.read_int
                bounds[:top] = @file.read_int
                bounds[:right] = @file.read_int
                bounds[:bottom] = @file.read_int
              end

              slice[:url] = @file.read_unicode_string
              slice[:target] = @file.read_unicode_string
              slice[:message] = @file.read_unicode_string
              slice[:alt] = @file.read_unicode_string

              slice[:cell_text_is_html] = @file.read_boolean
              slice[:cell_text] = @file.read_unicode_string

              slice[:horizontal_alignment] = @file.read_int
              slice[:vertical_alignment] = @file.read_int

              a, r, g, b = 4.times.map { @file.read_byte }
              slice[:color] = ChunkyPNG::Color.rgba(r, g, b, a)
            end
          end
        end

        # Normalizes the data between version 6 and versions 7/8 to make it easier
        # to deal with the discrepancies. We use version 6 as the base.
        def normalize_data!
          return if @version == 6
          data = {}
          data[:bounds] = {
            top: @data['bounds']['Top '],
            left: @data['bounds']['Left'],
            bottom: @data['bounds']['Btom'],
            right: @data['bounds']['Rght']
          }

          data[:name] = @data['baseName']

          data[:slices] = @data['slices'].map do |slice|
            {
              id: slice['sliceID'],
              group_id: slice['groupID'],
              origin: slice['origin'],
              associated_layer_id: nil,
              name: '',
              type: slice['Type'],
              bounds: {
                left: slice['bounds']['Left'],
                top: slice['bounds']['Top '],
                right: slice['bounds']['Rght'],
                bottom: slice['bounds']['Btom']
              },
              url: slice['url'],
              target: '',
              message: slice['Msge'],
              alt: slice['altTag'],
              cell_text_is_html: slice['cellTextIsHTML'],
              cell_text: slice['cellText'],
              horizontal_alignment: slice['horzAlign'],
              vertical_alignment: slice['vertAlign'],
              color: nil,
              outset: {
                top: slice['topOutset'],
                left: slice['leftOutset'],
                bottom: slice['bottomOutset'],
                right: slice['rightOutset']
              }
            }
          end

          @data = data
        end
      end
    end
  end
end