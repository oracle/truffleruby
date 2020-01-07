# -*- encoding: utf-8 -*-
# stub: shell 0.7 ruby lib

Gem::Specification.new do |s|
  s.name = "shell".freeze
  s.version = "0.7"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Keiju ISHITSUKA".freeze]
  s.bindir = "exe".freeze
  s.date = "2020-01-07"
  s.description = "An idiomatic Ruby interface for common UNIX shell commands.".freeze
  s.email = ["keiju@ruby-lang.org".freeze]
  s.files = ["shell.rb".freeze, "shell/builtin-command.rb".freeze, "shell/command-processor.rb".freeze, "shell/error.rb".freeze, "shell/filter.rb".freeze, "shell/process-controller.rb".freeze, "shell/system-command.rb".freeze, "shell/version.rb".freeze]
  s.homepage = "https://github.com/ruby/shell".freeze
  s.licenses = ["BSD-2-Clause".freeze]
  s.rubygems_version = "3.0.3".freeze
  s.summary = "An idiomatic Ruby interface for common UNIX shell commands.".freeze

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_development_dependency(%q<rake>.freeze, [">= 0"])
    else
      s.add_dependency(%q<bundler>.freeze, [">= 0"])
      s.add_dependency(%q<rake>.freeze, [">= 0"])
    end
  else
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
  end
end
