# -*- encoding: utf-8 -*-
# stub: gdbm 2.1.0 ruby lib
# stub: ext/gdbm/extconf.rb

Gem::Specification.new do |s|
  s.name = "gdbm".freeze
  s.version = "2.1.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Yukihiro Matsumoto".freeze]
  s.bindir = "exe".freeze
  s.date = "2022-03-17"
  s.description = "Ruby extension for GNU dbm.".freeze
  s.email = ["matz@ruby-lang.org".freeze]
  s.extensions = ["ext/gdbm/extconf.rb".freeze]
  s.files = ["ext/gdbm/extconf.rb".freeze, "gdbm.#{Truffle::Platform::DLEXT}".freeze]
  s.homepage = "https://github.com/ruby/gdbm".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.3.0".freeze)
  s.rubygems_version = "3.2.32".freeze
  s.summary = "Ruby extension for GNU dbm.".freeze
end
