require 'benchmark'
require './lib/psd'

file = ARGV[0] || 'examples/images/example.psd'

results = Benchmark.measure "Image exporting" do
  psd = PSD.new(file)
  psd.image.save_as_png 'output.png'
end

puts Benchmark::CAPTION
puts results.to_s