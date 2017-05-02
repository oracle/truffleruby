require './lib/psd'
require 'pp'

file = ARGV[0] || 'examples/images/example.psd'
psd = PSD.new(file)
psd.parse!

pp psd.tree.to_hash