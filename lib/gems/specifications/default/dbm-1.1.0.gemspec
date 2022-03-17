# -*- encoding: utf-8 -*-
# stub: dbm 1.1.0 ruby lib
# stub: ext/dbm/extconf.rb

Gem::Specification.new do |s|
  s.name = "dbm".freeze
  s.version = "1.1.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "msys2_mingw_dependencies" => "gdbm" } if s.respond_to? :metadata=
  s.require_paths = ["lib".freeze]
  s.authors = ["Yukihiro Matsumoto".freeze]
  s.date = "2022-03-17"
  s.description = "Provides a wrapper for the UNIX-style Database Manager Library".freeze
  s.email = ["matz@ruby-lang.org".freeze]
  s.extensions = ["ext/dbm/extconf.rb".freeze]
  s.files = ["dbm.#{Truffle::Platform::DLEXT}".freeze, "ext/dbm/extconf.rb".freeze]
  s.homepage = "https://github.com/ruby/dbm".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.3.0".freeze)
  s.rubygems_version = "3.2.32".freeze
  s.summary = "Provides a wrapper for the UNIX-style Database Manager Library".freeze

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
