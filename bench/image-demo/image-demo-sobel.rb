require_relative 'lib/sobel'

width = 240
height = 180
image = NoBorderImagePadded.new(width, height)

benchmark do
  sobel_magnitude_uint8(image)
end
