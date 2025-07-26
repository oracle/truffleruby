# truffleruby_primitives: true

require 'rbconfig'

# Inherit from the default configuration
load "#{__dir__}/ruby/default.mspec"

# Don't run ruby/spec as root on TruffleRuby
raise 'ruby/spec is not designed to be run as root on TruffleRuby' if Process.uid == 0

ENV['TRUFFLERUBY_ALLOW_PRIVATE_PRIMITIVES_IN'] = "#{__dir__}/truffle/"

class MSpecScript
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

  def self.aarch64?
    %w[arm64 aarch64].include? RbConfig::CONFIG['host_cpu']
  end

  if child_process?
    system_ruby = ENV['SYSTEM_RUBY'] || `which ruby`.chomp
    if !system_ruby.empty? and `#{system_ruby} --version`.start_with?('ruby ')
      SYSTEM_RUBY = system_ruby
    end
  end

  set :prefix, 'spec/ruby'

  # Run separately as they trigger instrumentation and are particularly slow (they instrument all loaded code)
  set :tracepoint, [
    "spec/ruby/core/tracepoint",
    "spec/ruby/optional/capi/tracepoint_spec.rb",
  ]

  set :core, [
    "spec/ruby/core",
    # See :tracepoint
    "^spec/ruby/core/tracepoint",
  ]

  # Specs that need Sulong and should be tested in the Sulong gate
  library_cext_specs = %w[
    spec/ruby/library/bigdecimal
    spec/ruby/library/date
    spec/ruby/library/etc
    spec/ruby/library/openssl
    spec/ruby/library/rbconfig/sizeof
    spec/ruby/library/ripper
    spec/ruby/library/syslog
    spec/ruby/library/yaml
    spec/ruby/library/zlib
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
    "spec/ruby/optional/capi",
    # See :tracepoint
    "^spec/ruby/optional/capi/tracepoint_spec.rb",
  ]

  set :truffle, [
    "spec/truffle",

    # Tested separately
    "^spec/truffle/capi"
  ]

  set :truffle_capi, [
    "spec/truffle/capi"
  ]

  # Use spec/truffleruby.next-specs to specify spec files to run for the next version.
  # Allow comments and empty lines.
  next_spec_files = File.readlines("spec/truffleruby.next-specs")
                        .map(&:strip)
                        .reject { |path| path.start_with?("#") || path.empty?  }

  set :next, next_spec_files

  set :tags_patterns, [
    [%r(^(.*)/spec/ruby/(\w+)/(.+)_spec\.rb$), '\1/spec/tags/\2/\3_tags.txt'],
    [%r(^(.*)/spec/truffle/(.+)_spec\.rb$),    '\1/spec/tags/truffle/\2_tags.txt'],
  ]

  set :xtags, (get(:xtags) || [])
  excludes = get(:xtags)

  if defined?(::TruffleRuby)
    if TruffleRuby.native?
      excludes << 'native'
      if GC.heap_stats.values.none?(Hash)
        excludes << 'native-g1'
        # GR-23507: exclude specs using execve() as concurrent pthread_create()
        # return EAGAIN and cause extra logging on stdout,
        # and -Xlog configuration is not available yet on Native Image.
        excludes << 'execve'
      end
    else
      excludes << 'jvm'
    end

    if Truffle::Boot.get_option('cexts-sulong')
      excludes << 'sulong'
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

  if aarch64?
    excludes << 'aarch64'
  end

  # All specs, excluding specs needing C-extensions support, and TracePoint specs.
  set :files, get(:command_line) + get(:language) + get(:core) + get(:library) + get(:truffle) + get(:security)

  # Specs needing C-extensions support.
  set :cext, get(:capi) + get(:truffle_capi) + get(:library_cext)

  # All specs, including specs needing C-extensions support.
  # :next specs are not included as they need to run in a separate process.
  # :tracepoint specs are not included as they need should run in a separate process.
  set :all, get(:files) + get(:cext)
end

if MSpecScript.child_process?
  if version = ENV["PRETEND_RUBY_VERSION"]
    ::VersionGuard.send :remove_const, :FULL_RUBY_VERSION
    ::VersionGuard::FULL_RUBY_VERSION = SpecVersion.new(version)
  elsif ARGV.include? ":next"
    version = File.read(".ruby-version")

    # get the next version, e.g. 3.1.2 -> 3.2.0
    next_version = version.split(".").tap { |ary| ary[1] = ary[1].next; ary[2] = '0' }.join(".")

    ::VersionGuard.send :remove_const, :FULL_RUBY_VERSION
    ::VersionGuard::FULL_RUBY_VERSION = SpecVersion.new(next_version)
  end
end

if i = ARGV.index('slow') and ARGV[i-1] == '--excl-tag' and MSpecScript.child_process?
  SKIP_SLOW_SPECS = true

  require 'mspec'
  require 'timeout'
  require 'objspace'

  slow_methods = [
    [Object, [:ruby_exe, :ruby_cmd]],
    [ObjectSpace.singleton_class, [:each_object, :trace_object_allocations_start]],
    [GC.singleton_class, [:start]],
    [Kernel, [:system, :`]],
    [Kernel.singleton_class, [:system, :`]],
    [Timeout.singleton_class, [:timeout]],
  ]

  missing_slow_tags = false

  slow_methods.each do |klass, meths|
    klass.class_exec do
      meths.each do |meth|
        original = instance_method(meth)
        define_method(meth) do |*args, &block|
          if MSpec.current and state = MSpec.current.state # an example is running
            tag = SpecTag.new
            tag.tag = 'slow'
            tag.comment = nil
            tag.description = "#{state.describe} #{state.it}"
            if MSpec.write_tag(tag)
              STDERR.puts "\nAdded slow tag for #{tag.description}"
            end

            # Make sure to notice when there is a missing slow tag
            unless missing_slow_tags
              missing_slow_tags = true
              MSpec.protect 'slow tags' do
                raise 'There were missing slow tags'
              end
            end
          end

          original.bind(self).call(*args, &block)
        end
        # Keep visibility for Kernel instance methods
        private meth if klass == Kernel
      end
    end
  end
end
