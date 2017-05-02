class PSD
  # Helper that lets us track the beginning and ending locations
  # of each section. This is for debug and error catching purposes,
  # primarily.
  module Section
    attr_reader :section_start, :section_end

    def start_section(section=:all)
      @section_start = {} unless @section_start
      @section_start[section] = @file.tell
    end

    def end_section(section=:all)
      @section_end = {} unless @section_end
      @section_end[section] = @file.tell
    end

    def start_of_section(section=:all)
      @section_start[section]
    end

    def end_of_section(section=:all)
      @section_end[section]
    end
  end
end