class PSD
  # A descriptor is a block of data that describes a complex data structure of some kind.
  # It was added sometime around Photoshop 5.0 and it superceded a few legacy things such
  # as layer names and type data.
  class Descriptor
    # Store a reference to our file and initialize our data structure.
    def initialize(file)
      @file = file
      @data = {}
    end

    # Parse the descriptor. Descriptors always start with a class identifier, followed by
    # a variable number of items in the descriptor. We return the Hash that represents
    # the full data structure.
    def parse
      PSD.logger.debug "Descriptor: pos = #{@file.tell}"

      @data[:class] = parse_class
      num_items = @file.read_int

      PSD.logger.debug "Class = #{@data[:class]}, Item count = #{num_items}"

      num_items.times do |i|
        id, value = parse_key_item
        @data[id] = value
      end

      @data
    end

    private

    def parse_class
      {
        name: @file.read_unicode_string,
        id: parse_id
      }
    end

    def parse_id
      len = @file.read_int
      len == 0 ? @file.read_string(4) : @file.read_string(len)
    end

    def parse_key_item
      id = parse_id
      PSD.logger.debug "Key = #{id}"

      value = parse_item

      return id, value
    end

    def parse_item(type = nil)
      type = @file.read_string(4) if type.nil?
      PSD.logger.debug "Type = #{type}"

      value = case type
      when 'bool'         then parse_boolean
      when 'type', 'GlbC' then parse_class
      when 'Objc', 'GlbO' then Descriptor.new(@file).parse
      when 'doub'         then parse_double
      when 'enum'         then parse_enum
      when 'alis'         then parse_alias
      when 'Pth'          then parse_file_path
      when 'long'         then parse_integer
      when 'comp'         then parse_large_integer
      when 'VlLs'         then parse_list
      when 'ObAr'         then parse_object_array
      when 'tdta'         then parse_raw_data
      when 'obj '         then parse_reference
      when 'TEXT'         then @file.read_unicode_string
      when 'UntF'         then parse_unit_double
      when 'UnFl'         then parse_unit_float
      end

      return value
    end

    def parse_boolean;  @file.read_boolean; end
    def parse_double;   @file.read_double; end
    def parse_integer;  @file.read_int; end
    def parse_large_integer; @file.read_longlong; end
    def parse_identifier; @file.read_int; end
    def parse_index; @file.read_int; end
    def parse_offset; @file.read_int; end
    def parse_property; parse_id; end

    # Discard the first ID becasue it's the same as the key
    # parsed from the Key/Item. Also, YOLO.
    def parse_enum
      parse_id
      parse_id
    end

    def parse_alias
      len = @file.read_int
      @file.read_string len
    end

    def parse_file_path
      len = @file.read_int

      # Little-endian, because fuck the world.
      sig = @file.read_string(4)
      path_size = @file.read('l<')
      num_chars = @file.read('l<')

      path = @file.read_unicode_string(num_chars)

      {sig: sig, path: path}
    end

    def parse_list
      count = @file.read_int
      items = []

      count.times do |i|
        items << parse_item
      end

      return items
    end

    def parse_object_array
      raise NotImplementedError.new("Object array not implemented yet")
      count = @file.read_int
      items_in_obj = @file.read_int
      wat = @file.read_short
      
      puts count
      puts items_in_obj
      puts wat

      obj = {}
      count.times do |i|
        item = []
        name = @file.read_string(@file.read_int)
        puts name
        obj[name] = parse_list
      end

      return obj
    end

    def parse_raw_data
      len = @file.read_int
      @file.read(len)
    end

    def parse_reference
      form = @file.read_string(4)
      klass = parse_class

      case form
      when 'Clss' then nil
      when 'Enmr' then parse_enum
      when 'Idnt' then parse_identifier
      when 'indx' then parse_index
      when 'name' then @file.read_unicode_string
      when 'rele' then parse_offset
      when 'prop' then parse_property
      end
    end

    def parse_unit_double
      unit_id = @file.read_string(4)
      unit = case unit_id
      when '#Ang' then 'Angle'
      when '#Rsl' then 'Density'
      when '#Rlt' then 'Distance'
      when '#Nne' then 'None'
      when '#Prc' then 'Percent'
      when '#Pxl' then 'Pixels'
      when '#Mlm' then 'Millimeters'
      when '#Pnt' then 'Points'
      end

      value = @file.read_double
      {id: unit_id, unit: unit, value: value}
    end

    def parse_unit_float
      unit_id = @file.read_string(4)
      unit = case unit_id
      when '#Ang' then 'Angle'
      when '#Rsl' then 'Density'
      when '#Rlt' then 'Distance'
      when '#Nne' then 'None'
      when '#Prc' then 'Percent'
      when '#Pxl' then 'Pixels'
      when '#Mlm' then 'Millimeters'
      when '#Pnt' then 'Points'
      end

      value = @file.read_float
      {id: unit_id, unit: unit, value: value}
    end
  end
end