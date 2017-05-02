require_relative '../layer_info'

class PSD
  # Not 100% sure what the purpose of this key is, but it seems to exist
  # whenever the lsct key doesn't. Parsing this like a layer section
  # divider seems to solve a lot of parsing issues with folders.
  #
  # See https://github.com/layervault/psd.rb/issues/38
  class NestedLayerDivider < LayerInfo
    @key = 'lsdk'

    attr_reader :is_folder, :is_hidden

    def initialize(file, length)
      super

      @is_folder = false
      @is_hidden = false
    end

    def parse
      code = @file.read_int

      case code
      when 1, 2 then @is_folder = true
      when 3 then @is_hidden = true
      end
    end
  end
end