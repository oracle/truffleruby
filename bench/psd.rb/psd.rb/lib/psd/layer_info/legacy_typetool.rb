require_relative '../layer_info'
require_relative 'typetool'

class PSD
  class LegacyTypeTool < TypeTool
    @key = 'tySh'
    
    def parse      
      version = @file.read_short
      parse_transform_info

      # Font Information
      version = @file.read_short

      faces_count = @file.read_short
      @data[:face] = []

      faces_count.times do |i|
        @data[:face][i] = {}
        @data[:face][i][:mark] = @file.read_short
        @data[:face][i][:font_type] = @file.read_int
        @data[:face][i][:font_name] = @file.read_string
        @data[:face][i][:font_family_name] = @file.read_string
        @data[:face][i][:font_style_name] = @file.read_string
        @data[:face][i][:script] = @file.read_short
        @data[:face][i][:number_axes_vector] = @file.read_int
        @data[:face][i][:vector] = []

        @data[:face][i][:number_axes_vector].times do |j|
          @data[:face][i][:vector] << @file.read_int
        end
      end

      # Style Information
      styles_count = @file.read_short
      @data[:style] = []

      styles_count.times do |i|
        @data[:style][i] = {}
        @data[:style][i][:mark] = @file.read_short
        @data[:style][i][:face_mark] = @file.read_short
        @data[:style][i][:size] = @file.read_int
        @data[:style][i][:tracking] = @file.read_int
        @data[:style][i][:kerning] = @file.read_int
        @data[:style][i][:leading] = @file.read_int
        @data[:style][i][:base_shift] = @file.read_int
        @data[:style][i][:auto_kern] = @file.read_boolean

        # Bleh
        @file.read 1

        @data[:style][i][:rotate] = @file.read_boolean
      end

      # Text information
      @data[:type] = @file.read_short
      @data[:scaling_factor] = @file.read_int
      @data[:character_count] = @file.read_int
      @data[:horz_place] = @file.read_int
      @data[:vert_place] = @file.read_int
      @data[:select_start] = @file.read_int
      @data[:select_end] = @file.read_int

      lines_count = @file.read_short
      @data[:line] = []

      lines_count.times do |i|
        @data[:line][i] = {}
        @data[:line][i][:char_count] = @file.read_int
        @data[:line][i][:orientation] = @file.read_short
        @data[:line][i][:alignment] = @file.read_short
        @data[:line][i][:actual_char] = @file.read_short
        @data[:line][i][:style] = @file.read_short
      end

      # Color information
      @data[:color] = @file.read_space_color
      @data[:antialias] = @file.read_boolean

      return self
    end

    # Not sure where this is stored right now
    def text_value
      ""
    end
  end
end