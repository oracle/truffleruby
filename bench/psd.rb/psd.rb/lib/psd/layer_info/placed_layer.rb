require_relative '../layer_info'

class PSD
  class PlacedLayer < LayerInfo
    @key = 'SoLd'

    def parse
      # Useless id/version info
      @file.seek 12, IO::SEEK_CUR
      @data = Descriptor.new(@file).parse
    end
  end
end