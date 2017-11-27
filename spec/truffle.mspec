require 'rbconfig'

class MSpecScript

  def self.child_process?
    ENV.key? "MSPEC_RUNNER"
  end

  def self.windows?
    ENV.key?('WINDIR') || ENV.key?('windir')
  end

  def self.linux?
    RbConfig::CONFIG['host_os'] == 'linux'
  end

  def self.solaris?
    RbConfig::CONFIG['host_os'] == 'solaris'
  end

  if child_process?
    system_ruby = `which ruby`.chomp
    if !system_ruby.empty? and `#{system_ruby} --version`.start_with?('ruby ')
      SYSTEM_RUBY = system_ruby
    end
  end

  TRUFFLERUBY_DIR = File.expand_path('../..', __FILE__)

  set :target, "#{TRUFFLERUBY_DIR}/bin/truffleruby"

  # No flags set if a Ruby binary is specified via -t
  if !child_process? and !ARGV.include?('-t')
    flags = %w[
      -J-ea
      -J-esa
      -J-Xmx2G
      -Xgraal.warn_unless=false
      --disable-gems
    ]
    core_path = "#{TRUFFLERUBY_DIR}/src/main/ruby"
    if File.directory?(core_path)
      flags << "-Xcore.load_path=#{core_path}"
      flags << "-Xbacktraces.hide_core_files=false"
    end
    set :flags, flags
  end

  set :command_line, [
    "spec/ruby/command_line"
  ]

  set :security, [
    "spec/ruby/security"
  ]

  set :language, [
    "spec/ruby/language"
  ]

  set :core, [
    "spec/ruby/core",
  ]

  set :library, [
    "spec/ruby/library",

    # Not yet explored
    "^spec/ruby/library/mathn",
    "^spec/ruby/library/readline",
    "^spec/ruby/library/syslog",

    # Doesn't exist as Ruby code - basically need to write from scratch
    "^spec/ruby/library/win32ole",

    # Uses the Rubinius FFI generator
    "^spec/ruby/library/etc",

    # Hangs in CI
    "^spec/ruby/library/net",

    # openssl is tested separately as it needs Sulong
    "^spec/ruby/library/openssl",
  ]

  set :capi, [
    "spec/ruby/optional/capi"
  ]

  set :openssl, [
    "spec/ruby/library/openssl"
  ]

  set :truffle, [
    "spec/truffle"
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

  if windows?
    # exclude specs tagged with 'windows'
    set :xtags, (get(:xtags) || []) + ['windows']
  end

  if linux?
    # exclude specs tagged with 'linux'
    set :xtags, (get(:xtags) || []) + ['linux']
  end

  if solaris?
    # exclude specs tagged with 'solaris'
    set :xtags, (get(:xtags) || []) + ['solaris']
  end

  # Enable features
  MSpec.enable_feature :fiber
  MSpec.enable_feature :fiber_library
  MSpec.disable_feature :fork
  MSpec.enable_feature :encoding
  MSpec.enable_feature :readline

  set :files, get(:command_line) + get(:language) + get(:core) + get(:library) + get(:truffle) + get(:security)
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
