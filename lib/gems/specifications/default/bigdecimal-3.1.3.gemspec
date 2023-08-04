# -*- encoding: utf-8 -*-
# stub: bigdecimal 3.1.3 ruby lib
# stub: ext/bigdecimal/extconf.rb

Gem::Specification.new do |s|
  s.name = "bigdecimal".freeze
  s.version = "3.1.3"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Kenta Murata".freeze, "Zachary Scott".freeze, "Shigeo Kobayashi".freeze]
  s.date = "2023-03-30"
  s.description = "This library provides arbitrary-precision decimal floating-point number class.".freeze
  s.email = ["mrkn@mrkn.jp".freeze]
  s.extensions = ["ext/bigdecimal/extconf.rb".freeze]
  s.files = ["ext/bigdecimal/extconf.rb".freeze, "lib/bigdecimal.rb".freeze, "lib/bigdecimal/jacobian.rb".freeze, "lib/bigdecimal/ludcmp.rb".freeze, "lib/bigdecimal/math.rb".freeze, "lib/bigdecimal/newton.rb".freeze, "lib/bigdecimal/util.rb".freeze]
  s.homepage = "https://github.com/ruby/bigdecimal".freeze
  s.licenses = ["Ruby".freeze, "bsd-2-clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.5.0".freeze)
  s.rubygems_version = "3.4.10".freeze
  s.summary = "Arbitrary-precision decimal floating-point number library.".freeze
end
