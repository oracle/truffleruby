# -*- encoding: utf-8 -*-
# stub: power_assert 0.2.3 ruby lib

Gem::Specification.new do |s|
  s.name = "power_assert"
  s.version = "0.2.3"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib"]
  s.authors = ["Kazuki Tsujimoto"]
  s.date = "2015-03-01"
  s.description = "Power Assert for Ruby. Power Assert shows each value of variables and method calls in the expression. It is useful for testing, providing which value wasn't correct when the condition is not satisfied."
  s.email = ["kazuki@callcc.net"]
  s.extra_rdoc_files = ["README.rdoc"]
  s.files = ["README.rdoc"]
  s.homepage = "https://github.com/k-tsj/power_assert"
  s.licenses = ["2-clause BSDL", "Ruby's"]
  s.rdoc_options = ["--main", "README.rdoc"]
  s.rubygems_version = "2.5.1"
  s.summary = "Power Assert for Ruby"

  s.installed_by_version = "2.5.1" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<test-unit>, [">= 0"])
      s.add_development_dependency(%q<rake>, [">= 0"])
      s.add_development_dependency(%q<simplecov>, [">= 0"])
    else
      s.add_dependency(%q<test-unit>, [">= 0"])
      s.add_dependency(%q<rake>, [">= 0"])
      s.add_dependency(%q<simplecov>, [">= 0"])
    end
  else
    s.add_dependency(%q<test-unit>, [">= 0"])
    s.add_dependency(%q<rake>, [">= 0"])
    s.add_dependency(%q<simplecov>, [">= 0"])
  end
end
