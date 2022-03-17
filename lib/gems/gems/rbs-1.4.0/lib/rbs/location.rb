module RBS
  class Location
    attr_reader :buffer
    attr_reader :start_pos
    attr_reader :end_pos

    def initialize(buffer:, start_pos:, end_pos:)
      @buffer = buffer
      @start_pos = start_pos
      @end_pos = end_pos
    end

    def inspect
      "#<#{self.class}:#{self.__id__} @buffer=#{buffer.name}, @pos=#{start_pos}...#{end_pos}, source='#{source.lines.first}', start_line=#{start_line}, start_column=#{start_column}>"
    end

    def name
      buffer.name
    end

    def start_line
      start_loc[0]
    end

    def start_column
      start_loc[1]
    end

    def end_line
      end_loc[0]
    end

    def end_column
      end_loc[1]
    end

    def start_loc
      @start_loc ||= buffer.pos_to_loc(start_pos)
    end

    def end_loc
      @end_loc ||= buffer.pos_to_loc(end_pos)
    end

    def range
      start_pos...end_pos
    end

    def source
      @source ||= buffer.content[start_pos...end_pos] or raise
    end

    def to_s
      "#{name || "-"}:#{start_line}:#{start_column}...#{end_line}:#{end_column}"
    end

    def self.to_string(location, default: "*:*:*...*:*")
      location&.to_s || default
    end

    def ==(other)
      other.is_a?(Location) &&
        other.buffer == buffer &&
        other.start_pos == start_pos &&
        other.end_pos == end_pos
    end

    def +(other)
      if other
        raise "Invalid concat: buffer=#{buffer.name}, other.buffer=#{other.buffer.name}" unless other.buffer == buffer

        self.class.new(buffer: buffer,
                       start_pos: start_pos,
                       end_pos: other.end_pos)
      else
        self
      end
    end

    def concat(*others)
      others.each { |other| self << other }
      self
    end

    def <<(other)
      if other
        raise "Invalid concat: buffer=#{buffer.name}, other.buffer=#{other.buffer.name}" unless other.buffer == buffer
        @end_pos = other.end_pos
        @source = nil
        @end_loc = nil
      end
      self
    end

    def pred?(loc)
      loc.is_a?(Location) &&
        loc.name == name &&
        loc.start_pos == end_pos
    end

    def to_json(state = _ = nil)
      {
        start: {
          line: start_line,
          column: start_column
        },
        end: {
          line: end_line,
          column: end_column
        },
        buffer: {
          name: name&.to_s
        }
      }.to_json(state)
    end

    def with_children(required: {}, optional: {})
      # @type var required: Hash[Symbol, Range[Integer] | Location]
      # @type var optional: Hash[Symbol, Range[Integer] | Location | nil]

      this = WithChildren.new(buffer: buffer, start_pos: start_pos, end_pos: end_pos)

      req = required.transform_values do |value|
        case value
        when Location
          value.range
        else
          value
        end
      end

      opt = optional.transform_values do |value|
        case value
        when Location
          value.range
        else
          value
        end
      end

      this.required_children.merge!(req)
      this.optional_children.merge!(opt)

      this
    end

    class WithChildren < Location
      attr_reader :required_children, :optional_children

      def initialize(buffer:, start_pos:, end_pos:)
        super(buffer: buffer, start_pos: start_pos, end_pos: end_pos)

        @optional_children = {}
        @required_children = {}
      end

      def initialize_copy(from)
        required_children.merge!(from.required_children)
        optional_children.merge!(from.optional_children)
        self
      end

      def [](key)
        case
        when required_children.key?(_ = key)
          range = required_children[_ = key]
          Location.new(buffer: buffer, start_pos: range.begin, end_pos: range.end)
        when optional_children.key?(_ = key)
          range = required_children[_ = key] || optional_children[_ = key]
          if range
            Location.new(buffer: buffer, start_pos: range.begin, end_pos: range.end)
          end
        else
          raise "Unknown key given: `#{key}`"
        end
      end

      def merge_required(hash)
        this = dup

        h = hash.transform_values do |value|
          case value
          when Range
            value
          when Location
            value.range
          else
            raise
          end
        end

        this.required_children.merge!(h)

        this
      end

      def merge_optional(hash)
        this = dup

        h = hash.transform_values do |value|
          case value
          when Range
            value
          when Location
            value.range
          else
            nil
          end
        end

        this.optional_children.merge!(h)

        this
      end
    end
  end
end
