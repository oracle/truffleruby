require 'rbconfig'

class MSpecScript

  def self.child_process?
    ENV.key? "MSPEC_RUNNER"
  end

  def self.windows?
    ENV.key?('WINDIR') || ENV.key?('windir')
  end

  def self.linux?
    RbConfig::CONFIG['host_os'].include?('linux')
  end

  def self.darwin?
    RbConfig::CONFIG['host_os'].include?('darwin')
  end

  def self.solaris?
    RbConfig::CONFIG['host_os'].include?('solaris')
  end

  if child_process?
    system_ruby = `which ruby`.chomp
    if !system_ruby.empty? and `#{system_ruby} --version`.start_with?('ruby ')
      SYSTEM_RUBY = system_ruby
    end
  end

  # Specs that need Sulong and should be tested in the Sulong gate
  library_cext_specs = %w[
    spec/ruby/library/etc
    spec/ruby/library/openssl
    spec/ruby/library/rbconfig/sizeof
    spec/ruby/library/syslog
    spec/ruby/library/yaml
    spec/ruby/library/zlib
    spec/ruby/security/cve_2017_17742_spec.rb
    spec/ruby/security/cve_2019_8321_spec.rb
    spec/ruby/security/cve_2019_8322_spec.rb
    spec/ruby/security/cve_2019_8323_spec.rb
    spec/ruby/security/cve_2019_8325_spec.rb
  ]

  set :command_line, [
    "spec/ruby/command_line"
  ]

  set :security, [
    "spec/ruby/security",

    # Tested separately as they need Sulong
    *library_cext_specs.map { |path| "^#{path}" }
  ]

  set :language, [
    "spec/ruby/language"
  ]

  set :core, [
    "spec/ruby/core"
  ]

  set :library, [
    "spec/ruby/library",

    # Trying to enable breaks a lot of things
    "^spec/ruby/library/net",

    # Unsupported
    "^spec/ruby/library/win32ole",

    # Tested separately as they need Sulong
    *library_cext_specs.map { |path| "^#{path}" }
  ]

  set :library_cext, library_cext_specs

  set :capi, [
    "spec/ruby/optional/capi"
  ]

  set :truffle, [
    "spec/truffle",

    # Tested separately
    "^spec/truffle/capi"
  ]

  set :truffle_capi, [
    "spec/truffle/capi"
  ]

  set :next, [
  ]

  set :backtrace_filter, /mspec\//

  set :tags_patterns, [
    [%r(^.*/command_line/),             'spec/tags/command_line/'],
    [%r(^.*/security/),                 'spec/tags/security/'],
    [%r(^.*/language/),                 'spec/tags/language/'],
    [%r(^.*/core/),                     'spec/tags/core/'],
    [%r(^.*/library/),                  'spec/tags/library/'],
    [%r(^.*/optional/capi/),            'spec/tags/optional/capi/'],
    [%r(^.*/truffle),                   'spec/tags/truffle/'],
    [/_spec.rb$/,                       '_tags.txt']
  ]

  set :xtags, (get(:xtags) || [])
  tags = get(:xtags)

  if defined?(::TruffleRuby)
    if TruffleRuby.native?
      # exclude specs tagged with 'aot'
      tags << 'aot'
    else
      # exclude specs tagged with 'jvm'
      tags << 'jvm'
    end
  end

  if windows?
    # exclude specs tagged with 'windows'
    tags << 'windows'
  elsif linux?
    # exclude specs tagged with 'linux'
    tags << 'linux'
  elsif darwin?
    # exclude specs tagged with 'darwin'
    tags << 'darwin'
  elsif solaris?
    # exclude specs tagged with 'solaris'
    tags << 'solaris'
  end

  set :files, get(:command_line) + get(:language) + get(:core) + get(:library) + get(:truffle) + get(:security)

  # All specs, including specs needing C-extensions support.
  # Next version specs are not included as they need to run in a separate process.
  set :all, get(:files) + get(:capi) + get(:truffle_capi) + get(:library_cext)
end

if MSpecScript.child_process?
  if version = ENV["PRETEND_RUBY_VERSION"]
    ::VersionGuard::FULL_RUBY_VERSION = SpecVersion.new(version)
  elsif ARGV.include? ":next"
    ::VersionGuard::FULL_RUBY_VERSION = SpecVersion.new("2.7.0")
  end
end

if i = ARGV.index('slow') and ARGV[i-1] == '--excl-tag' and MSpecScript.child_process?
  require 'mspec'

  class SlowSpecsTagger
    def initialize
      MSpec.register :exception, self
    end

    def exception(state)
      if state.exception.is_a? SlowSpecException
        tag = SpecTag.new
        tag.tag = 'slow'
        tag.description = "#{state.describe} #{state.it}"
        MSpec.write_tag(tag)
      end
    end
  end

  class SlowSpecException < Exception
  end

  require 'timeout'

  slow_methods = [
    [Object, [:ruby_exe, :ruby_cmd]],
    [ObjectSpace.singleton_class, [:each_object]],
    [GC.singleton_class, [:start]],
    [Kernel, [:system, :`]],
    [Kernel.singleton_class, [:system, :`]],
    [Timeout.singleton_class, [:timeout]],
  ]

  module Kernel
    alias_method :mspec_old_system, :system
    private :mspec_old_system

    alias_method :"mspec_old_`", :`
    private :"mspec_old_`"
  end

  slow_methods.each do |klass, meths|
    klass.class_exec do
      meths.each do |meth|
        define_method(meth) do |*args, &block|
          if MSpec.current && MSpec.current.state # an example is running
            raise SlowSpecException, "Was tagged as slow as it uses #{meth}(). Rerun specs."
          else
            send("mspec_old_#{meth}", *args, &block)
          end
        end
        # Keep visibility for Kernel instance methods
        private meth if klass == Kernel
      end
    end
  end

  SlowSpecsTagger.new
end
