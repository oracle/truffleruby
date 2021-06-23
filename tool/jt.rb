#!/usr/bin/env ruby
# encoding: utf-8

# Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# A workflow tool for TruffleRuby development

require 'fileutils'
require 'timeout'
require 'rbconfig'
require 'pathname'
autoload :JSON, 'json'

if RUBY_ENGINE != 'ruby' && !RUBY_DESCRIPTION.include?('Native')
  STDERR.puts 'WARNING: jt is not running on MRI or TruffleRuby Native, startup is slow'
  STDERR.puts '  Consider using following bash alias to run on MRI.'
  STDERR.puts '  `alias jt=/path/to/mri/bin/ruby /path/to/truffleruby/tool/jt.rb`'
end

abort "ERROR: jt requires Ruby 2.3 and above, was #{RUBY_VERSION}" if (RUBY_VERSION.split('.').map(&:to_i) <=> [2, 3, 0]) < 0

TRUFFLERUBY_DIR = File.expand_path('../..', File.realpath(__FILE__))
GRAAL_DIR = File.expand_path('../graal', TRUFFLERUBY_DIR)
PROFILES_DIR = "#{TRUFFLERUBY_DIR}/profiles"

CACHE_EXTRA_DIR = File.expand_path('~/.mx/cache/truffleruby')
FileUtils.mkdir_p(CACHE_EXTRA_DIR)

TRUFFLERUBY_GEM_TEST_PACK_VERSION = '58d3048b49629102945a60918f31bf74547f51f9'

JDEBUG = '--vm.agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y'
METRICS_REPS = Integer(ENV['TRUFFLERUBY_METRICS_REPS'] || 10)
DEFAULT_PROFILE_OPTIONS = %w[--cpusampler --cpusampler.Mode=roots --cpusampler.Output=json]

RUBOCOP_INCLUDE_LIST = %w[
  lib/cext
  lib/truffle
  src/main/ruby/truffleruby
  src/test/ruby
  tool/docker.rb
  tool/jt.rb
  spec/truffle
]

RUBOCOP_VERSION = '0.66.0'

DLEXT = RbConfig::CONFIG['DLEXT']

JT_PROFILE_SUBCOMMANDS = ENV['JT_PROFILE_SUBCOMMANDS'] == 'true'

# Expand GEM_HOME relative to cwd so it cannot be misinterpreted later.
ENV['GEM_HOME'] = File.expand_path(ENV['GEM_HOME']) if ENV['GEM_HOME']

MRI_TEST_RELATIVE_PREFIX = 'test/mri/tests'
MRI_TEST_PREFIX = "#{TRUFFLERUBY_DIR}/#{MRI_TEST_RELATIVE_PREFIX}"
MRI_TEST_CEXT_DIR = "#{MRI_TEST_PREFIX}/cext-c"
MRI_TEST_CEXT_LIB_DIR = "#{TRUFFLERUBY_DIR}/.ext/c"

# A list of MRI C API tests we can load. Tests that do not load at all are in failing.exclude.
MRI_TEST_CAPI_TESTS = File.readlines("#{TRUFFLERUBY_DIR}/test/mri/capi_tests.list").map(&:chomp)

MRI_TEST_MODULES = {
    '--openssl' => {
        help: 'include only openssl tests',
        include: openssl = ['openssl/test_*.rb'],
    },
    '--syslog' => {
        help: 'include only syslog tests',
        include: syslog = [
            'test_syslog.rb',
            'syslog/test_syslog_logger.rb'
        ]
    },
    '--cexts' => {
        help: 'run all MRI tests testing C extensions',
        include: cexts = openssl + syslog + [
            'etc/test_etc.rb',
            'nkf/test_kconv.rb',
            'nkf/test_nkf.rb',
            'zlib/test_zlib.rb',
        ]
    },
    '--capi' => {
      help: 'run all C-API MRI tests',
      include: capi = MRI_TEST_CAPI_TESTS,
    },
    '--all-sulong' => {
        help: 'run all tests requiring Sulong (C exts and C API)',
        include: all_sulong = cexts + capi
    },
    '--no-sulong' => {
        help: 'exclude all tests requiring Sulong',
        exclude: all_sulong
    },
}

SUBPROCESSES = []

# Forward signals to sub-processes, so they get notified when sending a signal to jt
[:SIGINT, :SIGTERM].each do |signal|
  trap(signal) do
    # Enables command line signals (e.g. Ctrl+C) when waiting for input if there are no subprocesses.
    raise Interrupt if SUBPROCESSES.empty?

    SUBPROCESSES.each do |pid|
      Utilities.send_signal(signal, pid)
    end
    # Keep running jt which will wait for the subprocesses termination
  end
end

