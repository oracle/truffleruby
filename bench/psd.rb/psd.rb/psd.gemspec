# -*- encoding: utf-8 -*-
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'psd/version'

Gem::Specification.new do |gem|
  gem.name          = "psd"
  gem.version       = PSD::VERSION
  gem.authors       = ["Ryan LeFevre", "Kelly Sutton"]
  gem.email         = ["ryan@layervault.com", "kelly@layervault.com"]
  gem.description   = %q{Parse Photoshop PSD files with ease}
  gem.summary       = %q{General purpose library for parsing Photoshop files}
  gem.homepage      = "http://cosmos.layervault.com/psdrb.html"
  gem.license       = 'MIT'

  gem.files         = `git ls-files`.split($/).delete_if { |f| f.include?('examples/') }
  gem.executables   = gem.files.grep(%r{^bin/}).map{ |f| File.basename(f) }
  gem.test_files    = gem.files.grep(%r{^(test|spec|features)/})
  gem.require_paths = ["lib"]

  gem.add_dependency 'rake'
  gem.add_dependency 'bindata'
  gem.add_dependency 'psd-enginedata', '~> 1.0'

  gem.add_dependency 'chunky_png'

  gem.test_files = Dir.glob("spec/**/*")
  gem.add_development_dependency 'rspec'
  gem.add_development_dependency 'guard'
  gem.add_development_dependency 'guard-rspec'
  gem.add_development_dependency 'rb-fsevent', '~> 0.9'
end
