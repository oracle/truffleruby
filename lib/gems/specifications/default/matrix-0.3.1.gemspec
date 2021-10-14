# -*- encoding: utf-8 -*-
# stub: matrix 0.3.1 ruby lib

Gem::Specification.new do |s|
  s.name = "matrix".freeze
  s.version = "0.3.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Marc-Andre Lafortune".freeze]
  s.bindir = "exe".freeze
  s.date = "2021-10-13"
  s.description = "An implementation of Matrix and Vector classes.".freeze
  s.email = ["ruby-core@marc-andre.ca".freeze]
  s.files = ["matrix.rb".freeze, "matrix/eigenvalue_decomposition.rb".freeze, "matrix/lup_decomposition.rb".freeze, "matrix/version.rb".freeze]
  s.homepage = "https://github.com/ruby/matrix".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.5.0".freeze)
  s.rubygems_version = "3.2.22".freeze
  s.summary = "An implementation of Matrix and Vector classes.".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_development_dependency(%q<rake>.freeze, [">= 0"])
  else
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
  end
end
