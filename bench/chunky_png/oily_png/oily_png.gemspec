# -*- encoding: utf-8 -*-

lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'oily_png/version'

Gem::Specification.new do |s|
  s.name = 'oily_png'
  s.rubyforge_project = s.name

  # Do not change the version and date fields by hand. This will be done
  # automatically by the gem release script.
  s.version = OilyPNG::VERSION

  s.summary     = "Native mixin to speed up ChunkyPNG"
  s.description = <<-EOT
    This Ruby C extenstion defines a module that can be included into ChunkyPNG to improve its speed.
  EOT

  s.license  = 'MIT'
  s.authors  = ['Willem van Bergen']
  s.email    = ['willem@railsdoctors.com']
  s.homepage = 'http://wiki.github.com/wvanbergen/oily_png'

  s.extensions    = ["ext/oily_png/extconf.rb"]
  s.require_paths = ["lib", "ext"]

  s.add_runtime_dependency('chunky_png', '~> 1.3.0')

  s.add_development_dependency('rake')
  s.add_development_dependency('rake-compiler')
  s.add_development_dependency('rspec', '~> 2')

  s.rdoc_options << '--title' << s.name << '--main' << 'README.rdoc' << '--line-numbers' << '--inline-source'
  s.extra_rdoc_files = ['README.rdoc']

  s.files = `git ls-files`.split($/)
  s.test_files = s.files.grep(%r{^(test|spec|features)/})
end
