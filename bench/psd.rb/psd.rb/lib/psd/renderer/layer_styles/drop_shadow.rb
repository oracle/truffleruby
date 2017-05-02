class PSD
  class LayerStyles
    # Not ready yet.
    class DropShadow
      def self.should_apply?(data)
        #data.has_key?('DrSh')
        false
      end

      def initialize(styles)
      end

      def apply!
      end

      private

      def apply_drop_shadow

      end

      def drop_shadow
        data['DrSh']
      end

      def drop_shadow_blend_mode
        drop_shadow['Md  ']
      end

      def drop_shadow_opacity
        drop_shadow['Opct'][:value]
      end

      def drop_shadow_light_angle
        drop_shadow['lagl'][:value]
      end

      def drop_shadow_use_global_light?
        drop_shadow['uglg']
      end

      def drop_shadow_distance
        drop_shadow['Dstn'][:value]
      end

      def drop_shadow_spread
        drop_shadow['Ckmt'][:value]
      end

      def drop_shadow_size
        drop_shadow['blur'][:value]
      end

      def drop_shadow_noise
        drop_shadow['Nose'][:value]
      end

      def drop_shadow_antialiased?
        drop_shadow['AntA']
      end

      def drop_shadow_contour
        drop_shadow['TrnS']['Nm  ']
      end

      def drop_shadow_contour_curve
        drop_shadow['TrnS']['Crv ']
      end

      def drop_shadow_knock_out?
        drop_shadow['layerConceals']
      end
    end
  end
end