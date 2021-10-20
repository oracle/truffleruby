# -*- encoding: utf-8 -*-
# stub: stringio 3.0.0 ruby lib
# stub: ext/stringio/extconf.rb

Gem::Specification.new do |s|
  s.name = "stringio".freeze
  s.version = "3.0.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 2.6".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Nobu Nakada".freeze]
  s.date = "2021-10-13"
  s.description = "Pseudo `IO` class from/to `String`.".freeze
  s.email = "nobu@ruby-lang.org".freeze
  s.extensions = ["ext/stringio/extconf.rb".freeze]
  s.files = ["ext/stringio/extconf.rb".freeze, "stringio.#{Truffle::Platform::DLEXT}".freeze]
  s.homepage = "https://github.com/ruby/stringio".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.5".freeze)
  s.rubygems_version = "3.2.22".freeze
  s.summary = "Pseudo IO on String".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_development_dependency(%q<rake-compiler>.freeze, [">= 0"])
    s.add_development_dependency(%q<test-unit>.freeze, [">= 0"])
  else
    s.add_dependency(%q<rake-compiler>.freeze, [">= 0"])
    s.add_dependency(%q<test-unit>.freeze, [">= 0"])
  end
end
