require 'benchmark'
require 'pp'
require 'psd'

file = ARGV[0] || '/Users/ryanlefevre/LayerVault/Turtleworks/Directory Page.psd'

results = Benchmark.measure "Image exporting" do
  psd = PSD.new(file)
  psd.parse!

  comp = psd.layer_comps[1]
  psd.tree.filter_by_comp(comp[:id]).save_as_png('./output.png')
end

puts Benchmark::CAPTION
puts results.to_s