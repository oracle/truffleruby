# -*- encoding: utf-8 -*-
# stub: drb 2.1.1 ruby lib

Gem::Specification.new do |s|
  s.name = "drb".freeze
  s.version = "2.1.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "homepage_uri" => "https://github.com/ruby/drb", "source_code_uri" => "https://github.com/ruby/drb" } if s.respond_to? :metadata=
  s.require_paths = ["lib".freeze]
  s.authors = ["Masatoshi SEKI".freeze]
  s.date = "2023-03-30"
  s.description = "Distributed object system for Ruby".freeze
  s.email = ["seki@ruby-lang.org".freeze]
  s.files = ["lib/drb.rb".freeze, "lib/drb/acl.rb".freeze, "lib/drb/drb.rb".freeze, "lib/drb/eq.rb".freeze, "lib/drb/extserv.rb".freeze, "lib/drb/extservm.rb".freeze, "lib/drb/gw.rb".freeze, "lib/drb/invokemethod.rb".freeze, "lib/drb/observer.rb".freeze, "lib/drb/ssl.rb".freeze, "lib/drb/timeridconv.rb".freeze, "lib/drb/unix.rb".freeze, "lib/drb/version.rb".freeze, "lib/drb/weakidconv.rb".freeze]
  s.homepage = "https://github.com/ruby/drb".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.7.0".freeze)
  s.rubygems_version = "3.4.10".freeze
  s.summary = "Distributed object system for Ruby".freeze

  s.specification_version = 4

  s.add_runtime_dependency(%q<ruby2_keywords>.freeze, [">= 0"])
end
