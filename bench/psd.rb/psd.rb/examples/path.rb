require './lib/psd'
require 'pp'

psd = PSD.new('examples/images/example.psd')
psd.parse!

pp psd.tree.children_at_path("Version D/Version E/Version F").first.to_hash