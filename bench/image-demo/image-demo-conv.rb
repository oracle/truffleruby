require_relative 'lib/noborder'

width = 240
height = 180
image = NoBorderImagePadded.new(width, height)
conv = NoBorderImagePadded.new(3, 3)

benchmark do
  conv3x3(image, conv)
end
