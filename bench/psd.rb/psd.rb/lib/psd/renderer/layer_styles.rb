require_relative 'layer_styles/color_overlay'
require_relative 'layer_styles/drop_shadow'

class PSD
  class LayerStyles
    # Blend modes in layer effects use different keys
    # than normal layer blend modes. Thanks Adobe.
    BLEND_TRANSLATION = {
      'Nrml' => 'norm',
      'Dslv' => 'diss',
      'Drkn' => 'dark',
      'Mltp' => 'mul',
      'CBrn' => 'idiv',
      'linearBurn' => 'lbrn',
      'Lghn' => 'lite',
      'Scrn' => 'scrn',
      'CDdg' => 'div',
      'linearDodge' => 'lddg',
      'Ovrl' => 'over',
      'SftL' => 'sLit',
      'HrdL' => 'hLit',
      'vividLight' => 'vLit',
      'linearLight' => 'lLit',
      'pinLight' => 'pLit',
      'hardMix' => 'hMix',
      'Dfrn' => 'diff',
      'Xclu' => 'smud',
      'H   ' => 'hue',
      'Strt' => 'sat',
      'Clr ' => 'colr',
      'Lmns' => 'lum'
    }.freeze

    attr_reader :canvas, :node, :data

    def initialize(canvas)
      @canvas = canvas
      @node = @canvas.node
      @data = @node.layer.info[:object_effects]

      if @data.nil?
        @applied = true
      else
        @data = @data.data
        @applied = false
      end
    end

    def apply!
      return if @applied || data.nil?

      ColorOverlay.new(self).apply! if ColorOverlay.should_apply?(data)
      DropShadow.new(self).apply!   if DropShadow.should_apply?(data)
    end
  end
end