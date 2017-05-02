require 'ruby-prof'
require './lib/psd'



file = ARGV[0] || 'examples/images/example.psd'
psd = PSD.new(file, parse_image: true)

result = RubyProf.profile do
  psd.parse!
end

printer = RubyProf::GraphHtmlPrinter.new(result)
printer.print(File.new("./profile.html", 'w'), min_percent: 2)
`open profile.html`