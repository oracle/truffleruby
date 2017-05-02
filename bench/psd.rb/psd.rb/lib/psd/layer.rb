class PSD
  # Represents a single layer and all of the data associated with
  # that layer.
  class Layer
    include Section
    include BlendModes
    include BlendingRanges
    include ChannelImage
    include Exporting
    include Helpers
    include Info
    include Mask
    include Name
    include PathComponents
    include PositionAndChannels

    attr_reader :id, :info_keys, :header
    attr_accessor :group_layer, :node, :file

    # Initializes all of the defaults for the layer.
    def initialize(file, header)
      @file = file
      @header = header

      @mask = {}
      @blending_ranges = {}
      @adjustments = {}
      @channels_info = []
      @blend_mode = {}
      @group_layer = nil

      @blending_mode = 'normal'
      @opacity = 255

      # Just used for tracking which layer adjustments we're parsing.
      # Not essential.
      @info_keys = []
    end

    # Parse the layer and all of it's sub-sections.
    def parse(index=nil)
      start_section

      @id = index

      parse_position_and_channels
      parse_blend_modes

      extra_len = @file.read_int
      @layer_end = @file.tell + extra_len

      parse_mask_data
      parse_blending_ranges
      parse_legacy_layer_name
      parse_layer_info

      PSD.logger.debug "Layer name = #{name}"

      @file.seek @layer_end # Skip over any filler zeros

      end_section
      return self
    end

    # We just delegate this to a normal method call.
    def [](val)
      self.send(val)
    end

    # We delegate all missing method calls to the extra layer info to make it easier
    # to access that data.
    def method_missing(method, *args, &block)
      return @adjustments[method] if @adjustments.has_key?(method)
      super
    end
  end
end