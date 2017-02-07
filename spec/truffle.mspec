require 'rbconfig'

class MSpecScript

  def self.windows?
    ENV.key?('WINDIR') || ENV.key?('windir')
  end

  def self.linux?
    RbConfig::CONFIG['host_os'] == 'linux'
  end

  def self.solaris?
    RbConfig::CONFIG['host_os'] == 'solaris'
  end

  JRUBY_DIR = File.expand_path('../..', __FILE__)

  set :target, "#{JRUBY_DIR}/bin/truffleruby"

  unless ARGV.include?('-t')  # No flags set if Ruby binary specified via -t. 
    flags = %w[
      -J-ea
      -J-esa
      -J-Xmx2G
      -Xgraal.warn_unless=false
    ]
    core_path = "#{JRUBY_DIR}/truffleruby/src/main/ruby"
    if File.directory?(core_path)
      flags << "-Xcore.load_path=#{core_path}"
      flags << "-Xbacktraces.hide_core_files=false"
    end
    set :flags, flags
  end

  set :command_line, [
    "spec/ruby/command_line"
  ]

  set :language, [
    "spec/ruby/language"
  ]

  set :core, [
    "spec/ruby/core",
  ]

  set :library, [
    "spec/ruby/library",
    
    # Since 2.3
    "^spec/ruby/library/resolv",
    "^spec/ruby/library/drb",

    # Not yet explored
    "^spec/ruby/library/continuation",
    "^spec/ruby/library/mathn",
    "^spec/ruby/library/readline",
    "^spec/ruby/library/syslog",

    # Doesn't exist as Ruby code - basically need to write from scratch
    "^spec/ruby/library/win32ole",

    # Uses the Rubinius FFI generator
    "^spec/ruby/library/etc",

    # Hangs
    "^spec/ruby/library/net",

    # Load issues with 'delegate'.
    "^spec/ruby/library/delegate/delegate_class/instance_method_spec.rb",
    "^spec/ruby/library/delegate/delegator/protected_methods_spec.rb",

    # Openssl not yet supported
    "^spec/ruby/library/openssl/cipher_spec.rb",
    "^spec/ruby/library/openssl/config/freeze_spec.rb",
    "^spec/ruby/library/openssl/hmac/digest_spec.rb",
    "^spec/ruby/library/openssl/hmac/hexdigest_spec.rb",
    "^spec/ruby/library/openssl/random/pseudo_bytes_spec.rb",
    "^spec/ruby/library/openssl/random/random_bytes_spec.rb",
    "^spec/ruby/library/openssl/x509/name/parse_spec.rb"
  ]

  set :capi, [
    "spec/ruby/optional/capi",
    
    # Tags are read as ASCII but contain UTF codepoints
    "^spec/ruby/optional/capi/encoding_spec.rb"
  ]

  set :truffle, [
    "spec/truffle"
  ]

  set :backtrace_filter, /mspec\//

  set :tags_patterns, [
    [%r(^.*/command_line/),             'spec/tags/command_line/'],
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
  MSpec.disable_feature :continuation_library
  MSpec.disable_feature :fork
  MSpec.enable_feature :encoding
  MSpec.enable_feature :readline

  set :files, get(:language) + get(:core) + get(:library) + get(:truffle)
end

is_child_process = ENV.key? "MSPEC_RUNNER"
if i = ARGV.index('slow') and ARGV[i-1] == '--excl-tag' and is_child_process
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
    [Kernel, [:system]],
    [Kernel.singleton_class, [:system]],
    [Timeout.singleton_class, [:timeout]],
  ]

  slow_methods.each do |klass, meths|
    klass.class_exec do
      meths.each do |meth|
        define_method(meth) do |*args, &block|
          raise SlowSpecException, "Was tagged as slow as it uses #{meth}(). Rerun specs."
        end
        # Keep visibility of Kernel#system
        private meth if klass == Kernel and meth == :system
      end
    end
  end

  SlowSpecsTagger.new
end
