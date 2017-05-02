class PSD
  class Resource
    class Section
      class LayerComps < Section
        def self.id; 1065; end
        def self.name; :layer_comps; end

        def self.visibility_captured?(comp)
          comp[:captured_info] & 0b001 > 0
        end

        def self.position_captured?(comp)
          comp[:captured_info] & 0b010 > 0
        end

        def self.appearance_captured?(comp)
          comp[:captured_info] & 0b100 > 0
        end

        def parse
          # Descriptor version
          @file.seek 4, IO::SEEK_CUR
          
          @data = Descriptor.new(@file).parse
          @resource.data = self
        end

        def names
          @data['list'].map { |c| c['Nm  '] }
        end

        def [](val)
          @data[val]
        end

        def to_a
          @data['list'].map { |c| {id: c['compID'], name: c['Nm  '], captured_info: c['capturedInfo']} }
        end
      end
    end
  end
end