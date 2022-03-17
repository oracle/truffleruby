# -*- encoding: utf-8 -*-
# stub: fiddle 1.0.8 ruby lib
# stub: ext/fiddle/extconf.rb

Gem::Specification.new do |s|
  s.name = "fiddle".freeze
  s.version = "1.0.8"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "msys2_mingw_dependencies" => "libffi" } if s.respond_to? :metadata=
  s.require_paths = ["lib".freeze]
  s.authors = ["Aaron Patterson".freeze, "SHIBATA Hiroshi".freeze]
  s.date = "2022-03-17"
  s.description = "A libffi wrapper for Ruby.".freeze
  s.email = ["aaron@tenderlovemaking.com".freeze, "hsbt@ruby-lang.org".freeze]
  s.extensions = ["ext/fiddle/extconf.rb".freeze]
  s.files = ["ext/fiddle/extconf.rb".freeze, "fiddle.rb".freeze, "fiddle.#{Truffle::Platform::DLEXT}".freeze, "fiddle/closure.rb".freeze, "fiddle/cparser.rb".freeze, "fiddle/function.rb".freeze, "fiddle/import.rb".freeze, "fiddle/pack.rb".freeze, "fiddle/struct.rb".freeze, "fiddle/types.rb".freeze, "fiddle/value.rb".freeze, "fiddle/version.rb".freeze]
  s.homepage = "https://github.com/ruby/fiddle".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.3.0".freeze)
  s.rubygems_version = "3.2.32".freeze
  s.summary = "A libffi wrapper for Ruby.".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_development_dependency(%q<rake>.freeze, [">= 0"])
    s.add_development_dependency(%q<rake-compiler>.freeze, [">= 0"])
  else
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
    s.add_dependency(%q<rake-compiler>.freeze, [">= 0"])
  end
end
