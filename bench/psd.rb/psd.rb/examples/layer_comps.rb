require 'benchmark'
require 'pp'
require 'psd'

file = ARGV[0] || 'examples/images/example.psd'

results = Benchmark.measure "Image exporting" do
  psd = PSD.new(file, parse_layer_images: true)
  psd.parse!

  psd.layer_comps.each do |comp|
    puts "Saving #{comp[:name]} - #{comp[:id]}"
    psd.tree
      .filter_by_comp(comp[:id])
      .save_as_png("./#{comp[:name]}.png")
  end
end

puts Benchmark::CAPTION
puts results.to_s