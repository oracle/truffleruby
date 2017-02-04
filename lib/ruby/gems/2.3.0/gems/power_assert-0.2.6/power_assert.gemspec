$:.push File.expand_path('../lib', __FILE__)
require 'power_assert/version'

Gem::Specification.new do |s|
  s.name        = 'power_assert'
  s.version     = PowerAssert::VERSION
  s.authors     = ['Kazuki Tsujimoto']
  s.email       = ['kazuki@callcc.net']
  s.homepage    = 'https://github.com/k-tsj/power_assert'
  s.summary     = %q{Power Assert for Ruby}
  s.description = %q{Power Assert for Ruby. Power Assert shows each value of variables and method calls in the expression. It is useful for testing, providing which value wasn't correct when the condition is not satisfied.}

  s.files            = `git ls-files`.split("\n")
  s.test_files       = `git ls-files -- {test,spec,features}/*`.split("\n")
  s.executables      = `git ls-files -- bin/*`.split("\n").map{|f| File.basename(f) }
  s.require_paths    = ['lib']
  s.add_development_dependency 'test-unit'
  s.add_development_dependency 'rake'
  s.add_development_dependency 'simplecov'
  s.extra_rdoc_files = ['README.rdoc']
  s.rdoc_options     = ['--main', 'README.rdoc']
  s.licenses         = ['2-clause BSDL', "Ruby's"]
end
