class PSD
  class Layer
    module BlendModes
      attr_reader :blend_mode, :opacity

      def blending_mode
        if !info[:section_divider].nil? && info[:section_divider].blend_mode
          BlendMode::BLEND_MODES[info[:section_divider].blend_mode.strip.to_sym]
        else
          @blending_mode
        end
      end

      # Is the layer below this one a clipping mask?
      def clipped?
        @blend_mode.clipping == 1
      end

      private
      
      def parse_blend_modes
        @blend_mode = BlendMode.read(@file)

        @blending_mode = @blend_mode.mode
        @opacity = @blend_mode.opacity
        @visible = @blend_mode.visible
      end
    end
  end
end