# -*- encoding: utf-8 -*-
# stub: etc 1.4.2 ruby lib
# stub: ext/etc/extconf.rb

Gem::Specification.new do |s|
  s.name = "etc".freeze
  s.version = "1.4.2"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Yukihiro Matsumoto".freeze]
  s.bindir = "exe".freeze
  s.date = "2023-03-30"
  s.description = "Provides access to information typically stored in UNIX /etc directory.".freeze
  s.email = ["matz@ruby-lang.org".freeze]
  s.extensions = ["ext/etc/extconf.rb".freeze]
  s.extra_rdoc_files = ["ChangeLog".freeze, "LICENSE.txt".freeze, "README.md".freeze, "ext/etc/constdefs.h".freeze, "ext/etc/etc.c".freeze, "ext/etc/extconf.rb".freeze, "ext/etc/mkconstants.rb".freeze, "test/etc/test_etc.rb".freeze]
  s.files = ["ChangeLog".freeze, "LICENSE.txt".freeze, "README.md".freeze, "ext/etc/constdefs.h".freeze, "ext/etc/etc.c".freeze, "ext/etc/extconf.rb".freeze, "ext/etc/mkconstants.rb".freeze, "test/etc/test_etc.rb".freeze]
  s.homepage = "https://github.com/ruby/etc".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.rdoc_options = ["--main".freeze, "README.md".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.6.0".freeze)
  s.rubygems_version = "3.4.10".freeze
  s.summary = "Provides access to information typically stored in UNIX /etc directory.".freeze
end
