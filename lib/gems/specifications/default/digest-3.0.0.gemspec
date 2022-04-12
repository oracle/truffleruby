# -*- encoding: utf-8 -*-
# stub: digest 3.0.0 ruby lib

Gem::Specification.new do |s|
  s.name = "digest".freeze
  s.version = "3.0.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "msys2_mingw_dependencies" => "openssl" } if s.respond_to? :metadata=
  s.require_paths = ["lib".freeze]
  s.authors = ["Akinori MUSHA".freeze]
  s.bindir = "exe".freeze
  s.date = "2022-03-17"
  s.description = "Provides a framework for message digest libraries.".freeze
  s.email = ["knu@idaemons.org".freeze]
  s.extensions = ["ext/digest/extconf.rb".freeze, "ext/digest/bubblebabble/extconf.rb".freeze, "ext/digest/md5/extconf.rb".freeze, "ext/digest/rmd160/extconf.rb".freeze, "ext/digest/sha1/extconf.rb".freeze, "ext/digest/sha2/extconf.rb".freeze]
  s.files = ["digest.rb".freeze, "digest.#{Truffle::Platform::DLEXT}".freeze, "digest/bubblebabble.#{Truffle::Platform::DLEXT}".freeze, "digest/md5.#{Truffle::Platform::DLEXT}".freeze, "digest/rmd160.#{Truffle::Platform::DLEXT}".freeze, "digest/sha1.#{Truffle::Platform::DLEXT}".freeze, "digest/sha2.rb".freeze, "digest/sha2.#{Truffle::Platform::DLEXT}".freeze, "ext/digest/bubblebabble/extconf.rb".freeze, "ext/digest/extconf.rb".freeze, "ext/digest/md5/extconf.rb".freeze, "ext/digest/rmd160/extconf.rb".freeze, "ext/digest/sha1/extconf.rb".freeze, "ext/digest/sha2/extconf.rb".freeze]
  s.homepage = "https://github.com/ruby/digest".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.3.0".freeze)
  s.rubygems_version = "3.2.32".freeze
  s.summary = "Provides a framework for message digest libraries.".freeze

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
