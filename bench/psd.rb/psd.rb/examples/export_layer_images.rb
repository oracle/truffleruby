require 'fileutils'
require 'benchmark'
require './lib/psd'

file = ARGV[0] || 'examples/images/example.psd'
psd = PSD.new(file, parse_layer_images: true)

results = Benchmark.measure "Layer image exporting" do
  psd.parse!
end

psd.tree.descendant_layers.each do |layer|
  path = layer.path.split('/')[0...-1].join('/')
  FileUtils.mkdir_p("output/#{path}")
  layer.image.save_as_png "output/#{layer.path}.png"
end

puts Benchmark::CAPTION
puts results.to_s