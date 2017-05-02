require_relative '../layer_info'

class PSD
  class UnicodeName < LayerInfo
    @key = 'luni'

    def parse
      pos = @file.tell
      @data = @file.read_unicode_string

      # The name seems to be padded with null bytes. This is the easiest solution.
      @file.seek pos + @length

      return self
    end
  end
end