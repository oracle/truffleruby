class PSD
  # Definition for a single Resource record.
  #
  # Most of the resources are options/preferences set by the user
  # or automatically by Photoshop.
  class Resource
    attr_reader :type, :id, :name, :size
    attr_accessor :data

    def initialize(file)
      @file = file
      @data = {}
      @type = nil
    end

    def parse
      @type = @file.read_string(4) # Always 8BIM
      @id = @file.read_short

      name_length = Util.pad2(@file.read(1).bytes.to_a[0] + 1) - 1
      @name = @file.read_string(name_length)

      @size = Util.pad2(@file.read_int)
    end
  end
end