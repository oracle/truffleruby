# -*- encoding: utf-8 -*-
# stub: stringio 0.0.2 ruby lib
# stub: ext/stringio/extconf.rb

Gem::Specification.new do |s|
  s.name = "stringio".freeze
  s.version = "0.0.2"

  s.required_rubygems_version = Gem::Requirement.new(">= 2.6".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Nobu Nakada".freeze]
  s.date = "2019-02-01"
  s.description = "Pseudo `IO` class from/to `String`.".freeze
  s.email = "nobu@ruby-lang.org".freeze
  s.extensions = ["ext/stringio/extconf.rb".freeze]
  s.files = ["ext/stringio/extconf.rb".freeze, "stringio.bundle".freeze]
  s.homepage = "https://github.com/ruby/stringio".freeze
  s.licenses = ["BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.2".freeze)
  s.rubygems_version = "3.0.1".freeze
  s.summary = "Pseudo IO on String".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<rake-compiler>.freeze, [">= 0"])
    else
      s.add_dependency(%q<rake-compiler>.freeze, [">= 0"])
    end
  else
    s.add_dependency(%q<rake-compiler>.freeze, [">= 0"])
  end
end
