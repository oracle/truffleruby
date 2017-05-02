class PSD
  # Parses and reads all of the Resource records in the document.
  class Resources
    include Section

    attr_reader :resources
    alias :data :resources
    
    def initialize(file)
      @file = file
      @resources = {}
      @type_index = {}
      @length = nil
    end

    # Parses each Resource and stores them.
    def parse
      start_section

      n = length
      start = @file.tell

      while n > 0
        pos = @file.tell

        resource = Resource.new(@file)
        resource.parse

        resource_end = @file.tell + resource.size

        name = Resource::Section.factory(@file, resource)
        @resources[resource.id] = resource
        @type_index[name] = resource.id unless name.nil?

        @file.seek resource_end
        n -= @file.tell - pos
      end

      unless n == 0
        @file.seek start + length
      end

      end_section
    end

    def skip
      @file.seek length, IO::SEEK_CUR
    end

    def [](id)
      if id.is_a?(Symbol)
        by_type(id)
      else
        @resources[id]
      end
    end

    def by_type(id)
      @resources[@type_index[id]]
    end

    private

    def length
      return @length unless @length.nil?
      @length = @file.read_int
    end
  end
end