module Utilities
  private

  def linux?
    @linux ||= RbConfig::CONFIG['host_os'].include?('linux')
  end

  def darwin?
    @darwin ||= RbConfig::CONFIG['host_os'].include?('darwin')
  end

  def ci?
    ENV.key?('BUILD_URL')
  end

  def bold(text)
    STDOUT.tty? ? "\e[1m#{text}\e[22m" : text
  end

  def get_truffle_version(from: :suite)
    case from
    when :suite
      suite = File.read("#{TRUFFLERUBY_DIR}/mx.truffleruby/suite.py")
      raise unless /"name": "sulong",.+?"version": "(\h{40})"/m =~ suite
      $1
    when :repository
      raw_sh('git', 'rev-parse', 'HEAD', capture: :out, no_print_cmd: true, chdir: GRAAL_DIR).chomp
    else
      raise ArgumentError, from: from
    end
  end

  def jvmci_version
    @jvmci_version ||= begin
      ci = File.read("#{TRUFFLERUBY_DIR}/common.json")
      if @jdk_version == 8
        regex = /{\s*"name"\s*:\s*"openjdk"\s*,\s*"version"\s*:\s*"8u.+-(jvmci-[^"]+)"\s*,/
      else
        regex = /{\s*"name"\s*:\s*"labsjdk"\s*,\s*"version"\s*:\s*"ce-11\..+-(jvmci-[^"]+)"\s*,/
      end
      raise 'JVMCI version not found in common.json' unless regex =~ ci
      $1
    end
  end

  def send_signal(signal, pid)
    STDERR.puts "\nSending #{signal} to process #{pid}"
    begin
      Process.kill(signal, pid)
    rescue Errno::ESRCH
      # Already killed
      STDERR.puts "Process #{pid} not found"
      nil
    end
  end
  module_function :send_signal

  def which(binary)
    ENV['PATH'].split(File::PATH_SEPARATOR).each do |dir|
      path = "#{dir}/#{binary}"
      return path if File.executable? path
    end
    nil
  end

  def find_mx
    if which('mx')
      'mx'
    else
      mx_repo = find_or_clone_repo('https://github.com/graalvm/mx.git')
      "#{mx_repo}/mx"
    end
  end

  def ruby_launcher
    return @ruby_launcher if defined? @ruby_launcher

    @ruby_name ||= ENV['RUBY_BIN'] || ENV['JT_ENV'] || 'jvm'
    ruby_launcher = if @ruby_name == 'ruby'
                      ENV['RBENV_ROOT'] ? `rbenv which ruby`.chomp : which('ruby')
                    elsif @ruby_name.start_with?('/')
                      File.directory?(@ruby_name) ? "#{@ruby_name}/bin/ruby" : @ruby_name
                    else
                      graalvm = "#{TRUFFLERUBY_DIR}/mxbuild/truffleruby-#{@ruby_name}"
                      "#{graalvm}/#{language_dir(graalvm)}/ruby/bin/ruby"
                    end

    raise "The Ruby executable #{ruby_launcher} does not exist" unless File.exist?(ruby_launcher)
    raise "The Ruby executable #{ruby_launcher} is not executable" unless File.executable?(ruby_launcher)

    @ruby_launcher = ruby_launcher
    @ruby_launcher_realpath = File.realpath(ruby_launcher)

    unless @silent
      shortened_path = @ruby_launcher.sub(%r[^#{Regexp.escape TRUFFLERUBY_DIR}/], '').sub(%r[/bin/(ruby|truffleruby)$], '')
      shortened_path = shortened_path.sub(%r[/#{language_dir(graalvm_home)}/ruby$], '') if graalvm_home
      tags = [*('Native' if truffleruby_native?),
              *('Interpreted' if truffleruby? && !truffleruby_compiler?),
              truffleruby? ? 'TruffleRuby' : 'a Ruby',
              *('with Graal' if truffleruby_compiler?)]
      STDERR.puts "Using #{tags.join(' ')}: #{shortened_path}"
    end

    # use same ruby_launcher in subprocess jt instances
    # cannot be set while building
    ENV['RUBY_BIN'] = @ruby_launcher
    @ruby_launcher
  end
  alias_method :require_ruby_launcher!, :ruby_launcher

  def ruby_home
    require_ruby_launcher!
    File.expand_path('../..', @ruby_launcher_realpath)
  end

  def graalvm_home
    up = if ruby_home.end_with?('jre/languages/ruby')
           3
         elsif ruby_home.end_with?('languages/ruby')
           2
         else
           nil # standalone
         end
    return nil unless up
    File.expand_path((['..'] * up).join('/'), ruby_home)
  end

  def truffleruby_native!
    unless truffleruby_native?
      raise "The ruby executable #{ruby_launcher} is not native."
    end
  end

  def truffleruby_compiler!
    unless truffleruby_compiler?
      raise "The ruby executable #{ruby_launcher} does not have Graal.\nUse `jt build --env jvm-ce` and `jt --use jvm-ce <command> ...`."
    end
  end

  def truffleruby_native?
    return @truffleruby_native if defined?(@truffleruby_native)
    # the truffleruby executable is bigger than 10MB if it is a native executable
    # the executable delegator for mac has less than 1MB
    @truffleruby_native = truffleruby? && File.size(truffleruby_launcher_path) > 10*1024*1024
  end

  def truffleruby_compiler?
    return @truffleruby_compiler if defined?(@truffleruby_compiler)

    return @truffleruby_compiler = false unless truffleruby?
    return @truffleruby_compiler = true if truffleruby_native?

    # Detect if the compiler is present by reading the $graalvm_home/release file
    @truffleruby_compiler = File.readlines("#{graalvm_home}/release").grep(/^COMMIT_INFO=/).any? do |line|
      line.include?('"compiler":') || line.include?("'compiler':")
    end
  end

  def truffleruby?
    # only truffleruby has sibling truffleruby executable
    @truffleruby ||= File.executable?(truffleruby_launcher_path)
  end

  def truffleruby_jvm?
    truffleruby? and !truffleruby_native?
  end

  def jdk8?
    graalvm_home and !Dir.exist?("#{graalvm_home}/jmods")
  end

  def truffleruby_launcher_path
    require_ruby_launcher!
    @truffleruby_launcher_path ||= File.expand_path('../truffleruby', @ruby_launcher_realpath)
  end

  def truffleruby!
    raise 'This command requires TruffleRuby.' unless truffleruby?
  end

  def find_or_clone_repo(url, commit=nil)
    name = File.basename url, '.git'
    path = File.expand_path("../#{name}", TRUFFLERUBY_DIR)
    unless Dir.exist? path
      target = "../#{name}"
      sh 'git', 'clone', url, target
      sh 'git', 'checkout', commit, chdir: target if commit
    end
    path
  end

  def git_branch
    @git_branch ||= `GIT_DIR="#{TRUFFLERUBY_DIR}/.git" git rev-parse --abbrev-ref HEAD`.strip
  end

  def no_gem_vars_env
    {
      'GEM_HOME' => nil,
      'GEM_PATH' => nil,
    }
  end

  def human_size(bytes)
    if bytes < 1024
      "#{bytes} B"
    elsif bytes < 1000**2
      "#{(bytes/1024.0).round(2)} KB"
    elsif bytes < 1000**3
      "#{(bytes/1024.0**2).round(2)} MB"
    elsif bytes < 1000**4
      "#{(bytes/1024.0**3).round(2)} GB"
    else
      "#{(bytes/1024.0**4).round(2)} TB"
    end
  end

  def log(tty_message, full_message)
    if STDERR.tty?
      STDERR.print tty_message unless tty_message.nil?
    else
      STDERR.print full_message unless full_message.nil?
    end
  end

  def diff(expected, actual)
    `diff -u #{expected} #{actual}`
  end

  def raw_sh_failed_status
    `false`
    $?
  end

  def terminate_process(pid, timeout = 10)
    send_signal(:SIGTERM, pid)
    begin
      Timeout.timeout(timeout) do
        Process.wait pid
      end
    rescue Timeout::Error
      send_signal(:SIGKILL, pid)
      Process.wait pid
    end
    STDERR.puts "Process #{pid} terminated"
  end

  def raw_sh_with_timeout(timeout, pid)
    if !timeout
      yield
    else
      begin
        Timeout.timeout(timeout) do
          yield
        end
      rescue Timeout::Error
        if send_signal(:SIGTERM, pid)
          sleep 1
          send_signal(:SIGKILL, pid)
        end
        yield # Wait and read the pipe if :capture
        :timeout
      end
    end
  end

  def raw_sh_track_subprocess(pid)
    SUBPROCESSES << pid
    yield
  ensure
    SUBPROCESSES.delete(pid)
  end

  def raw_sh(*args)
    options = args.last.is_a?(Hash) ? args.last : {}
    continue_on_failure = options.delete :continue_on_failure
    use_exec = options.delete :use_exec
    timeout = options.delete :timeout
    capture = options.delete :capture

    unless options.delete :no_print_cmd
      STDERR.puts bold "$ #{printable_cmd(args)}"
    end

    exec(*args) if use_exec

    if capture
      capture_modes = [:out, :err, :both]
      raise ":capture can only be one of #{capture_modes}" unless capture_modes.include?(capture)
      pipe_r, pipe_w = IO.pipe
      options[:out] = pipe_w if capture == :out || capture == :both
      options[:err] = pipe_w if capture == :err || capture == :both
    end

    status = nil
    out = nil
    start = Process.clock_gettime(Process::CLOCK_MONOTONIC) if JT_PROFILE_SUBCOMMANDS
    begin
      pid = Process.spawn(*args)
    rescue Errno::ENOENT => no_such_executable
      STDERR.puts bold no_such_executable
      status = raw_sh_failed_status
    else
      raw_sh_track_subprocess(pid) do
        pipe_w.close if capture

        result = raw_sh_with_timeout(timeout, pid) do
          out = pipe_r.read if capture
          _, status = Process.waitpid2(pid)
        end
        if result == :timeout
          status = raw_sh_failed_status
        end
      end
    end

    if JT_PROFILE_SUBCOMMANDS
      finish = Process.clock_gettime(Process::CLOCK_MONOTONIC)
      STDERR.puts "\n[jt] #{printable_cmd(args)} took #{'%.3f' % (finish - start)}s\n\n"
    end

    if capture
      pipe_r.close
    end

    if status.success? || continue_on_failure
      if capture
        out
      else
        status.success?
      end
    else
      STDERR.puts "FAILED (#{status}): #{printable_cmd(args)}"
      STDERR.puts out if capture
      exit(status.exitstatus || status.termsig || status.stopsig || 1)
    end
  end

  def printable_cmd(args)
    env = {}
    if Hash === args.first
      env, *args = args
    end
    if Hash === args.last && args.last.empty?
      *args, _options = args
    end

    raise 'use multiple arguments instead of a single string with spaces' if args[0].include?(' ')
    sets = []
    unsets = []
    env.each_pair do |k, v|
      if v
        sets << "#{k}=#{shellescape(v)}"
      else
        unsets << k
      end
    end
    args = args.map { |a| shellescape(a) }

    all = [*("unset #{unsets.join(' ')};" unless unsets.empty?), *sets, *args]
    size = all.reduce(0) { |s, v| s + v.size }
    all.join(size <= 180 ? ' ' : " \\\n  ")
  end

  def shellescape(str)
    return str unless str.is_a?(String)
    if str.include?(' ')
      if str.include?("'")
        require 'shellwords'
        Shellwords.escape(str)
      else
        "'#{str}'"
      end
    else
      str
    end
  end

  def replace_env_vars(string, env = ENV)
    string.gsub(/\$([A-Z_]+)/) do
      var = $1
      abort "You need to set $#{var}" unless env[var]
      env[var]
    end
  end

  def sh(*args)
    chdir(TRUFFLERUBY_DIR) do
      raw_sh(*args)
    end
  end

  def app_open(file)
    cmd = darwin? ? 'open' : 'xdg-open'

    sh cmd, file
  end

  def chdir(dir, &block)
    raise LocalJumpError, 'no block given' unless block_given?
    if dir == Dir.pwd
      yield
    else
      STDERR.puts "$ cd #{dir}"
      ret = Dir.chdir(dir, &block)
      STDERR.puts "$ cd #{Dir.pwd}"
      ret
    end
  end

  def find_java_home
    @java_home ||= begin
      mx_env = File.expand_path('~/.mx/env')
      if File.readable?(mx_env)
        if line = File.readlines(mx_env).grep(/^JAVA_HOME=/).first
          java_home = line.strip.split('=', 2).last
        end
      end
      java_home ||= ENV['JAVA_HOME']

      if java_home
        java_home = File.realpath(java_home)
        if java_home.include?(jvmci_version)
          return :use_env_java_home
        end

        java_version_output = `#{java_home}/bin/java -version 2>&1`
        if java_version_output.include?(jvmci_version)
          :use_env_java_home
        elsif java_version_output.include?('jvmci')
          warn "warning: JAVA_HOME=#{java_home} is not the same JVMCI version as in common.json (#{jvmci_version})"
          :use_env_java_home
        else
          raise "$JAVA_HOME does not seem to point to a JVMCI-enabled JDK (`#{java_home}/bin/java -version` does not contain 'jvmci')"
        end
      else
        raise '$JAVA_HOME should be set in CI' if ci?
        install_jvmci(
            "$JAVA_HOME is not set, downloading JDK#{@jdk_version} with JVMCI",
            (@mx_env || @ruby_name || '').include?('ee'))
      end
    end
  end

  def language_dir(graalvm_home)
    raise "GraalVM #{graalvm_home} does not exist" unless Dir.exist?(graalvm_home)
    if Dir.exist?("#{graalvm_home}/jmods")
      'languages'
    else
      'jre/languages'
    end
  end

  def mx(*args, java_home: find_java_home, **options)
    mx_args = args.dup

    env = mx_args.first.is_a?(Hash) ? mx_args.shift : {}
    if java_home == :use_env_java_home
      # mx reads $JAVA_HOME
    elsif java_home == :none
      # Make sure $JAVA_HOME is not set, as a wrong value causes mx to abort
      env['JAVA_HOME'] = nil
    else
      mx_args.unshift '--java-home', java_home
    end

    raw_sh(env, find_mx, *mx_args, **options)
  end

  def mx_os
    if darwin?
      'darwin'
    elsif linux?
      'linux'
    else
      abort 'Unknown OS'
    end
  end

  def bitbucket_url(repo)
    unless Remotes.bitbucket
      raise 'Need a git remote in truffleruby with the internal repository URL'
    end
    Remotes.url(Remotes.bitbucket).sub('truffleruby', repo)
  end

  def git_clone(url, path)
    if ci?
      # Use mx sclone to use the git cache in CI
      # Unset $JAVA_HOME, mx sclone should not use it, and $JAVA_HOME might be set to the standalone home in graalvm tests
      mx('sclone', '--kind', 'git', url, path, java_home: :none)
    else
      raw_sh 'git', 'clone', url, path
    end
  end

  def run_mspec(env_vars, command = 'run', *args)
    mspec_args = ['spec/mspec/bin/mspec', command, '--config', 'spec/truffleruby.mspec']
    Dir.chdir(TRUFFLERUBY_DIR) do
      ruby env_vars, *mspec_args, '-t', ruby_launcher, *args
    end
  end

  def args_split(args)
    delimiter_index = args.index('--')
    return [args, []] unless delimiter_index
    [args[0...delimiter_index], args[(delimiter_index + 1)..-1]]
  end

  def with_color(color_code, &block)
    print color_code if STDOUT.tty?
    begin
      result = yield block
    ensure
      print TERM_COLOR_DEFAULT if STDOUT.tty?
    end
    result
  end

  def boxed(&block)
    puts ''
    puts '============================================================'
    puts ''
    result = yield block
    puts ''
    puts '============================================================'
    puts ''
    result
  end

  # https://misc.flogisoft.com/bash/tip_colors_and_formatting
  TERM_COLOR_RED = "\e[31m"
  TERM_COLOR_DEFAULT = "\e[39m"
end

module Remotes
  include Utilities
  extend self

  def bitbucket(dir = TRUFFLERUBY_DIR)
    candidate = remote_urls(dir).find { |_r, u| u.include? 'ol-bitbucket' }
    candidate.first if candidate
  end

  def github(dir = TRUFFLERUBY_DIR)
    candidate = remote_urls(dir).find { |_r, u| u.match %r(github.com[:/]oracle) }
    candidate.first if candidate
  end

  def remote_urls(dir = TRUFFLERUBY_DIR)
    @remote_urls ||= Hash.new
    @remote_urls[dir] ||= begin
      out = raw_sh 'git', '-C', dir, 'remote', capture: :out, no_print_cmd: true
      out.split.map do |remote|
        url = raw_sh 'git', '-C', dir, 'config', '--get', "remote.#{remote}.url", capture: :out, no_print_cmd: true
        [remote, url.chomp]
      end
    end
  end

  def url(remote_name, dir = TRUFFLERUBY_DIR)
    remote_urls(dir).find { |r, _u| r == remote_name }.last
  end
end

module Commands
  include Utilities

  def help
    puts <<~TXT
      Usage: jt [options] COMMAND [command-options]
          Where options are:
          --build                   Runs `jt build` before the command
          --rebuild                 Runs `jt rebuild` before the command
          --use|-u [RUBY_SELECTOR]  Specifies which Ruby interpreter should be used in a given command. jt will apply
                                    options based on the given Ruby interpreter. Allowed values are:
                                    * name given to the --name option during build
                                    * absolute path to a Ruby interpreter executable
                                    * prefix of a Ruby interpreter
                                    * 'ruby' which uses the current Ruby executable in the PATH
                                    Default value is --use jvm, therefore all commands run on truffleruby-jvm by default.
                                    The default can be changed with `export RUBY_BIN=RUBY_SELECTOR`
          --silent                  Does not print the command and which Ruby is used
          --jdk                     Specifies which version of the JDK should be used: 8, 11 (default) or 16

      jt build [graalvm|parser|options] ...   by default it builds graalvm
        jt build [parser|options] [options]
            parser                            build the parser
            options                           build the options
            core-symbols                      build the core symbols
        jt build graalvm [options] [mx options] [-- mx build options]
            graalvm                           build a GraalVM based on the given env file, the default is a minimal
                                              GraalVM with JVM and Truffleruby only available in mxbuild/truffleruby-jvm,
                                              the Ruby is symlinked into rbenv or chruby if available
            options:
              --sforceimports                 run `mx sforceimports` before building (default: false)
              --env|-e                        mx env file used to build the GraalVM, default is "jvm"
              --name|-n NAME                  specify the name of the build "mxbuild/truffleruby-NAME",
                                              it is also linked in your ruby manager (if found) under the same name,
                                              by default it is the name of the mx env file,
                                              the named build stays until it is rebuilt or deleted manually
            mx options                        options passed directly to mx
            mx build options                  options passed to the 'build' command of mx

      jt build_stats [--json] <attribute>            prints attribute's value from build process (e.g., binary size)
      jt clean                                       clean
      jt env                                         prints the current environment
      jt rebuild [build options]                     clean and build
      jt ruby [jt options] [--] [ruby options] args...
                                                     run TruffleRuby with args
          --stress        stress the compiler (compile immediately, foreground compilation, compilation exceptions are fatal)
          --reveal        enable assertions
          --asm           show assembly
          --igv           dump select Graal graphs to graal_dumps/ (-Dgraal.Dump=Truffle:1)
          --igv-full      dump all Graal graphs to graal_dumps/ (-Dgraal.Dump=Truffle:2)
          --infopoints    show source location for each node in IGV
          --fg            disable background compilation
          --trace         show compilation information on stdout
          --jdebug        run a JDWP debug server on port 8000
          --jexception[s] print java exceptions
          --exec          use exec rather than system
      jt gem                                         shortcut for `jt ruby -S gem`, to install Ruby gems, etc
      jt e 14 + 2                                    evaluate an expression
      jt puts 14 + 2                                 evaluate and print an expression
      jt cextc directory clang-args                  compile the C extension in directory, with optional extra clang arguments
      jt test                                        run all mri tests, specs and integration tests
      jt test basictest                              run MRI's basictest suite
      jt test bootstraptest                          run MRI's bootstraptest suite
      jt test mri                                    run mri tests
      #{MRI_TEST_MODULES.map { |k, h| format ' ' * 4 + '%-16s%s', k, h[:help] }.join("\n")}
      jt test mri test/mri/tests/test_find.rb [-- <MRI runner options>]
                                                     run tests in given file, -n option of the runner can be used to further
                                                     limit executed test methods
      jt lint                                        run all lints, used in CI
      jt lint fast                                   run fast lints, recommended as a git hook (see tool/hooks/lint-check.sh)
      jt test specs [fast] [mspec arguments] [-- ruby options]
      jt test specs                                  run all specs
      jt test specs fast                             run all specs except sub-processes, GC, sleep, ...
      jt test spec/ruby/language                     run specs in this directory
      jt test spec/ruby/language/while_spec.rb       run specs in this file
      jt test ... -- --jdebug                        run specs with the Java debugger
      jt test compiler                               run compiler tests
      jt test integration [TESTS]                    run integration tests
      jt test bundle [--jdebug]                      tests using bundler
      jt test gems [TESTS]                           tests using gems
      jt test ecosystem [TESTS] [options]            tests using the wider ecosystem such as bundler, Rails, etc
      jt test cexts [--no-openssl] [--no-gems] [test_names...]
                                                     run C extension tests (set GEM_HOME)
      jt test unit [unittest flags] [-- mx options]  run Java unittests
      jt test tck                                    run tck tests
      jt gem-test-pack                               check that the gem test pack is downloaded, or download it for you, and print the path
      jt rubocop [rubocop options]                   run rubocop rules (using ruby available in the environment)
      jt tag spec/ruby/language                      tag failing specs in this directory
      jt tag spec/ruby/language/while_spec.rb        tag failing specs in this file
      jt tag all spec/ruby/language                  tag all specs in this file, without running them
      jt untag ...                                   untag passing specs
      jt purge ...                                   remove tags without specs
      jt mspec ...                                   run MSpec with the TruffleRuby configuration and custom arguments
      jt metrics alloc [--json] ...                  how much memory is allocated running a program
      jt metrics instructions ...                    how many CPU instructions are used to run a program
      jt metrics minheap ...                         what is the smallest heap you can use to run an application
      jt metrics time ...                            how long does it take to run a command, broken down into different phases
      jt benchmark args... [-- ruby options]         run benchmark-interface
                                       note that to run most MRI benchmarks, you should translate them first with normal
                                       Ruby and cache the result, such as benchmark bench/mri/bm_vm1_not.rb --cache
                                       jt benchmark bench/mri/bm_vm1_not.rb --use-cache
      jt profile                                    profiles an application, including the TruffleRuby runtime, and generates a flamegraph
      jt igv                                        launches IdealGraphVisualizer
      jt next                                       tell you what to work on next (give you a random core library spec)
      jt install [jvmci|eclipse]                    install [the right JVMCI JDK | Eclipse] in the parent directory
      jt docker                                     build a Docker image - see doc/contributor/docker.md
      jt sync                                       continuously synchronize changes from the Ruby source files to the GraalVM build
      jt idea                                       generates IntelliJ projects
      jt format                                     run eclipse code formatter
      jt graalvm-home                               prints the GraalVM home of the RUBY_SELECTOR

      you can also put --build or --rebuild in front of any command to build or rebuild first

      recognised environment variables:

        RUBY_BIN                                     The TruffleRuby executable to use (normally just bin/truffleruby)
        JAVA_HOME                                    Path to the JVMCI JDK used for building with mx
        OPENSSL_PREFIX                               Where to find OpenSSL headers and libraries
        ECLIPSE_EXE                                  Where to find Eclipse
        SYSTEM_RUBY                                  The Ruby interpreter to run 'jt' itself, when using 'bin/jt'
        JT_JDK                                       The default JDK version to use: 8, 11 (default) or 16
        JT_ENV                                       The default value for 'jt build --env JT_ENV' and for 'jt --use JT_ENV'
        JT_PROFILE_SUBCOMMANDS                       Print the time each subprocess takes on stderr
    TXT
  end

  def truffle_version
    puts get_truffle_version
  end

  def mx(*args)
    super(*args)
  end
  ruby2_keywords :mx if respond_to?(:ruby2_keywords, true)

  def launcher
    puts ruby_launcher
  end

  define_method(:'graalvm-home') do
    puts graalvm_home
  end

  def build(*options)
    project = options.shift
    case project
    when 'parser'
      jay = "#{TRUFFLERUBY_DIR}/tool/jay"
      raw_sh 'make', chdir: jay
      ENV['PATH'] = "#{jay}:#{ENV['PATH']}"
      sh 'bash', 'tool/generate_parser'
      yytables = 'src/main/java/org/truffleruby/parser/parser/YyTables.java'
      File.write(yytables, File.read(yytables).gsub('package org.jruby.parser;', 'package org.truffleruby.parser.parser;'))
    when 'options'
      sh 'tool/generate-options.rb'
    when 'core-symbols'
      sh 'tool/generate-core-symbols.rb'
    else
      build_graalvm(*project, *options)
    end
  end

  def clean(*options)
    project = options.shift
    case project
    when 'cexts'
      mx 'clean', '--dependencies', 'org.truffleruby.cext'
    when nil
      mx 'clean'
      sh 'rm', '-rf', 'spec/ruby/ext'
      Dir.glob("#{TRUFFLERUBY_DIR}/mxbuild/{*,.*}") do |path|
        next if File.basename(path).start_with?('truffleruby-')
        next if File.basename(path).start_with?('toolchain')
        next if %w(. ..).include? File.basename(path)
        sh 'rm', '-rf', path
      end
    else
      raise ArgumentError, project
    end
  end

  def env
    puts 'Environment'
    env_vars = %w[JAVA_HOME PATH RUBY_BIN OPENSSL_PREFIX TRUFFLERUBYOPT RUBYOPT]
    column_size = env_vars.map(&:size).max
    env_vars.each do |e|
      puts format "%#{column_size}s: %s", e, ENV[e].inspect
    end
    shell = -> command { raw_sh(*command.split, continue_on_failure: true) }
    shell['ruby -v']
    shell['uname -a']
    shell['cc -v']
    shell['gcc -v']
    shell['clang -v']
    shell['opt -version']
    shell['brew --prefix llvm@4']
    begin
      llvm = `brew --prefix llvm@4`.chomp
      shell["#{llvm}/bin/clang -v"]
      shell["#{llvm}/bin/opt -version"]
    rescue Errno::ENOENT # rubocop:disable Lint/HandleExceptions
      # No Homebrew
    end
    shell['mx version']
    sh('mx', 'sversions', continue_on_failure: true)
    shell['git --no-pager show -s --format=%H']
    if ENV['OPENSSL_PREFIX']
      shell["#{ENV['OPENSSL_PREFIX']}/bin/openssl version"]
    else
      shell['openssl version']
    end
    shell['java -version']
  end

  def rebuild(*options)
    clean
    build(*options)
  end

  private def ruby_options(options, args)
    raise ArgumentError, args.inspect + ' has non-String values' if args.any? { |v| not v.is_a? String }

    ruby_args = []
    vm_args = []

    core_load_path = true
    experimental_options_added = false

    add_experimental_options = -> do
      unless experimental_options_added
        experimental_options_added = true
        vm_args << '--experimental-options'
      end
    end

    while (arg = args.shift)
      case arg
      when '--no-core-load-path'
        core_load_path = false
      when '--reveal'
        vm_args += %w[--vm.ea --vm.esa] if truffleruby_jvm?
      when '--check-compilation'
        add_experimental_options.call
        vm_args << '--engine.CompilationFailureAction=ExitVM'
        vm_args << '--engine.TreatPerformanceWarningsAsErrors=all'
      when '--stress'
        add_experimental_options.call
        vm_args << '--engine.CompileImmediately'
        vm_args << '--engine.BackgroundCompilation=false'
        args.unshift '--check-compilation'
      when '--asm'
        vm_args += %w[--vm.XX:+UnlockDiagnosticVMOptions --vm.XX:CompileCommand=print,*::callRoot]
      when '--jacoco'
        vm_args << jacoco_args
      when '--jdebug'
        vm_args << JDEBUG
      when '--jexception', '--jexceptions'
        add_experimental_options.call
        vm_args << '--exceptions-print-uncaught-java=true'
      when '--infopoints'
        if truffleruby_jvm?
          vm_args << '--vm.XX:+UnlockDiagnosticVMOptions' << '--vm.XX:+DebugNonSafepoints'
        end
        vm_args << '--engine.NodeSourcePositions=true'
      when '--fg'
        add_experimental_options.call
        vm_args << '--engine.BackgroundCompilation=false'
      when '--trace'
        truffleruby_compiler!
        vm_args << '--engine.TraceCompilation'
      when '--igv', '--igv-full'
        truffleruby_compiler!
        vm_args << (arg == '--igv-full' ? '--vm.Dgraal.Dump=Truffle:2' : '--vm.Dgraal.Dump=Truffle:1')
        vm_args << '--vm.Dgraal.PrintBackendCFG=false'
      when '--exec'
        options[:use_exec] = true
      when '--'
        # marks rest of the options as Ruby arguments, stop parsing jt options
        break
      else
        ruby_args.push arg
        # do not continue looking for jt options when we encounter an argument
        break unless arg.start_with? '-'
      end
    end

    if core_load_path and truffleruby_jvm?
      add_experimental_options.call
      vm_args << "--core-load-path=#{TRUFFLERUBY_DIR}/src/main/ruby/truffleruby"
    end

    if ci? and truffleruby_jvm? and !jdk8?
      vm_args << '--vm.Xlog:os+thread=off' # GR-23507: prevent thread warnings on stdout to break specs/tests
    end

    [vm_args, ruby_args + args, options]
  end

  private def run_ruby(*args)
    env_vars = args.first.is_a?(Hash) ? args.shift : {}
    options = args.last.is_a?(Hash) ? args.pop : {}

    vm_args, ruby_args, options = ruby_options(options, args)

    options[:no_print_cmd] = true if @silent

    raw_sh env_vars, ruby_launcher, *(vm_args if truffleruby?), *ruby_args, options
  end

  def ruby(*args)
    require_ruby_launcher!
    env = args.first.is_a?(Hash) ? args.shift : {}
    run_ruby(env, '--exec', *args)
  end

  # Legacy alias
  alias_method :run, :ruby

  def e(*args)
    ruby '-e', args.join(' ')
  end

  def command_puts(*args)
    e 'puts begin', *args, 'end'
  end

  def command_p(*args)
    e 'p begin', *args, 'end'
  end

  # Just convenience
  def gem(*args)
    ruby '-S', 'gem', *args
  end

  def cextc(cext_dir, *clang_opts)
    require_ruby_launcher!
    cext_dir = File.expand_path(cext_dir)
    name = File.basename(cext_dir)
    ext_dir = "#{cext_dir}/ext/#{name}"
    target = "#{cext_dir}/lib/#{name}/#{name}.#{DLEXT}"
    compile_cext(name, ext_dir, target, clang_opts)
  end

  private def compile_cext(name, ext_dir, target, clang_opts, env: {})
    extconf = "#{ext_dir}/extconf.rb"
    raise "#{extconf} does not exist" unless File.exist?(extconf)

    chdir(ext_dir) do
      run_ruby(env, '-rmkmf', "#{ext_dir}/extconf.rb") # -rmkmf is required for C ext tests
      if File.exist?('Makefile')
        raw_sh('make')
        FileUtils::Verbose.cp("#{name}.#{DLEXT}", target) if target
      else
        STDERR.puts "Makefile not found in #{ext_dir}, skipping make."
      end
    end
  end

  def test(*args)
    require_ruby_launcher!
    path, *rest = args

    case path
    when nil
      %w[specs mri bundle cexts integration gems ecosystem compiler].each do |kind|
        jt('test', kind)
      end
    when 'bundle' then test_bundle(*rest)
    when 'compiler' then test_compiler(*rest)
    when 'cexts' then test_cexts(*rest)
    when 'report' then test_report(*rest)
    when 'integration' then test_integration(*rest)
    when 'gems' then test_gems(*rest)
    when 'ecosystem' then test_ecosystem(*rest)
    when 'specs' then test_specs('run', *rest)
    when 'basictest' then test_basictest(*rest)
    when 'bootstraptest' then test_bootstraptest(*rest)
    when 'mri' then test_mri(*rest)
    when 'unit', 'unittest'
      unittest_args, mx_options = args_split(rest)
      unittest_options, tests = unittest_args.partition { |arg| arg.start_with?('-') }
      tests = tests.empty? ? ['org.truffleruby'] : tests
      # TODO (eregon, 4 Feb 2019): This should run on GraalVM, not development jars
      mx(*mx_options, 'unittest', *unittest_options, *tests)
    when 'tck' then mx 'tck', *rest
    else
      if File.expand_path(path, TRUFFLERUBY_DIR).start_with?("#{TRUFFLERUBY_DIR}/test")
        test_mri(*args)
      else
        test_specs('run', *args)
      end
    end
  end

  private def jt(*args)
    sh RbConfig.ruby, 'tool/jt.rb', *args
  end

  private def test_basictest(*args)
    run_runner_test 'basictest/runner.rb', *args
  end

  private def test_bootstraptest(*args)
    run_runner_test 'bootstraptest/runner.rb', *args
  end

  private def run_runner_test(runner, *args)
    double_dash_index = args.index '--'
    if double_dash_index
      args, runner_args = args[0...double_dash_index], args[(double_dash_index+1)..-1]
    else
      runner_args = []
    end
    run_ruby(*args, "#{TRUFFLERUBY_DIR}/test/#{runner}", *runner_args)
  end

  private def test_mri(*args)
    double_dash_index = args.index '--'
    if double_dash_index
      args, runner_args = args[0...double_dash_index], args[(double_dash_index+1)..-1]
    else
      runner_args = []
    end

    mri_args = []
    excluded_files = File.readlines("#{TRUFFLERUBY_DIR}/test/mri/failing.exclude").
      map { |line| line.gsub(/#.*/, '').strip }.reject(&:empty?)
    patterns = []

    args.each do |arg|
      test_module = MRI_TEST_MODULES[arg]
      if test_module
        patterns.push(*test_module[:include])
        excluded_files.concat test_module[:exclude].to_a.flat_map { |pattern|
          Dir.glob("#{MRI_TEST_PREFIX}/#{pattern}").sort.map { |path| mri_test_name(path) }
        }
      elsif arg.start_with?('-')
        mri_args.push arg
      else
        patterns.push arg
      end
    end

    patterns.push "#{MRI_TEST_PREFIX}/**/test_*.rb" if patterns.empty?

    files_to_run = patterns.flat_map do |pattern|
      if pattern.start_with?(MRI_TEST_RELATIVE_PREFIX)
        pattern = "#{TRUFFLERUBY_DIR}/#{pattern}"
      elsif !pattern.start_with?(MRI_TEST_PREFIX)
        pattern = "#{MRI_TEST_PREFIX}/#{pattern}"
      end
      glob = Dir.glob(pattern).flat_map do |path|
        if File.directory?(path)
          Dir.glob("#{path}/**/test_*.rb")
        else
          path
        end
      end
      abort "pattern #{pattern} matched no files" if glob.empty?
      glob.map { |path| mri_test_name(path) }
    end.sort
    files_to_run -= excluded_files

    run_mri_tests(mri_args, files_to_run, runner_args, use_exec: true)
  end

  private def mri_test_name(test)
    prefix = "#{MRI_TEST_RELATIVE_PREFIX}/"
    abs_prefix = "#{MRI_TEST_PREFIX}/"
    if test.start_with?(prefix)
      test[prefix.size..-1]
    elsif test.start_with?(abs_prefix)
      test[abs_prefix.size..-1]
    else
      test
    end
  end

  private def run_mri_tests(extra_args, test_files, runner_args, run_options)
    abort 'No test files! (probably filtered out by failing.exclude)' if test_files.empty?
    test_files = test_files.map { |file| mri_test_name(file) }

    truffle_args = []
    if truffleruby?
      truffle_args += %w(--reveal --vm.Xmx2G)
    end

    env_vars = {
      'EXCLUDES' => 'test/mri/excludes',
      'RUBYGEMS_TEST_PATH' => "#{MRI_TEST_PREFIX}/rubygems",
      'RUBYOPT' => [*ENV['RUBYOPT'], '--disable-gems'].join(' '),
      'TRUFFLERUBYOPT' => [*ENV['TRUFFLERUBYOPT'], '--experimental-options', '--testing-rubygems'].join(' '),
    }
    compile_env = {
      # MRI C-ext tests expect to be built with $extmk = true.
      'MKMF_SET_EXTMK_TO_TRUE' => 'true',
    }

    cext_tests = test_files.select do |f|
      f.include?('cext-ruby') ||
      f == 'ruby/test_file_exhaustive.rb'
    end
    cext_tests.each do |test|
      puts
      puts test
      test_path = "#{MRI_TEST_PREFIX}/#{test}"
      match = File.read(test_path).match(/\brequire ['"]c\/(.*?)["']/)
      if match
        cext_name = match[1]
        compile_dir = if Dir.exist?("#{MRI_TEST_CEXT_DIR}/#{cext_name}")
                        "#{MRI_TEST_CEXT_DIR}/#{cext_name}"
                      elsif cext_name.include?('/')
                        "#{MRI_TEST_CEXT_DIR}/#{File.dirname(cext_name)}"
                      else
                        "#{MRI_TEST_CEXT_DIR}/#{cext_name.gsub('_', '-')}"
                      end
        # Remove depend files copied from MRI as they hardcode header locations
        FileUtils::Verbose.rm_f("#{compile_dir}/depend")

        name = File.basename(match[1])
        target_dir = if match[1].include?('/')
                       File.dirname(match[1])
                     else
                       ''
                     end
        dest_dir = File.join(MRI_TEST_CEXT_LIB_DIR, target_dir)
        FileUtils::Verbose.mkdir_p(dest_dir)
        compile_cext(name, compile_dir, dest_dir, [], env: compile_env)
      else
        puts "c require not found for cext test: #{test_path}"
      end
    end

    command = %w[test/mri/tests/runner.rb -v --color=never --tty=no -q]
    command.unshift("-I#{TRUFFLERUBY_DIR}/.ext")  if !cext_tests.empty?
    run_ruby(env_vars, *truffle_args, *extra_args, *command, *test_files, *runner_args, run_options)
  end

  def retag(*args)
    require_ruby_launcher!
    options, test_files = args.partition { |a| a.start_with?('-') }

    test_files.each do |test_file|
      puts '', test_file
      test_classes = File.read(test_file).scan(/class\s+([\w:]+)\s*<.+TestCase/).map(&:first)
      raise "Could not find class inheriting from TestCase in #{test_file}" if test_classes.empty?
      found_excludes = false
      test_classes.each do |test_class|
        prefix = "test/mri/excludes/#{test_class.gsub('::', '/')}"
        ["#{prefix}.rb", prefix].each do |file|
          if File.exist?(file)
            FileUtils::Verbose.rm_r file
            found_excludes = true
          end
        end
      end
      unless found_excludes
        puts "Found no excludes for #{test_classes.join(', ')}"
        next
      end

      puts '1. Tagging tests'
      output_file = 'mri_tests.txt'
      run_mri_tests(options, [test_file], [], out: output_file, continue_on_failure: true)

      puts '2. Parsing errors'
      sh 'ruby', 'tool/parse_mri_errors.rb', output_file

      puts '3. Verifying tests pass'
      run_mri_tests(options, [test_file], [], use_exec: test_files.size == 1)
    end
  end

  private def test_compiler(*args)
    truffleruby_compiler!
    env = {}
    env['TRUFFLERUBYOPT'] = [*ENV['TRUFFLERUBYOPT'], '--experimental-options', '--exceptions-print-java=true'].join(' ')

    run_tests('test/truffle/compiler', args) do |test_script|
      sh env, test_script
    end
  end

  private def jacoco_args
    out = mx('ruby_jacoco_args', '--quiet', '--no-warning', capture: :out).lines.last.chomp
    out.sub('-', '--vm.')
  end

  private def test_cexts(*args)
    all_tests = %w[
      tools minimum method module globals backtraces xopenssl postinstallhook
      oily_png psd_native
      puma sqlite3 unf_ext json RubyInline msgpack
    ]
    no_openssl = args.delete('--no-openssl')
    no_gems = args.delete('--no-gems')
    tests = args.empty? ? all_tests : all_tests & args
    tests -= %w[xopenssl] if no_openssl
    tests.delete 'gems' if no_gems

    tests.each do |test_name|
      run_single_cexts_test(test_name)
    end
  end

  private def run_single_cexts_test(test_name)
    time_test("jt test cexts #{test_name}") do
      case test_name
      when 'tools'
        # Test tools
        run_ruby 'test/truffle/cexts/test-preprocess.rb'

      when 'minimum', 'method', 'module', 'globals', 'backtraces', 'xopenssl'
        # Test that we can compile and run some very basic C extensions
        begin
          output_file = 'cext-output.txt'
          dir = "#{TRUFFLERUBY_DIR}/test/truffle/cexts/#{test_name}"
          cextc(dir)
          run_ruby "-I#{dir}/lib", "#{dir}/bin/#{test_name}", out: output_file
          actual = File.read(output_file)
          expected_file = "#{dir}/expected.txt"
          expected = File.read(expected_file)
          unless actual == expected
            abort <<~EOS
              C extension #{dir} didn't work as expected

              Actual:
              #{actual}

              Expected:
              #{expected}

              Diff:
              #{diff(expected_file, output_file)}
            EOS
          end
        ensure
          File.delete output_file if File.exist? output_file
        end

      when 'postinstallhook'
        # Test that running the post-install hook works, even when opt &
        # llvm-link are not on PATH, as it is the case on macOS.
        sh({'TRUFFLERUBY_RECOMPILE_OPENSSL' => 'true'}, "#{ruby_home}/lib/truffle/post_install_hook.sh")

      when 'oily_png', 'psd_native'
        gem_home = "#{gem_test_pack}/gems"
        tests = {
          'oily_png' => [['chunky_png-1.3.6', 'oily_png-1.2.0'], ['oily_png']],
          'psd_native' => [['chunky_png-1.3.6', 'oily_png-1.2.0', 'bindata-2.3.1', 'hashie-3.4.4', 'psd-enginedata-1.1.1', 'psd-2.1.2', 'psd_native-1.1.3'], ['oily_png', 'psd_native']],
        }

        gem_name = test_name
        dependencies, libs = tests.fetch(gem_name)

        puts '', gem_name
        gem_root = "#{TRUFFLERUBY_DIR}/test/truffle/cexts/#{gem_name}"
        ext_dir = Dir.glob("#{gem_home}/gems/#{gem_name}*/")[0] + "ext/#{gem_name}"

        compile_cext gem_name, ext_dir, "#{gem_root}/lib/#{gem_name}/#{gem_name}.#{DLEXT}", ['-Werror=implicit-function-declaration']

        next if gem_name == 'psd_native' # psd_native is excluded just for running
        run_ruby(*dependencies.map { |d| "-I#{gem_home}/gems/#{d}/lib" },
                 *libs.map { |l| "-I#{TRUFFLERUBY_DIR}/test/truffle/cexts/#{l}/lib" },
                 "#{TRUFFLERUBY_DIR}/test/truffle/cexts/#{gem_name}/test.rb", gem_root)

      # Tests using gem install to compile the cexts
      when 'puma'
        sh 'test/truffle/cexts/puma/puma.sh'
      when 'sqlite3'
        sh 'test/truffle/cexts/sqlite3/sqlite3.sh'
      when 'unf_ext'
        sh 'test/truffle/cexts/unf_ext/unf_ext.sh'
      when 'json'
        sh 'test/truffle/cexts/json/json.sh'

      when 'RubyInline'
        # Test a gem dynamically compiling a C extension
        # Does not work on macOS. Also fails on macOS on MRI with --enabled-shared.
        # It's a bug of RubyInline not using LIBRUBYARG/LIBRUBYARG_SHARED.
        sh 'test/truffle/cexts/RubyInline/RubyInline.sh' unless darwin?

      # Test cexts used by many projects
      when 'msgpack'
        sh 'test/truffle/cexts/msgpack/msgpack.sh'
      else
        raise "unknown test: #{test_name}"
      end
    end
  end

  private def run_tests(tests_path, tests)
    tests_path = "#{TRUFFLERUBY_DIR}/#{tests_path}"
    test_names = tests.empty? ? '*' : '{' + tests.join(',') + '}'

    candidates = Dir["#{tests_path}/#{test_names}.sh"].sort
    if candidates.empty?
      targets = Dir["#{tests_path}/*.sh"].sort.map { |f| File.basename(f, '.sh') }
      puts "No targets found by pattern #{test_names}. Available targets: "
      targets.each { |t| puts " * #{t}" }
      exit 1
    end

    STDERR.puts
    candidates.each do |test_script|
      time_test(test_script) do
        yield test_script
      end
    end
  end

  private def time_test(test_name)
    STDERR.puts "[jt] Running #{test_name} ..."
    start = Process.clock_gettime(Process::CLOCK_MONOTONIC)
    begin
      yield
    ensure
      finish = Process.clock_gettime(Process::CLOCK_MONOTONIC)
      duration = finish - start
      STDERR.puts "[jt] #{test_name} took #{'%.1f' % duration}s\n\n\n"
    end
  end

  private def test_integration(*args)
    run_tests('test/truffle/integration', args) do |test_script|
      sh test_script
    end
  end

  private def test_gems(*args)
    gem_test_pack

    run_tests('test/truffle/gems', args) do |test_script|
      sh test_script
    end
  end

  private def test_ecosystem(*args)
    gem_test_pack if gem_test_pack?

    run_tests('test/truffle/ecosystem', args) do |test_script|
      sh test_script, *(gem_test_pack if gem_test_pack?)
    end
  end

  def find_ports_for_pid(pid)
    while (ports = `lsof -P -i 4 -a -p #{pid} -Fn | grep nlocalhost | cut -d ':' -f 2`.strip).empty?
      sleep 1
    end
    puts ports
    ports
  end

  private def test_bundle(*args)
    require 'tmpdir'

    bundle_install_flags = [
      %w[],
      %w[--standalone],
      %w[--deployment]
    ]
    gems = %w[algebrick]

    gem_server = spawn('gem', 'server', '-b', '127.0.0.1', '-p', '0', '-d', "#{gem_test_pack}/gems")
    SUBPROCESSES << gem_server
    begin
      ports = find_ports_for_pid(gem_server)
      raise 'More than one port opened' if ports.lines.size > 1
      port = Integer(ports)

      bundle_install_flags.each do |install_flags|
        puts "Testing Bundler with install flags: #{install_flags}"
        gems.each do |gem_name|
          temp_dir = Dir.mktmpdir(gem_name)
          begin
            gem_home = "#{temp_dir}/gems"
            puts "Using temporary GEM_HOME: #{gem_home}"

            puts "Copying gem #{gem_name} source into temp directory: #{temp_dir}"
            original_source_tree = "#{gem_test_pack}/gem-testing/#{gem_name}"
            gem_source_tree = "#{temp_dir}/#{gem_name}"
            FileUtils.copy_entry(original_source_tree, gem_source_tree)

            chdir(gem_source_tree) do
              environment = no_gem_vars_env.merge(
                'GEM_HOME' => gem_home,
                'GEM_PATH' => gem_home,
                # add bin from gem_home to PATH
                'PATH' => ["#{gem_home}/bin", ENV['PATH']].join(File::PATH_SEPARATOR))

              options = %w[--experimental-options --exceptions-print-java]

              run_ruby(environment, *args, *options,
                '-Sbundle', 'config', '--local', 'mirror.http://localhost:8808', "http://localhost:#{port}")

              run_ruby(environment, *args, *options,
                '-Sbundle', 'install', '-V', *install_flags)

              run_ruby(environment, *args, *options,
                '-Sbundle', 'exec', '-V', 'rake')
            end
          ensure
            STDERR.puts 'Removing temp dir'
            FileUtils.remove_entry_secure temp_dir
          end
        end
      end
    ensure
      STDERR.puts 'Terminating gem server'
      terminate_process(gem_server)
      SUBPROCESSES.delete(gem_server)
      STDERR.puts 'gem server terminated'
    end
  end

  def mspec(*args)
    require_ruby_launcher!
    run_mspec({}, *args)
  end

  private def test_specs(command, *args)
    env_vars = {}
    options = []

    case command
    when 'run'
      options += %w[--excl-tag fails]
    when 'tag'
      options += %w[--add fails --fail --excl-tag fails]
    when 'untag'
      options += %w[--del fails --pass]
      command = 'tag'
    when 'purge'
      options += %w[--purge]
      command = 'tag'
    when 'tag_all'
      options += %w[--unguarded --all --dry-run --add fails]
      command = 'tag'
    else
      raise command
    end

    if args.first == 'fast'
      args.shift
      options += %w[--excl-tag slow]
    end

    options += %w[--format specdoc] if ci?

    args, ruby_args = args_split(args)

    vm_args, ruby_args, parsed_options = ruby_options({}, ['--reveal', *ruby_args])
    vm_args << (truffleruby_native? ? '--vm.Xmx3G' : '--vm.Xmx2G')
    vm_args << '--polyglot' if truffleruby_jvm?
    # Until pattern matching is complete, we enable it in specs but not globally
    vm_args << '--experimental-options' << '--pattern-matching'

    raise "unsupported options #{parsed_options}" unless parsed_options.empty?

    prefixed_ruby_args = [*(vm_args if truffleruby?), *ruby_args].map { |v| "-T#{v}" }
    run_mspec env_vars, command, *options, *prefixed_ruby_args, *args
  end

  def gem_test_pack?
    return true if ci?
    Dir.exist?(File.expand_path('truffleruby-gem-test-pack', TRUFFLERUBY_DIR))
  end

  def gem_test_pack
    name = 'truffleruby-gem-test-pack'
    gem_test_pack = File.expand_path(name, TRUFFLERUBY_DIR)

    unless Dir.exist?(gem_test_pack)
      STDERR.puts 'Cloning the truffleruby-gem-test-pack repository'
      git_clone(bitbucket_url(name), gem_test_pack)
    end

    # Unset variable set by the pre-commit hook which confuses git
    env = { 'GIT_DIR' => nil, 'GIT_INDEX_FILE' => nil }

    current = raw_sh(env, 'git', '-C', gem_test_pack, 'rev-parse', 'HEAD', capture: :out, no_print_cmd: true).chomp
    unless current == TRUFFLERUBY_GEM_TEST_PACK_VERSION
      if ENV['GEM_TEST_PACK_WIP'] == 'true'
        STDERR.puts 'WARNING: the gem test pack commit is different than TRUFFLERUBY_GEM_TEST_PACK_VERSION in jt.rb'
      else
        has_commit = raw_sh env, 'git', '-C', gem_test_pack, 'cat-file', '-e', TRUFFLERUBY_GEM_TEST_PACK_VERSION, continue_on_failure: true
        unless has_commit
          raw_sh env, 'git', '-C', gem_test_pack, 'fetch', Remotes.bitbucket(gem_test_pack), continue_on_failure: true
        end
        raw_sh env, 'git', '-C', gem_test_pack, 'checkout', '-q', TRUFFLERUBY_GEM_TEST_PACK_VERSION
      end
    end

    puts gem_test_pack
    gem_test_pack
  end
  alias_method :'gem-test-pack', :gem_test_pack

  def tag(path, *args)
    require_ruby_launcher!
    return tag_all(*args) if path == 'all'
    test_specs('tag', path, *args)
  end

  # Add tags to all given examples without running them. Useful to avoid file exclusions.
  private def tag_all(*args)
    test_specs('tag_all', *args)
  end

  def purge(path, *args)
    require_ruby_launcher!
    test_specs('purge', path, *args)
  end

  def untag(path, *args)
    require_ruby_launcher!
    puts
    puts "WARNING: untag is currently not very reliable - run `jt test #{[path,*args] * ' '}` after and manually annotate any new failures"
    puts
    test_specs('untag', path, *args)
  end

  def build_stats(attribute, *args)
    require_ruby_launcher!

    use_json = args.delete '--json'

    value = case attribute
            when 'binary-size'
              build_stats_native_binary_size(*args)
            when 'build-time'
              build_stats_native_build_time(*args)
            when 'runtime-compilable-methods'
              build_stats_native_runtime_compilable_methods(*args)
            else
              raise ArgumentError, attribute
            end

    if use_json
      puts JSON.generate({ attribute => value })
    else
      puts "#{attribute}: #{value}"
    end
  end

  private def build_stats_native_binary_size(*args)
    truffleruby_native!
    File.size(ruby_launcher) / 1024.0 / 1024.0
  end

  private def build_stats_native_build_time(*args)
    log = File.read('aot-build.log')
    build_time = log[/\[truffleruby.*\].*\[total\]:\s*([0-9,.]+)\s*ms/, 1]
    Float(build_time.gsub(',', '')) / 1000.0
  end

  private def build_stats_native_runtime_compilable_methods(*args)
    log = File.read('aot-build.log')
    log =~ /(?<method_count>\d+) method\(s\) included for runtime compilation/m
    Integer($~[:method_count])
  end

  def metrics(command, *args)
    require_ruby_launcher!
    args = args.dup
    case command
    when 'alloc'
      metrics_alloc(*args)
    when 'minheap'
      metrics_minheap(*args)
    when 'maxrss'
      metrics_maxrss(*args)
    when 'instructions'
      metrics_native_instructions(*args)
    when 'time'
      metrics_time(*args)
    else
      raise ArgumentError, command
    end
  end

  private def metrics_alloc(*args)
    use_json = args.delete '--json'
    samples = []
    METRICS_REPS.times do
      log '.', "sampling\n"
      out = run_ruby '--vm.Dtruffleruby.metrics.memory_used_on_exit=true', '--vm.verbose:gc', *args, capture: :both, no_print_cmd: true
      samples.push memory_allocated(out)
    end
    log "\n", nil
    range = samples.max - samples.min
    error = range / 2
    median = samples.min + error
    human_readable = "#{human_size(median)} ± #{human_size(error)}"
    if use_json
      puts JSON.generate({
          samples: samples,
          median: median,
          error: error,
          human: human_readable
      })
    else
      puts human_readable
    end
  end

  private def memory_allocated(trace)
    allocated = 0
    trace.lines do |line|
      case line
      when /(\d+)K->(\d+)K/
        before = $1.to_i * 1024
        after = $2.to_i * 1024
        collected = before - after
        allocated += collected
      when /^allocated (\d+)$/
        allocated += $1.to_i
      end
    end
    allocated
  end

  private def metrics_minheap(*args)
    use_json = args.delete '--json'
    heap = 10
    log '>', "Trying #{heap} MB\n"
    until can_run_in_heap(heap, *args)
      heap += 10
      log '>', "Trying #{heap} MB\n"
    end
    heap -= 9
    heap = 1 if heap == 0
    successful = 0
    loop do
      if successful > 0
        log '?', "Verifying #{heap} MB\n"
      else
        log '+', "Trying #{heap} MB\n"
      end
      if can_run_in_heap(heap, *args)
        successful += 1
        break if successful == METRICS_REPS
      else
        heap += 1
        successful = 0
      end
    end
    log "\n", nil
    human_readable = "#{heap} MB"
    if use_json
      puts JSON.generate({
          min: heap,
          human: human_readable
      })
    else
      puts human_readable
    end
  end

  private def can_run_in_heap(heap, *command)
    run_ruby("--vm.Xmx#{heap}M", *command, err: '/dev/null', out: '/dev/null', no_print_cmd: true, continue_on_failure: true, timeout: 60)
  end

  private def metrics_maxrss(*args)
    truffleruby_native!

    use_json = args.delete '--json'
    samples = []

    METRICS_REPS.times do
      log '.', "sampling\n"

      max_rss_in_mb = if linux?
                        out = raw_sh('/usr/bin/time', '-v', '--', ruby_launcher, *args, capture: :both, no_print_cmd: true)
                        out =~ /Maximum resident set size \(kbytes\): (?<max_rss_in_kb>\d+)/m
                        Integer($~[:max_rss_in_kb]) / 1024.0
                      elsif darwin?
                        out = raw_sh('/usr/bin/time', '-l', '--', ruby_launcher, *args, capture: :both, no_print_cmd: true)
                        out =~ /(?<max_rss_in_bytes>\d+)\s+maximum resident set size/m
                        Integer($~[:max_rss_in_bytes]) / 1024.0 / 1024.0
                      else
                        raise "Can't measure RSS on this platform."
                      end

      samples.push(maxrss: max_rss_in_mb)
    end
    log "\n", nil

    results = {}
    samples[0].each_key do |region|
      region_samples = samples.map { |s| s[region] }
      mean = region_samples.inject(:+) / samples.size
      human = "#{region} #{mean.round(2)} MB"
      results[region] = {
          samples: region_samples,
          mean: mean,
          human: human
      }
      if use_json
        file = STDERR
      else
        file = STDOUT
      end
      file.puts region[/\s*/] + human
    end
    if use_json
      puts JSON.generate(Hash[results.map { |key, values| [key, values] }])
    end
  end

  private def metrics_native_instructions(*args)
    truffleruby_native!

    use_json = args.delete '--json'

    out = raw_sh('perf', 'stat', '-e', 'instructions', '--', ruby_launcher, *args, capture: :both, no_print_cmd: true)

    out =~ /(?<instruction_count>[\d,]+)\s+instructions/m
    instruction_count = $~[:instruction_count].gsub(',', '')

    log "\n", nil
    human_readable = "#{instruction_count} instructions"
    if use_json
      puts JSON.generate({
          instructions: Integer(instruction_count),
          human: human_readable
      })
    else
      puts human_readable
    end
  end

  private def metrics_time_measure(use_json, *args)
    truffleruby!
    metrics_time_option = '--vm.Dtruffleruby.metrics.time=true'
    verbose_gc_flag = truffleruby_native? ? '--vm.XX:+PrintGC' : '--vm.verbose:gc' unless use_json
    args = [metrics_time_option, *verbose_gc_flag, '--no-core-load-path', *args]
    # JVM verbose:gc outputs on stdout, metrics outputs on stderr
    capture = truffleruby_native? ? :err : :both

    samples = METRICS_REPS.times.map do
      log '.', "sampling\n"
      start = Time.now
      out = run_ruby(*args, no_print_cmd: true, capture: capture)
      finish = Time.now
      get_times(out, (finish - start) * 1000.0)
    end
    log "\n", nil
    samples
  end

  private def metrics_time(*args)
    use_json = args.delete '--json'
    flamegraph = args.delete '--flamegraph'

    samples = metrics_time_measure(use_json, *args)
    metrics_time_format_results(samples, use_json, flamegraph)
  end

  def format_time_metrics(*args)
    use_json = args.delete '--json'
    flamegraph = args.delete '--flamegraph'

    data = STDIN.read
    times = data.lines.grep(/^(before|after)\b/)
    total = times.last.split.last.to_f - times.first.split.last.to_f
    samples = [get_times(data, total)]

    metrics_time_format_results(samples, use_json, flamegraph)
  end

  private def metrics_time_format_results(samples, use_json, flamegraph)
    min_time = Float(ENV.fetch('TRUFFLERUBY_METRICS_MIN_TIME', '-1'))

    results = {}
    mean_by_stack = {}
    samples[0].each_key do |stack|
      region_samples = samples.map { |s| s[stack] }
      mean = region_samples.inject(:+) / samples.size
      mean_by_stack[stack] = mean
      mean_in_seconds = (mean / 1000.0)

      region = stack.last
      human = "#{'%.3f' % mean_in_seconds} #{region}"
      results[region] = {
        samples: region_samples,
        mean: mean_in_seconds,
        human: human
      }

      indent = ' ' * (stack.size-1)
      if use_json
        STDERR.puts indent + human
      else
        STDOUT.puts indent + human if mean_in_seconds > min_time
      end
    end
    if use_json
      puts JSON.generate(results)
    elsif flamegraph
      repo = find_or_clone_repo('https://github.com/eregon/FlameGraph.git', 'graalvm')
      path = "#{TRUFFLERUBY_DIR}/time_metrics.stacks"
      File.open(path, 'w') do |file|
        mean_by_stack.each_pair do |stack, mean|
          on_top_of_stack = mean
          mean_by_stack.each_pair do |sub_stack, time|
            on_top_of_stack -= time if sub_stack[0...-1] == stack
          end

          file.puts "#{stack.join(';')} #{on_top_of_stack.round}"
        end
      end
      sh "#{repo}/flamegraph.pl", '--flamechart', '--countname', 'ms', path, out: 'time_metrics_flamegraph.svg'
    end
  end

  private def get_times(trace, total)
    result = Hash.new(0)
    stack = [['total', 0]]

    result[stack.map(&:first)] = total
    result[%w[total jvm]] = 0

    trace.each_line do |line|
      if line =~ /^(.+) (\d+)$/
        region = $1
        time = Float($2)
        if region.start_with? 'before-'
          name = region['before-'.size..-1]
          stack << [name, time]
          result[stack.map(&:first)] += 0
        elsif region.start_with? 'after-'
          name = region['after-'.size..-1]
          prev, start = stack.last
          raise "#{region} after before-#{prev}" unless name == prev
          result[stack.map(&:first)] += (time - start)
          stack.pop
        else
          STDERR.puts line
        end
      # [Full GC (Metadata GC Threshold)  23928K->23265K(216064K), 0.1168747 secs]
      elsif line =~ /^\[(.+), (\d+\.\d+) secs\]$/
        name = $1
        time = Float($2)
        stack << [name, time]
        result[stack.map(&:first)] += (time * 1000.0)
        stack.pop
      else
        STDERR.puts line
      end
    end

    result[%w[total jvm]] = total - result[%w[total main]]
    result
  end

  def benchmark(*args)
    vm_args = []
    if truffleruby?
      if truffleruby_compiler?
        vm_args << '--check-compilation'
      else
        STDERR.puts 'WARNING: benchmarking without the GraalVM compiler'
      end
    end

    args, ruby_args = args_split(args)

    if start_time_index = args.index('START_TIME_SET_BY_JT_BENCHMARK')
      args[start_time_index] = Process.clock_gettime(Process::CLOCK_MONOTONIC).to_s
    end

    run_ruby(*vm_args, *ruby_args, "#{TRUFFLERUBY_DIR}/bench/benchmark", *args, use_exec: true)
  end

  def profile(*args)
    require_ruby_launcher!
    env = args.first.is_a?(Hash) ? args.shift : {}
    stdin = args.delete '-'

    repo = find_or_clone_repo('https://github.com/eregon/FlameGraph.git', 'graalvm')
    Dir.mkdir(PROFILES_DIR) unless Dir.exist?(PROFILES_DIR)

    time = Time.now.strftime('%Y%m%d-%H%M%S')
    profile_data_file = "#{PROFILES_DIR}/truffleruby-profile-#{time}.json"
    flamegraph_data_file = "#{PROFILES_DIR}/truffleruby-flamegraph-data.stacks"
    svg_filename = "#{PROFILES_DIR}/flamegraph_#{time}.svg"

    if stdin
      File.write(profile_data_file, STDIN.read)
    else
      require 'benchmark'
      FileUtils.rm_f(profile_data_file)
      run_args = DEFAULT_PROFILE_OPTIONS + ["--cpusampler.OutputFile=#{profile_data_file}"] + args
      puts Benchmark.measure {
        run_ruby(env, *run_args)
      }
    end
    raw_sh "#{repo}/stackcollapse-graalvm.rb", profile_data_file, out: flamegraph_data_file
    unit = args.any?(/^--cpusampler\.Period=/) ? [] : ['--countname', 'ms']
    raw_sh "#{repo}/flamegraph.pl", *unit, flamegraph_data_file, out: svg_filename

    app_open svg_filename
  end

  def igv
    clone_enterprise
    graal_enterprise = File.expand_path '../graal-enterprise/graal-enterprise', TRUFFLERUBY_DIR
    mx('igv', chdir: graal_enterprise)
  end

  def install(name, *options)
    case name
    when 'jvmci'
      puts install_jvmci("Downloading JDK#{@jdk_version} with JVMCI", (@ruby_name || '').include?('ee'))
    when 'eclipse'
      puts install_eclipse
    else
      raise "Unknown how to install #{what}"
    end
  end

  private def install_jvmci(download_message, ee, jdk_version: @jdk_version)
    if jdk_version == 8
      jdk_name = ee ? 'oraclejdk8' : 'openjdk8'
    elsif jdk_version == 11
      jdk_name = ee ? 'labsjdk-ee-11' : 'labsjdk-ce-11'
    elsif jdk_version == 16
      jdk_name = ee ? 'labsjdk-ee-16' : 'labsjdk-ce-16'
    else
      raise "Unknown JDK version: #{jdk_version}"
    end

    java_home = "#{CACHE_EXTRA_DIR}/#{jdk_name}-#{jvmci_version}"
    unless File.directory?(java_home)
      STDERR.puts "#{download_message} (#{jdk_name})"
      if ee
        clone_enterprise
        jdk_binaries = File.expand_path '../graal-enterprise/jdk-binaries.json', TRUFFLERUBY_DIR
      end
      mx '-y', 'fetch-jdk',
         '--configuration', "#{TRUFFLERUBY_DIR}/common.json",
         *(['--jdk-binaries', jdk_binaries] if jdk_binaries),
         '--java-distribution', jdk_name,
         '--to', CACHE_EXTRA_DIR,
         '--alias', java_home, # ensure the JDK ends up in the path we expect
         java_home: :none # avoid recursion
    end

    java_home = "#{java_home}/Contents/Home" if darwin?
    java = "#{java_home}/bin/java"
    abort "#{java} does not exist" unless File.executable?(java)

    java_home
  end

  private def install_eclipse
    require 'digest'
    if linux?
      eclipse_url = 'https://github.com/chrisseaton/eclipse-mirror/releases/download/eclipse-SDK-4.5.2/eclipse-SDK-4.5.2-linux-gtk-x86_64.tar.gz'
      eclipse_exe = 'eclipse/eclipse'
      sha256 = '87f82b0c13c245ee20928557dbc4435657d1e029f72d9135683c8d585c69ba8d'
    elsif darwin?
      eclipse_url = 'https://github.com/chrisseaton/eclipse-mirror/releases/download/eclipse-SDK-4.5.2/eclipse-SDK-4.5.2-macosx-cocoa-x86_64.tar.gz'
      eclipse_exe = 'Eclipse.app/Contents/MacOS/eclipse'
      sha256 = '755f8a75075f6310a8d0453b5766a84aca2fcc687808341b7a657259230b490f'
    else
      raise 'Installing Eclipse is only available on Linux and macOS currently'
    end

    eclipse_tar = eclipse_url.split('/').last
    eclipse_name = File.basename(eclipse_tar, '.tar.gz')
    eclipse_path = "#{CACHE_EXTRA_DIR}/#{eclipse_name}/#{eclipse_exe}"
    return eclipse_path if File.exist?(eclipse_path)

    chdir(CACHE_EXTRA_DIR) do
      unless File.exist?(eclipse_tar)
        raw_sh 'curl', '-L', eclipse_url, '-o', eclipse_tar
      end
      unless File.exist?(eclipse_name)
        computed = Digest::SHA256.file(eclipse_tar).hexdigest
        if computed == sha256
          Dir.mkdir eclipse_name
          raw_sh 'tar', 'xf', eclipse_tar, '-C', eclipse_name
        else
          raise "Incorrect sha256 for #{eclipse_tar}: #{computed} instead of expected #{sha256}"
        end
      end
    end

    eclipse_path
  end

  def clone_enterprise
    ee_path = File.expand_path '../graal-enterprise', TRUFFLERUBY_DIR
    if File.directory?(ee_path)
      false
    else
      git_clone(bitbucket_url('graal-enterprise'), ee_path)
      true
    end
  end

  def checkout_enterprise_revision(env = 'jvm-ee')
    mx('-p', TRUFFLERUBY_DIR, '--env', env, 'checkout-downstream', 'compiler', 'graal-enterprise')
  end

  def bootstrap_toolchain
    sulong_home = File.join(GRAAL_DIR, 'sulong')
    # clone the graal repository if it is missing
    mx 'sversions' unless File.directory? sulong_home
    graal_version = get_truffle_version from: :repository
    toolchain_dir = File.join(TRUFFLERUBY_DIR, 'mxbuild', 'toolchain')
    destination = File.join(toolchain_dir, graal_version)
    unless File.exist? destination
      puts "Building toolchain for: #{graal_version}"
      mx '-p', sulong_home, '--env', 'toolchain-only', 'build'
      toolchain_graalvm = mx('-p', sulong_home, '--env', 'toolchain-only', 'graalvm-home', capture: :out).lines.last.chomp
      FileUtils.mkdir_p destination
      FileUtils.cp_r toolchain_graalvm + '/.', destination
    end

    # mark as used
    FileUtils.touch destination
    # leave only 4 built toolchains which were used last
    caches = Dir.
        glob(File.join(toolchain_dir, '*')).
        sort_by { |cached_toolchain_path| File.mtime cached_toolchain_path }
    unless (oldest = caches[0...-4]).empty?
      puts "Removing old cached toolchains: #{oldest.join ' '}"
      FileUtils.rm_rf oldest
    end

    destination
  end

  private def sforceimports?(mx_base_args)
    scheckimports_output = mx(*mx_base_args, 'scheckimports', '--ignore-uncommitted', '--warn-only', capture: :both)

    unless scheckimports_output.empty?
      # Don't ask to update, just warn.
      if ENV['JT_IMPORTS_DONT_ASK'] || !STDIN.tty?
        with_color(TERM_COLOR_RED) do
          boxed do
            puts scheckimports_output
            puts <<~MESSAGE
              You might want to:
              * use the version of graal in suite.py, then use "jt build --sforceimports", useful when building TruffleRuby and not changing graal at the same time (most common)
              * update the graal version in suite.py, then use "mx scheckimports", useful when explicitly updating the default graal version of TruffleRuby (rare)
              * test TruffleRuby with a different graal version with your own changes in graal, then you can ignore this warning
            MESSAGE
          end
        end
        false
      else
        # Ask to update imports.
        with_color(TERM_COLOR_RED) do
          puts "\nNOTE: Set env variable JT_IMPORTS_DONT_ASK to always answer 'no' to this prompt.\n\n"
          puts scheckimports_output
          input = ''
          until %w(y n).include? input
            print 'Do you want to checkout the supported version of graal as specified in truffleruby\'s suite.py? (runs `mx sforceimports`) [y/n] '
            input = STDIN.gets.chomp
          end
          puts ''
          input == 'y'
        end
      end
    end
  end

  private def build_graalvm(*options)
    raise 'use --env jvm-ce instead' if options.delete('--graal')
    raise 'use --env native instead' if options.delete('--native')

    if os_version_changed?
      warn "Kernel version changed since last build: #{build_kernel_ver.inspect} -> #{host_kernel_ver.inspect}"
      remove_shared_compile_artifacts
    end

    env = if (i = options.index('--env') || options.index('-e'))
            options.delete_at i
            options.delete_at i
          else
            ENV['JT_ENV'] || 'jvm'
          end
    @mx_env = env
    raise 'Cannot use both --use and --env' if defined?(@ruby_name)

    @ruby_name = if (i = options.index('--name') || options.index('-n'))
                   options.delete_at i
                   options.delete_at i
                 else
                   env
                 end

    name = "truffleruby-#{@ruby_name}"
    mx_base_args = ['-p', TRUFFLERUBY_DIR, '--env', env]

    # Must clone enterprise before running `mx scheckimports` in `sforceimports?`
    ee = env.include?('ee')
    cloned = clone_enterprise if ee
    checkout_enterprise_revision(env) if cloned

    if options.delete('--sforceimports') || sforceimports?(mx_base_args)
      mx('-p', TRUFFLERUBY_DIR, 'sforceimports')
      checkout_enterprise_revision(env) if ee && !cloned
    end

    mx_options, mx_build_options = args_split(options)
    mx_args = mx_base_args + mx_options

    process_env = ENV['JT_CACHE_TOOLCHAIN'] ? { 'SULONG_BOOTSTRAP_GRAALVM' => bootstrap_toolchain } : {}

    mx(process_env, *mx_args, 'build', *mx_build_options)
    build_dir = mx(*mx_args, 'graalvm-home', capture: :out).lines.last.chomp

    dest = "#{TRUFFLERUBY_DIR}/mxbuild/#{name}"
    dest_ruby = "#{dest}/#{language_dir(build_dir)}/ruby"
    dest_bin = "#{dest_ruby}/bin"
    FileUtils.rm_rf dest
    if @ruby_name != @mx_env
      # if `--name NAME` is passed, we want to copy so we don't end up with two symlinks
      # to the same directory for the same --env but different names
      FileUtils.cp_r(build_dir, dest)
    else
      File.symlink(build_dir, dest)
    end

    # Insert native wrapper around the bash launcher
    # since nested shebang does not work on macOS when fish shell is used.
    if darwin? && File.binread(truffleruby_launcher_path)[2] == '#!'
      FileUtils.mv "#{dest_bin}/truffleruby", "#{dest_bin}/truffleruby.sh"
      FileUtils.cp "#{TRUFFLERUBY_DIR}/tool/native_launcher_darwin", "#{dest_bin}/truffleruby"
    end

    # Symlink builds into version manager
    rbenv_root = ENV['RBENV_ROOT']
    rubies_dir = File.join(rbenv_root, 'versions') if rbenv_root && File.directory?(rbenv_root)

    chruby_versions = File.expand_path('~/.rubies')
    rubies_dir = chruby_versions if File.directory?(chruby_versions)

    if rubies_dir
      Dir.glob(rubies_dir + '/truffleruby-*').each do |link|
        next unless File.symlink?(link)
        next if File.exist?(link)
        target = File.readlink(link)
        next unless target.start_with?("#{TRUFFLERUBY_DIR}/mxbuild")
        File.delete link
        puts "Deleted broken link: #{link} -> #{target}"
      end

      link_path = "#{rubies_dir}/#{name}"
      File.delete link_path if File.symlink? link_path or File.exist? link_path
      File.symlink dest_ruby, link_path
    end
  end

  def remove_shared_compile_artifacts
    if build_information_path.file?
      warn "Deleting shared build artifacts to trigger rebuild: #{shared_path}"
      shared_path.rmtree
    end
  end

  def os_version_changed?
    build_kernel_ver != host_kernel_ver
  end

  def host_kernel_ver
    `uname -r`[/^\d+/]
  end

  def build_kernel_ver
    return '' unless build_information_path.file?

    build_information = build_information_path.readlines
    build_os_ver_loc  = build_information.index { |l| l.include?('getKernelMajorVersion') }
    return '' unless build_os_ver_loc

    build_information[build_os_ver_loc + 1][/"(\d+)/, 1]
  end

  def shared_path
    Pathname.new("#{TRUFFLERUBY_DIR}/mxbuild/org.truffleruby.shared")
  end

  def build_information_path
    shared_path
      .join('src_gen/org/truffleruby/shared/BuildInformationImpl.java')
  end

  def next(*args)
    puts `cat spec/tags/core/**/**.txt | grep 'fails:'`.lines.sample
  end

  def native_launcher
    sh 'cc', '-o', 'tool/native_launcher_darwin', 'tool/native_launcher_darwin.c'
  end
  alias :'native-launcher' :native_launcher

  def rubocop(*args)
    if args.empty? or args.all? { |arg| arg.start_with?('-') }
      args += RUBOCOP_INCLUDE_LIST
    end

    if gem_test_pack?
      gem_home = "#{gem_test_pack}/rubocop-gems"
      env = { 'GEM_HOME' => gem_home, 'GEM_PATH' => gem_home }
      sh env, 'ruby', "#{gem_home}/bin/rubocop", *args
    else
      unless sh('rubocop', "_#{RUBOCOP_VERSION}_", *args, continue_on_failure: true)
        sh 'gem', 'install', 'rubocop', '-v', RUBOCOP_VERSION
        sh 'rubocop', "_#{RUBOCOP_VERSION}_", *args
      end
    end
  end

  def idea(*args)
    ENV['ECLIPSE_EXE'] ||= install_eclipse
    mx(*args, 'intellijinit')
  end

  def command_format(changed_java_files = nil)
    STDERR.puts bold '$ jt format'
    ENV['ECLIPSE_EXE'] ||= install_eclipse
    if changed_java_files.is_a?(Array)
      File.write('mxbuild/javafilelist.txt', changed_java_files.join("\n"))
      filelist_args = %w[--filelist mxbuild/javafilelist.txt]
    end
    env = { '_JAVA_OPTIONS' => '-Djava.net.preferIPv4Stack=true' }
    mx env, 'eclipseformat', '--no-backup', '--primary', *filelist_args
    format_specializations_check
  end

  private def check_filename_length
    # For eCryptfs, see https://bugs.launchpad.net/ecryptfs/+bug/344878
    max_length = 143

    too_long = []
    Dir.chdir(TRUFFLERUBY_DIR) do
      Dir.glob('**/*') do |f|
        if !f.start_with?('graal_dumps/')
          if File.basename(f).size > max_length
            too_long << f
          end
        end
      end
    end

    unless too_long.empty?
      abort "Too long filenames for eCryptfs:\n#{too_long.join "\n"}"
    end
  end

  private def check_core_symbols
    build('core-symbols')
    diff = sh 'git', 'diff', 'src/main/java/org/truffleruby/core/symbol/CoreSymbols.java', capture: :out
    unless diff.empty?
      STDERR.puts 'DIFF:'
      STDERR.puts diff
      abort "CoreSymbols.java must be regenerated by 'jt build core-symbols'"
    end
  end

  def check_heap_dump(*args)
    common_args = ['--lang', 'ruby',
                   '--forbidden-class', 'org.truffleruby.language.RubyDynamicObject',
                   '--forbidden-class', 'org.truffleruby.RubyContext']
    args.unshift(*common_args)
    mx 'ruby_check_heap_dump', '--shared-engine', *args, '--code', "p 42; p require 'set'" # "; p require 'openssl'"
  end

  private def check_parser
    build('parser')
    diff = sh 'git', 'diff', 'src/main/java/org/truffleruby/parser/parser/RubyParser.java', capture: :out
    unless diff.empty?
      STDERR.puts 'DIFF:'
      STDERR.puts diff
      abort "RubyParser.java must be regenerated by 'jt build parser'"
    end
  end

  private def check_options
    build('options')
    ['Options.java', 'LanguageOptions.java'].each do |file|
      diff = sh 'git', 'diff', "src/main/java/org/truffleruby/options/#{file}", capture: :out
      unless diff.empty?
        STDERR.puts 'DIFF:'
        STDERR.puts diff
        abort "#{file} must be regenerated by 'jt build options'"
      end
    end
  end

  private def check_source_files
    diff = sh 'git', 'diff', capture: :out
    unless diff.empty?
      STDERR.puts 'DIFF:'
      STDERR.puts diff
      abort 'Source files should not be modified in CI'
    end
  end

  def check_native_configuration
    files = Dir.glob('src/main/java/org/truffleruby/platform/*NativeConfiguration.java')
    diff = sh 'git', 'diff', *files, capture: :out
    unless diff.empty?
      STDERR.puts 'DIFF:'
      STDERR.puts diff
      abort "*NativeConfiguration.java must be regenerated by 'ruby tool/generate-native-config.rb'"
    end
  end

  def check_documentation
    status = true
    allowed = -> str {
      str.ascii_only? or str.chars.all? { |c| c.ascii_only? or %w[± – → ⌥ ⌘ ⇧].include?(c) }
    }

    `git -C #{TRUFFLERUBY_DIR} ls-files '**/*.md'`.lines.map(&:chomp).each do |file|
      next if file.start_with?('lib/gems/')
      contents = File.read(file, encoding: Encoding::UTF_8)
      unless allowed[contents]
        non_ascii = contents.lines.reject(&allowed).map do |line|
          line.chars.map { |c| allowed[c] ? c : "[#{c}]" }.join
        end.join
        status = false
        puts "Non-US-ASCII character in #{file}:\n#{non_ascii}"
      end
    end
    abort unless status
  end

  def check_documentation_urls
    url_base = 'https://github.com/oracle/truffleruby/blob/master/doc/'
    # Explicit list of URLs, so they can be added manually
    # Notably, Ruby installers reference the LLVM urls
    known_hardcoded_urls = %w[
      https://github.com/oracle/truffleruby/blob/master/doc/user/installing-libssl.md
      https://github.com/oracle/truffleruby/blob/master/doc/user/installing-llvm.md
      https://github.com/oracle/truffleruby/blob/master/doc/user/installing-zlib.md
      https://github.com/oracle/truffleruby/blob/master/doc/user/ruby-managers.md
    ]

    known_hardcoded_urls.each do |url|
      file = url[url_base.size..-1]
      path = "#{TRUFFLERUBY_DIR}/doc/#{file}"
      unless File.file?(path)
        abort "#{path} could not be found but is referenced in code"
      end
    end

    hardcoded_urls = `git -C #{TRUFFLERUBY_DIR} grep -Fn #{url_base.inspect}`
    status = true
    hardcoded_urls.each_line do |line|
      abort "Could not parse #{line.inspect}" unless /(.+?):(\d+):.+?(https:.+?)[ "'\n]/ =~ line
      file, line, url = $1, $2, $3
      if !%w[tool/jt.rb tool/generate-user-doc.rb].include?(file) and !known_hardcoded_urls.include?(url)
        puts "Found unknown hardcoded url #{url} in #{file}:#{line}, add it in tool/jt.rb"
        status = false
      end
    end
    abort unless status
  end

  def shellcheck
    files = sh 'git', 'ls-files', capture: :out, no_print_cmd: true
    sh_files = files.lines.map(&:chomp).select { |f| f.end_with?('.sh') }
    puts bold '$ shellcheck -a -x ...'
    sh 'shellcheck', '-a', '-x', *sh_files, no_print_cmd: true
  end

  def check_license
    v = '1.0' # to avoid self-matching
    ["Eclipse Public License version #{v}", "Eclipse Public License #{v}", "EPL #{v}", "EPL#{v}", "EPL-#{v}"].each do |match|
      output = `git -C #{TRUFFLERUBY_DIR} grep '#{match}'`
      output = output.lines.reject { |line| line.start_with?('lib/mri/rubygems/util/licenses.rb:') }.join
      unless output.empty?
        abort "There should be no mention of #{match} in the repository:\n#{output}"
      end
    end
  end

  def checkstyle(changed_java_files = nil)
    if changed_java_files.is_a?(Array)
      File.write('mxbuild/javafilelist.txt', changed_java_files.join("\n"))
      filelist_args = %w[--filelist mxbuild/javafilelist.txt]
    end
    output = mx 'checkstyle', '--primary', *filelist_args, capture: :both, continue_on_failure: true
    status = $?

    unused_import = /: Unused import -/
    if !status.success? and output =~ unused_import
      puts 'Automatically removing unused imports'
      output.lines.reverse.grep(unused_import) do |line|
        path, lineno, _ = line.split(':', 3)
        lineno = Integer(lineno)

        puts "Removing unused import in #{path}:#{lineno}"
        lines = File.readlines path
        lines.delete_at(lineno-1)
        File.write path, lines.join
      end

      # Still error out as the removed imports should be committed, but mark them as fixed
      output = output.lines.map { |line| line =~ unused_import ? "[FIXED] #{line}" : line }.join
      puts
    end

    STDERR.puts output
    unless status.success?
      exit status.exitstatus
    end
  end

  module Formatting
    extend self

    def format_specializations_visibility
      iterate do |type, (first, *rest)|
        if type == :ExportMessage and first !~ /\bstatic\b/
          # Keep non-static @ExportMessage public
          [first, *rest]
        else
          # Change to protected
          [first.gsub(/^( *)(public |protected |private |)/, '\1protected '), *rest]
        end
      end
    end

    def format_specializations_arguments
      iterate do |_type, lines|
        first = lines.first
        indent = "#{first[/^ +/]}"
        arg_indent = indent + ' ' * 8
        declaration, *arguments, rest = split_arguments(lines.join)

        dynamic_arguments, cached_arguments = arguments.partition { |segment| segment !~ /^ *@/ || segment =~ /^ *@SuppressWarnings/ }

        if cached_arguments.empty?
          tail = rest
        else
          tail = dynamic_arguments.empty? ? '' : ','
        end
        one_line = indent + declaration + dynamic_arguments.join(', ') + tail
        one_line_below = arg_indent + dynamic_arguments.join(', ') + tail

        if one_line.size <= 120
          [one_line + "\n",
           *cached_arguments[0..-2].map { |c| arg_indent + c + ",\n" },
           *(arg_indent + cached_arguments[-1] + rest + "\n" unless cached_arguments.empty?)]
        elsif one_line_below.size <= 120
          ["#{indent}#{declaration}\n",
           one_line_below + "\n",
           *cached_arguments[0..-2].map { |c| arg_indent + c + ",\n" },
           *(arg_indent + cached_arguments[-1] + rest + "\n" unless cached_arguments.empty?)]
        else
          [indent + declaration + "\n",
           *arguments[0..-2].map { |c| arg_indent + c + ",\n" },
           arg_indent + arguments[-1] + rest + "\n"]
        end
      end
    end

    def format_imports
      each_file do |content|
        content
          .sub(/\n{3,}import /, "\n\nimport ")
          .sub(/^(import .+;)\n{3,}((?:(?:@|\/\/|\/\*).+\n)*)public/, "\\1\n\n\\2public")
      end
    end

    private

    def split_arguments(line)
      i = 0
      split_points = []
      i += 1 while line[i] != '('
      i += 1
      parens = 1
      generic = 0
      split_points << i # ( start of arguments


      while i < line.size
        case line[i]
        when '('
          parens += 1
        when ')'
          parens -= 1
          if parens == 0
            split_points << i
            break
          end
        when '<'
          generic += 1
        when '>'
          generic -= 1
        when ','
          split_points << i if parens == 1 && generic == 0
        else
          # nothing to do
        end
        i += 1
      end

      split_points = [0, *split_points, -1]
      segments = split_points.each_with_index.reduce([]) do |arr, (split_point, j)|
        if j + 1 == split_points.size
          arr
        else
          arr << line[split_point...split_points[j + 1]]
        end
      end
      segments.map { |segment| segment.gsub(/\A,?\s+|\s+,?\Z/, '') }
    end

    def each_file
      changed = false
      Dir.glob(File.join(TRUFFLERUBY_DIR, 'src', '**', '*.java')) do |file|
        content = File.read file
        new_content = yield content

        if content != new_content
          puts "#{file} updated"
          changed = true
          File.write file, new_content
        end
      end
      changed
    end

    def iterate(&update)
      each_file do |content|
        new_content = ''
        lines = content.lines.to_a

        while (line = lines.shift)
          if /^ *@(Specialization|Fallback|CreateCast|ExportMessage)/ =~ line
            type = $1.to_sym
          else
            new_content << line
            next
          end

          braces = count_braces(line)
          # look for end of annotation
          while braces != 0
            new_content << line
            line = lines.shift
            braces += count_braces(line)
          end

          # Never look for a declaration on the same line as the end of the annotation,
          # as the regex below can cause false positives.
          new_content << line
          line = lines.shift

          # look for declaration
          while line !~ /[\w<>]+(\[\])? \w+\(/
            new_content << line
            line = lines.shift
          end

          # look for whole declaration
          braces = count_braces(line)
          declaration = [line]
          while braces != 0
            # new_content << line
            line = lines.shift
            braces += count_braces(line)
            declaration << line
          end

          declaration = update.call type, declaration
          declaration.each { |l| new_content << l }
        end

        new_content
      end
    end

    def count_braces(line, brackets = '()')
      line.count(brackets[0]) - line.count(brackets[1])
    end
  end

  def format_specializations_visibility
    Formatting.format_specializations_visibility
  end

  def format_specializations_arguments
    Formatting.format_specializations_arguments
  end

  def format_specializations_check
    abort 'Some Specializations were not protected.' if format_specializations_visibility
    abort 'Some Specializations were not properly formatted.' if format_specializations_arguments
    abort 'There were extra blank lines around imports.' if Formatting.format_imports
  end

  def lint(*args)
    fast = args.first == 'fast'
    args.shift if fast

    if fast and compare_to = args.shift
      changed_files = changed_files(compare_to)
      changed = {}
      changed_files.each do |file|
        changed.fetch(File.extname(file)) { |k| changed[k] = [] } << file
      end
    else
      changed = -> _ext { true }
    end

    ENV['ECLIPSE_EXE'] ||= install_eclipse

    check_filename_length unless fast

    # Lint
    rubocop if changed['.rb']
    sh 'tool/lint.sh' if changed['.c']
    checkstyle(changed['.java']) if changed['.java']
    command_format(changed['.java']) if changed['.java']
    shellcheck if changed['.sh'] or changed['.inc']

    mx 'verify-ci' if changed['.py']

    unless fast
      mx 'gate', '--tags', 'style' # mx eclipseformat, mx checkstyle and a few more checks

      check_core_symbols
      check_parser
      check_options
      check_documentation
      check_documentation_urls
      check_license
      check_abi

      check_source_files if ci?
      check_heap_dump if ci?
    end
  end

  ABI_VERSION_FILE = 'lib/cext/ABI_version.txt'
  ABI_CHECK_FILE = 'lib/cext/ABI_check.txt'

  def check_abi
    # Check since the last commit at which ABI_CHECK_FILE or ABI_VERSION_FILE were modified
    base_commit = `git log -n 1 --format=%H #{ABI_VERSION_FILE} #{ABI_CHECK_FILE}`.chomp

    changed_files = changed_files(base_commit)
    # All files which can affect the ABI of libtruffleruby.so
    abi_files = %w[
      lib/truffle/rbconfig.rb
      lib/mri/mkmf.rb
      lib/cext/include/**/*.{h,hpp}
      src/main/c/cext/extconf.rb
      src/main/c/cext/*.{c,h}
    ].flat_map { |pattern| Dir[pattern].sort }

    changed_abi_files = changed_files & abi_files
    unless changed_abi_files.empty?
      puts 'These files have changed and might have affected the ABI:'
      puts changed_abi_files
      puts
      if changed_files.include?(ABI_VERSION_FILE)
        puts "#{ABI_VERSION_FILE} was updated to use a new ABI version"
      elsif changed_files.include?(ABI_CHECK_FILE)
        puts "#{ABI_CHECK_FILE} was updated, so ABI was marked as compatible"
      else
        puts 'Check the diff of this PR, and:'
        puts "* if ABI has changed, then increment #{ABI_VERSION_FILE}"
        puts "* if ABI has not changed, then increment #{ABI_CHECK_FILE}"
        puts
        puts 'Changing a macro, changing compilation flags, removing or adding a non-static function'
        puts '(because e.g. mkmf have_func can depend on that) should all be considered ABI changes.'
        abort
      end
    end
  end

  private def changed_files(base_commit)
    `git diff --cached --name-only #{base_commit}`.lines.map(&:chomp)
  end

  # Separate from lint as it needs to build
  def spotbugs
    mx 'ruby_spotbugs'
  end

  def sync
    exec(RbConfig.ruby, "#{TRUFFLERUBY_DIR}/tool/sync.rb", ruby_home)
  end

  def docker(*args)
    require_relative 'docker'
    JT::Docker.new.docker(*args)
  end

  def visualvm
    raw_sh "#{graalvm_home}/bin/jvisualvm"
  end
end

class JT
  include Commands

  def self.ruby(*args)
    jt = JT.new
    jt.send(:run_ruby, *args)
  end

  def self.gem_test_pack
    JT.new.gem_test_pack
  end

  def process_pre_args(args)
    needs_build = false
    needs_rebuild = false
    @silent = false
    @jdk_version = Integer(ENV['JT_JDK'] || 11)

    until args.empty?
      arg = args.shift
      case arg
      when '--build'
        needs_build = true
      when '--rebuild'
        needs_rebuild = true
      when '-u', '--use'
        @ruby_name = args.shift
      when '--silent'
        @silent = true
      when '--jdk'
        @jdk_version = Integer(args.shift)
      when '-h', '-help', '--help'
        help
        exit
      else
        args.unshift arg
        break
      end
    end

    raise "Invalid JDK version: #{@jdk_version}" unless [8, 11, 16].include?(@jdk_version)

    if needs_rebuild
      rebuild
    elsif needs_build
      build
    end
  end

  def main(args)
    args = args.dup
    process_pre_args(args)

    commands = Commands.public_instance_methods(false).map(&:to_s)

    command, *rest = args
    command ||= 'help'
    command = "command_#{command}" if %w[p puts format].include? command

    abort "no command matched #{command.inspect}" unless commands.include?(command)

    begin
      send(command, *rest)
    rescue
      puts "Error during command: #{args*' '}"
      raise $!
    end
  end
end

if $0 == __FILE__
  JT.new.main(ARGV)
end
