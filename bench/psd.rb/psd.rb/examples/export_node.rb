require './lib/psd'

psd = PSD.new('examples/images/example.psd')
psd.parse!


psd.export_node psd.tree.children.last.children[1], "asset.psd"