require 'benchmark'
require './lib/psd'

require 'pp'

psd = nil
file = ARGV[0] || 'examples/images/guides.psd'
results = Benchmark.measure "PSD parsing" do
  psd = PSD.new(file)
  psd.parse!
end

if psd.guides
  psd.guides.each do |guide|
    pp guide
  end
end