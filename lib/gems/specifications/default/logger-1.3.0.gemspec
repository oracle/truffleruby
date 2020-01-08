# -*- encoding: utf-8 -*-
# stub: logger 1.3.0 ruby lib

Gem::Specification.new do |s|
  s.name = "logger".freeze
  s.version = "1.3.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["SHIBATA Hiroshi".freeze]
  s.bindir = "exe".freeze
  s.date = "2020-01-07"
  s.description = "Provides a simple logging utility for outputting messages.".freeze
  s.email = ["hsbt@ruby-lang.org".freeze]
  s.files = ["logger.rb".freeze]
  s.homepage = "https://github.com/ruby/logger".freeze
  s.licenses = ["BSD-2-Clause".freeze]
  s.rubygems_version = "3.0.3".freeze
  s.summary = "Provides a simple logging utility for outputting messages.".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<bundler>.freeze, ["~> 1.16"])
      s.add_development_dependency(%q<rake>.freeze, ["~> 10.0"])
      s.add_development_dependency(%q<minitest>.freeze, ["~> 5.0"])
    else
      s.add_dependency(%q<bundler>.freeze, ["~> 1.16"])
      s.add_dependency(%q<rake>.freeze, ["~> 10.0"])
      s.add_dependency(%q<minitest>.freeze, ["~> 5.0"])
    end
  else
    s.add_dependency(%q<bundler>.freeze, ["~> 1.16"])
    s.add_dependency(%q<rake>.freeze, ["~> 10.0"])
    s.add_dependency(%q<minitest>.freeze, ["~> 5.0"])
  end
end
