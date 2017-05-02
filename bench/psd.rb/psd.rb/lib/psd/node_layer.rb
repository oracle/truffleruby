require_relative 'node'

class PSD::Node
  class Layer < PSD::Node
    include PSD::Node::LockToOrigin

    attr_reader :layer

    # Stores a reference to the PSD::Layer
    def initialize(layer)
      @layer = layer
      layer.node = self

      super([])
    end

    # Delegates some methods to the PSD::Layer
    [:text, :ref_x, :ref_y].each do |meth|
      define_method meth do
        @layer.send(meth)
      end

      define_method "#{meth}=" do |val|
        @layer.send("#{meth}=", val)
      end
    end

    # Attempt to translate the layer.
    def translate(x=0, y=0)
      @layer.translate x, y
    end

    # Attempt to scale the path components of the layer.
    def scale_path_components(xr, yr)
      @layer.scale_path_components(xr, yr)
    end

    # Tries to hide the layer by moving it way off canvas.
    def hide!
      # TODO actually mess with the blend modes instead of
      # just putting things way off canvas
      return if @hidden_by_kelly
      translate(100000, 10000)
      @hidden_by_kelly = true
    end

    # Tries to re-show the canvas by moving it back to it's original position.
    def show!
      if @hidden_by_kelly
        translate(-100000, -10000)
        @hidden_by_kelly = false
      end
    end

    def empty?
      width == 0 || height == 0
    end

    # Exports this layer to a Hash.
    def to_hash
      super.merge({
        type: :layer,
        text: @layer.text,
        ref_x: @layer.reference_point.x,
        ref_y: @layer.reference_point.y,
        mask: @layer.mask.to_hash,
        image: {
          width: @layer.image.width,
          height: @layer.image.height,
          channels: @layer.channels_info
        }
      })
    end

    # If the method is missing, we blindly send it to the layer.
    # The layer handles the case in which the method doesn't exist.
    def method_missing(method, *args, &block)
      @layer.send(method, *args, &block)
    end
  end
end