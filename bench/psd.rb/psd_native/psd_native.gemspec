# coding: utf-8
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'psd_native/version'

Gem::Specification.new do |spec|
  spec.name          = "psd_native"
  spec.version       = PSDNative::VERSION
  spec.authors       = ["Ryan LeFevre"]
  spec.email         = ["ryan@layervault.com"]
  spec.description   = %q{Native mixins to speed up PSD.rb}
  spec.summary       = %q{Native C mixins to speed up the slowest parts of PSD.rb}
  spec.homepage      = "http://cosmos.layervault.com/psdrb.html"
  spec.license       = "MIT"

  spec.files         = `git ls-files`.split($/)
  spec.executables   = spec.files.grep(%r{^bin/}) { |f| File.basename(f) }
  spec.test_files    = spec.files.grep(%r{^(test|spec|features)/})

  spec.platform      = Gem::Platform::RUBY
  spec.extensions    = ["ext/psd_native/extconf.rb"]
  spec.require_paths = ["lib", "ext"]

  spec.add_runtime_dependency "psd", ">= 2.1.1"
  spec.add_runtime_dependency "oily_png", "~> 1.1"

  spec.add_development_dependency "bundler", "~> 1.3"
  spec.add_development_dependency "rake"
  spec.add_development_dependency "rake-compiler", "~> 0.9"

  spec.test_files = Dir.glob("spec/**/*")
  spec.add_development_dependency 'rspec', "~> 2.14"
  spec.add_development_dependency 'guard'
  spec.add_development_dependency 'guard-rspec'
end