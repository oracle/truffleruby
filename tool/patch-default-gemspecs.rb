require 'rbconfig'

dlext = ".#{RbConfig::CONFIG['DLEXT']}"
replacement = '.#{Truffle::Platform::DLEXT}'

Dir.glob("lib/gems/specifications/default/*.gemspec").sort.each do |spec|
  contents = File.read(spec)
  contents = contents.gsub(dlext, replacement)
  File.write(spec, contents)
end
