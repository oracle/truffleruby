require 'rbconfig'

# Inherit from the default configuration
load "#{__dir__}/ruby/default.mspec"

# Don't run ruby/spec as root on TruffleRuby
raise 'ruby/spec is not designed to be run as root on TruffleRuby' if Process.uid == 0

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
    system_ruby = ENV['SYSTEM_RUBY'] || `which ruby`.chomp
    if !system_ruby.empty? and `#{system_ruby} --version`.start_with?('ruby ')
      SYSTEM_RUBY = system_ruby
    end
  end

  set :prefix, 'spec/ruby'

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

  set :security, [
    "spec/ruby/security",

    # Tested separately as they need Sulong
    *library_cext_specs.map { |path| "^#{path}" }
  ]

  set :library, [
    "spec/ruby/library",

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

  set :next, %w[
    spec/ruby/core/file/absolute_path_spec.rb
    spec/ruby/core/matchdata/allocate_spec.rb
    spec/ruby/core/unboundmethod/bind_call_spec.rb
  ]

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
  excludes = get(:xtags)

  if defined?(::TruffleRuby)
    if TruffleRuby.native?
      excludes << 'aot'
    else
      excludes << 'jvm'
    end
  end

  if windows?
    excludes << 'windows'
  elsif linux?
    excludes << 'linux'
  elsif darwin?
    excludes << 'darwin'
  elsif solaris?
    excludes << 'solaris'
  end

  # All specs, excluding specs needing C-extensions support.
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

  slow_methods.each do |klass, meths|
    klass.class_exec do
      meths.each do |meth|
        original = instance_method(meth)
        define_method(meth) do |*args, &block|
          if MSpec.current && MSpec.current.state # an example is running
            Thread.main.raise SlowSpecException, "Was tagged as slow as it uses #{meth}(). Rerun specs."
          else
            original.bind(self).call(*args, &block)
          end
        end
        # Keep visibility for Kernel instance methods
        private meth if klass == Kernel
      end
    end
  end

  SlowSpecsTagger.new
end
