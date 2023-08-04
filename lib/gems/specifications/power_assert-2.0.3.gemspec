# -*- encoding: utf-8 -*-
# stub: power_assert 2.0.3 ruby lib

Gem::Specification.new do |s|
  s.name = "power_assert".freeze
  s.version = "2.0.3"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Kazuki Tsujimoto".freeze]
  s.bindir = "exe".freeze
  s.date = "2023-03-30"
  s.description = "Power Assert shows each value of variables and method calls in the expression. It is useful for testing, providing which value wasn't correct when the condition is not satisfied.".freeze
  s.email = ["kazuki@callcc.net".freeze]
  s.extra_rdoc_files = ["README.md".freeze]
  s.files = ["README.md".freeze]
  s.homepage = "https://github.com/ruby/power_assert".freeze
  s.licenses = ["BSD-2-Clause".freeze, "Ruby".freeze]
  s.rdoc_options = ["--main".freeze, "README.md".freeze]
  s.rubygems_version = "3.4.10".freeze
  s.summary = "Power Assert for Ruby".freeze

  s.installed_by_version = "3.4.10" if s.respond_to? :installed_by_version

  s.specification_version = 4

  s.add_development_dependency(%q<test-unit>.freeze, [">= 0"])
  s.add_development_dependency(%q<rake>.freeze, [">= 0"])
  s.add_development_dependency(%q<simplecov>.freeze, [">= 0"])
  s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
  s.add_development_dependency(%q<irb>.freeze, [">= 1.3.1"])
  s.add_development_dependency(%q<byebug>.freeze, [">= 0"])
  s.add_development_dependency(%q<benchmark-ips>.freeze, [">= 0"])
end
