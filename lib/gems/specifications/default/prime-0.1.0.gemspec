# -*- encoding: utf-8 -*-
# stub: prime 0.1.0 ruby lib

Gem::Specification.new do |s|
  s.name = "prime".freeze
  s.version = "0.1.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Yuki Sonoda".freeze]
  s.bindir = "exe".freeze
  s.date = "2019-02-01"
  s.description = "Prime numbers and factorization library.".freeze
  s.email = ["yugui@yugui.jp".freeze]
  s.files = ["prime.rb".freeze]
  s.homepage = "https://github.com/ruby/prime".freeze
  s.licenses = ["BSD-2-Clause".freeze]
  s.rubygems_version = "3.0.1".freeze
  s.summary = "Prime numbers and factorization library.".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_development_dependency(%q<rake>.freeze, [">= 0"])
      s.add_development_dependency(%q<test-unit>.freeze, [">= 0"])
    else
      s.add_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_dependency(%q<rake>.freeze, [">= 0"])
      s.add_dependency(%q<test-unit>.freeze, [">= 0"])
    end
  else
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
    s.add_dependency(%q<test-unit>.freeze, [">= 0"])
  end
end
