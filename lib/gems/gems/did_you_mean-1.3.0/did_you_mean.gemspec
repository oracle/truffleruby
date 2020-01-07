# -*- encoding: utf-8 -*-
# stub: did_you_mean 1.3.0 ruby lib

Gem::Specification.new do |s|
  s.name = "did_you_mean".freeze
  s.version = "1.3.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Yuki Nishijima".freeze]
  s.date = "2018-12-18"
  s.description = "The gem that has been saving people from typos since 2014.".freeze
  s.email = ["mail@yukinishijima.net".freeze]
  s.files = [".gitignore".freeze, ".ruby-version".freeze, ".travis.yml".freeze, "CHANGELOG.md".freeze, "Gemfile".freeze, "LICENSE.txt".freeze, "README.md".freeze, "Rakefile".freeze, "benchmark/jaro_winkler/memory_usage.rb".freeze, "benchmark/jaro_winkler/speed.rb".freeze, "benchmark/levenshtein/memory_usage.rb".freeze, "benchmark/levenshtein/speed.rb".freeze, "benchmark/memory_usage.rb".freeze, "did_you_mean.gemspec".freeze, "doc/CHANGELOG.md.erb".freeze, "doc/changelog_generator.rb".freeze, "lib/did_you_mean.rb".freeze, "lib/did_you_mean/core_ext/name_error.rb".freeze, "lib/did_you_mean/experimental.rb".freeze, "lib/did_you_mean/experimental/initializer_name_correction.rb".freeze, "lib/did_you_mean/experimental/ivar_name_correction.rb".freeze, "lib/did_you_mean/formatters/plain_formatter.rb".freeze, "lib/did_you_mean/formatters/verbose_formatter.rb".freeze, "lib/did_you_mean/jaro_winkler.rb".freeze, "lib/did_you_mean/levenshtein.rb".freeze, "lib/did_you_mean/spell_checker.rb".freeze, "lib/did_you_mean/spell_checkers/key_error_checker.rb".freeze, "lib/did_you_mean/spell_checkers/method_name_checker.rb".freeze, "lib/did_you_mean/spell_checkers/name_error_checkers.rb".freeze, "lib/did_you_mean/spell_checkers/name_error_checkers/class_name_checker.rb".freeze, "lib/did_you_mean/spell_checkers/name_error_checkers/variable_name_checker.rb".freeze, "lib/did_you_mean/spell_checkers/null_checker.rb".freeze, "lib/did_you_mean/verbose.rb".freeze, "lib/did_you_mean/version.rb".freeze, "test/core_ext/name_error_extension_test.rb".freeze, "test/edit_distance/jaro_winkler_test.rb".freeze, "test/experimental/initializer_name_correction_test.rb".freeze, "test/experimental/method_name_checker_test.rb".freeze, "test/fixtures/book.rb".freeze, "test/spell_checker_test.rb".freeze, "test/spell_checking/class_name_check_test.rb".freeze, "test/spell_checking/key_name_check_test.rb".freeze, "test/spell_checking/method_name_check_test.rb".freeze, "test/spell_checking/uncorrectable_name_check_test.rb".freeze, "test/spell_checking/variable_name_check_test.rb".freeze, "test/test_helper.rb".freeze, "test/verbose_formatter_test.rb".freeze, "tmp/.keep".freeze]
  s.homepage = "https://github.com/yuki24/did_you_mean".freeze
  s.licenses = ["MIT".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.5.0".freeze)
  s.rubygems_version = "2.7.6".freeze
  s.summary = "\"Did you mean?\" experience in Ruby".freeze
  s.test_files = ["test/core_ext/name_error_extension_test.rb".freeze, "test/edit_distance/jaro_winkler_test.rb".freeze, "test/experimental/initializer_name_correction_test.rb".freeze, "test/experimental/method_name_checker_test.rb".freeze, "test/fixtures/book.rb".freeze, "test/spell_checker_test.rb".freeze, "test/spell_checking/class_name_check_test.rb".freeze, "test/spell_checking/key_name_check_test.rb".freeze, "test/spell_checking/method_name_check_test.rb".freeze, "test/spell_checking/uncorrectable_name_check_test.rb".freeze, "test/spell_checking/variable_name_check_test.rb".freeze, "test/test_helper.rb".freeze, "test/verbose_formatter_test.rb".freeze]

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_development_dependency(%q<rake>.freeze, [">= 0"])
      s.add_development_dependency(%q<minitest>.freeze, [">= 0"])
    else
      s.add_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_dependency(%q<rake>.freeze, [">= 0"])
      s.add_dependency(%q<minitest>.freeze, [">= 0"])
    end
  else
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
    s.add_dependency(%q<minitest>.freeze, [">= 0"])
  end
end
