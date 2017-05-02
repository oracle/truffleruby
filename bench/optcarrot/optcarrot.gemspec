# coding: utf-8
lib = File.expand_path("../lib", __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require "optcarrot"

Gem::Specification.new do |spec|
  spec.name          = "optcarrot"
  spec.version       = Optcarrot::VERSION
  spec.authors       = ["Yusuke Endoh"]
  spec.email         = ["mame@ruby-lang.org"]

  spec.summary       = "A NES emulator written in Ruby."
  spec.description   =
    'An "enjoyable" benchmark for Ruby implementation.  The goal of this project is to drive Ruby3x3.'
  spec.homepage      = "https://github.com/mame/optcarrot"
  spec.license       = "MIT"

  spec.files         = `git ls-files -z`.split("\x0").reject {|f| f.match(%r{^tmp/|^tools/|^examples/|^\.}) }
  spec.bindir        = "bin"
  spec.executables   = ["optcarrot"]
  spec.require_paths = ["lib"]

  spec.add_runtime_dependency "ffi", "~> 1.9"
  spec.add_development_dependency "bundler", "~> 1.11"
  spec.add_development_dependency "rake", "~> 10.0"
  spec.add_development_dependency "stackprof", "~> 0.2"
end
