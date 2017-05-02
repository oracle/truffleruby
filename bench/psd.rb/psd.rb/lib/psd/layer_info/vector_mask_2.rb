require_relative 'vector_mask'

class PSD
  # Identical to VectorMask, except with a different key. This
  # exists in Photoshop >= CS6. If this key exists, then there
  # is also a vscg key.
  class VectorMask2 < VectorMask
    @key = 'vsms'
  end
end