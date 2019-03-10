# -*- encoding: utf-8 -*-
# stub: forwardable 1.2.0 ruby lib

Gem::Specification.new do |s|
  s.name = "forwardable".freeze
  s.version = "1.2.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Keiju ISHITSUKA".freeze]
  s.bindir = "exe".freeze
  s.date = "2019-02-01"
  s.description = "Provides delegation of specified methods to a designated object.".freeze
  s.email = ["keiju@ruby-lang.org".freeze]
  s.files = ["forwardable.rb".freeze, "forwardable/impl.rb".freeze]
  s.homepage = "https://github.com/ruby/forwardable".freeze
  s.licenses = ["BSD-2-Clause".freeze]
  s.rubygems_version = "3.0.1".freeze
  s.summary = "Provides delegation of specified methods to a designated object.".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_development_dependency(%q<rake>.freeze, [">= 0"])
    else
      s.add_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_dependency(%q<rake>.freeze, [">= 0"])
    end
  else
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
  end
end
