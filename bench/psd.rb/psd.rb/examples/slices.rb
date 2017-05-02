require 'benchmark'
require './lib/psd'

require 'pp'

psd = nil
file = ARGV[0] || 'examples/images/example.psd'
results = Benchmark.measure "PSD parsing" do
  psd = PSD.new(file)
  psd.parse!
end

if psd.resources[:slices]
  psd.resources[:slices].data.to_a.each do |slice|
    pp slice
  end
end