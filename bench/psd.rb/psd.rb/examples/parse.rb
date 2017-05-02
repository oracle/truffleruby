require 'benchmark'
require './lib/psd'

psd = nil
file = ARGV[0] || 'examples/images/example.psd'
results = Benchmark.measure "PSD parsing" do
  psd = PSD.new(file)
  psd.parse!
end

if psd.resources[:layer_comps]
  puts "\nLayer Comps:\n===================="
  puts psd.resources[:layer_comps].data.names
end

puts "\nVisible Layers:\n===================="
psd.layers.each do |layer|
  next if layer.folder? || layer.hidden?

  puts "Name: #{layer.name}"
  puts "Position: top = #{layer.top}, left = #{layer.left}"
  puts "Size: width = #{layer.width}, height = #{layer.height}"
  puts "Mask: width = #{layer.mask.width}, height = #{layer.mask.height}"
  puts "Reference point: #{layer.reference_point.x}, #{layer.reference_point.y}"

  puts ""
end

puts "\nPSD Info:\n===================="
puts "#{psd.width}x#{psd.height} #{psd.header.mode_name}"
puts "#{psd.resources.data.size} resources parsed"
puts "#{psd.layers.size} layers, #{psd.folders.size} folders"

puts "\nBenchmark Results (seconds):\n===================="
puts " "*7 + Benchmark::CAPTION
puts "parse: " + results.to_s