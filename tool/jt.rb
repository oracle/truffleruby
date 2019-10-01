#!/usr/bin/env ruby
# encoding: utf-8

# Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# A workflow tool for TruffleRuby development

require 'fileutils'
require 'json'
require 'timeout'
require 'yaml'
require 'rbconfig'
require 'pathname'

if RUBY_ENGINE != 'ruby' && !RUBY_DESCRIPTION.include?('Native')
  $stderr.puts 'WARNING: jt is not running on MRI or TruffleRuby Native, startup is slow'
  $stderr.puts '  Consider using following bash alias to run on MRI.'
  $stderr.puts '  `alias jt=/path/to/mri/bin/ruby /path/to/truffleruby/tool/jt.rb`'
end

abort "ERROR: jt requires Ruby 2.3 and above, was #{RUBY_VERSION}" if (RUBY_VERSION.split('.').map(&:to_i) <=> [2, 3, 0]) < 0

TRUFFLERUBY_DIR = File.expand_path('../..', File.realpath(__FILE__))
PROFILES_DIR = "#{TRUFFLERUBY_DIR}/profiles"

TRUFFLERUBY_GEM_TEST_PACK_VERSION = 'b6150c498a0764ffdc079cddf7f4e68cb141b65c'

JDEBUG = '--vm.agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y'
METRICS_REPS = Integer(ENV['TRUFFLERUBY_METRICS_REPS'] || 10)
DEFAULT_PROFILE_OPTIONS = %w[--cpusampler --cpusampler.SampleInternal=true --cpusampler.Mode=roots --cpusampler.Output=json]

RUBOCOP_INCLUDE_LIST = %w[
  lib/cext
  lib/truffle
  src/main/ruby/truffleruby
  src/test/ruby
  tool/generate-sulongmock.rb
  tool/jt.rb
  spec/truffle
]

ON_MAC = RbConfig::CONFIG['host_os'].include?('darwin')
ON_LINUX = RbConfig::CONFIG['host_os'].include?('linux')

SOEXT = ON_MAC ? 'dylib' : 'so'

# Expand GEM_HOME relative to cwd so it cannot be misinterpreted later.
ENV['GEM_HOME'] = File.expand_path(ENV['GEM_HOME']) if ENV['GEM_HOME']

require "#{TRUFFLERUBY_DIR}/lib/truffle/truffle/openssl-prefix.rb"

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
    SUBPROCESSES.each do |pid|
      Utilities.send_signal(signal, pid)
    end
    # Keep running jt which will wait for the subprocesses termination
  end
end

module Utilities

  private

  def bold(text)
    STDOUT.tty? ? "\e[1m#{text}\e[22m" : text
  end

  def ci?
    ENV.key?('BUILD_URL')
  end

  def get_truffle_version
    suite = File.read("#{TRUFFLERUBY_DIR}/mx.truffleruby/suite.py")
    raise unless /"name": "tools",.+?"version": "(\h{40})"/m =~ suite
    $1
  end

  def jvmci_update_and_version
    if env = ENV['JVMCI_VERSION']
      unless /8u(\d+)-(jvmci-0.\d+)/ =~ env
        raise 'Could not parse JDK update and JVMCI version from $JVMCI_VERSION'
      end
    else
      ci = File.read("#{TRUFFLERUBY_DIR}/ci.jsonnet")
      unless /JAVA_HOME: \{\n\s*name: "openjdk",\n\s*version: "8u(\d+)-(jvmci-.+)",/ =~ ci
        raise "JVMCI version not found in ci.jsonnet: #{ci[0, 1000]}"
      end
    end
    update, jvmci = $1, $2
    [update, jvmci]
  end

  def send_signal(signal, pid)
    STDERR.puts "\nSending #{signal} to process #{pid}"
    begin
      Process.kill(signal, pid)
    rescue Errno::ESRCH
      # Already killed
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

    @ruby_name ||= ENV['RUBY_BIN'] || 'jvm'
    @ruby_launcher = if @ruby_name == 'ruby'
                       ENV['RBENV_ROOT'] ? `rbenv which ruby`.chomp : which('ruby')
                     elsif File.executable?(@ruby_name)
                       @ruby_name
                     else
                       "#{TRUFFLERUBY_DIR}/mxbuild/truffleruby-#{@ruby_name}/#{language_dir}/ruby/bin/ruby"
                     end

    raise "The Ruby executable #{@ruby_launcher} does not exist" unless File.exist?(@ruby_launcher)
    raise "The Ruby executable #{@ruby_launcher} is not executable" unless File.executable?(@ruby_launcher)

    unless @silent
      shortened_path = @ruby_launcher.gsub(%r[^#{TRUFFLERUBY_DIR}/], '').gsub(%r[/bin/ruby$], '').gsub(%r[/#{language_dir}/ruby$], '')
      tags = [*('Native' if truffleruby_native?),
              *('Interpreted' if truffleruby? && !truffleruby_compiler?),
              truffleruby? ? 'TruffleRuby' : 'a Ruby',
              *('with Graal' if truffleruby_compiler?)]
      $stderr.puts "Using #{tags.join(' ')}: #{shortened_path}"
    end

    # use same ruby_launcher in subprocess jt instances
    # cannot be set while building
    ENV['RUBY_BIN'] = ruby_launcher
    @ruby_launcher
  end

  alias_method :require_ruby_launcher!, :ruby_launcher

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
    return @truffleruby_native unless @truffleruby_native.nil?
    # the truffleruby executable is bigger than 10MB if it is a native executable
    # the executable delegator for mac has less than 1MB
    @truffleruby_native = truffleruby? && File.size(truffleruby_launcher_path) > 10*1024*1024
  end

  def truffleruby_compiler?
    return @truffleruby_compiler unless @truffleruby_compiler.nil?

    return @truffleruby_compiler = false unless truffleruby?
    return @truffleruby_compiler = true if truffleruby_native?

    # Detect if the compiler is present by reading the $graalvm_home/release file
    # Use realpath to always use the executable in languages/ruby/bin/
    graalvm_home = File.expand_path("../../../..#{'/..' * language_dir.count('/')}", File.realpath(ruby_launcher))
    @truffleruby_compiler = File.readlines("#{graalvm_home}/release").grep(/^COMMIT_INFO=/).any? do |line|
      line.include?('"compiler":')
    end
  end

  def truffleruby?
    # only truffleruby has sibling truffleruby executable
    @truffleruby ||= File.executable?(truffleruby_launcher_path)
  end

  def truffleruby_launcher_path
    @truffleruby_launcher_path ||= File.join File.dirname(ruby_launcher), 'truffleruby'
  end

  def truffleruby!
    raise 'This command requires TruffleRuby.' unless truffleruby?
  end

  def find_repo(name)
    [TRUFFLERUBY_DIR, "#{TRUFFLERUBY_DIR}/.."].each do |dir|
      found = Dir.glob("#{dir}/#{name}*").sort.first
      return File.expand_path(found) if found
    end
    raise "Can't find the #{name} repo - clone it into the repository directory or its parent"
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

  def find_benchmark(benchmark)
    if File.exist?(benchmark)
      benchmark
    else
      File.join(TRUFFLERUBY_DIR, 'bench', benchmark)
    end
  end

  def find_gem(name)
    [TRUFFLERUBY_DIR, "#{TRUFFLERUBY_DIR}/.."].each do |dir|
      found = Dir.glob("#{dir}/#{name}").sort.first
      return File.expand_path(found) if found
    end
    raise "Can't find the #{name} gem - gem install it in this repository, or put it in the repository directory or its parent"
  end

  def git_branch
    @git_branch ||= `GIT_DIR="#{TRUFFLERUBY_DIR}/.git" git rev-parse --abbrev-ref HEAD`.strip
  end

  def igv_running?
    `ps ax`.include?('idealgraphvisualizer')
  end

  def no_gem_vars_env
    {
      'TRUFFLERUBY_RESILIENT_GEM_HOME' => nil,
      'GEM_HOME' => nil,
      'GEM_PATH' => nil,
      'GEM_ROOT' => nil,
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
        yield # Wait and read the pipe if capture: true
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
      raise ':capture can only be combined with :err => :out' if options.include?(:out)
      pipe_r, pipe_w = IO.pipe
      options[:out] = pipe_w
      options[:err] = pipe_w if options[:err] == :out
    end

    status = nil
    out = nil
    begin
      pid = Process.spawn(*args)
    rescue Errno::ENOENT # No such executable
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
      $stderr.puts "FAILED (#{status}): #{printable_cmd(args)}"
      $stderr.puts out if capture
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

    env = env.map { |k, v| "#{k}=#{shellescape(v)}" }
    # do not shellscpape if the command is passed as one string
    args = args.map { |a| shellescape(a) } if args.size > 1

    all = [*env, *args]
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
    cmd = ON_MAC ? 'open' : 'xdg-open'

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
    @java_home ||= ci? ? nil : ENV['JVMCI_HOME'] || install_jvmci
  end

  def language_dir
    java_home = find_java_home || ENV.fetch('JAVA_HOME')
    if java_home =~ /jdk-11/
      'languages'
    else
      'jre/languages'
    end
  end

  def mx(*args, **kwargs)
    mx_args = args.dup

    if java_home = find_java_home
      mx_args.unshift '--java-home', java_home
    end

    raw_sh find_mx, *mx_args, **kwargs
  end

  def mx_os
    if ON_MAC
      'darwin'
    elsif ON_LINUX
      'linux'
    else
      abort 'Unknown OS'
    end
  end

  def run_mspec(env_vars, command = 'run', *args)
    mspec_args = ['spec/mspec/bin/mspec', command, '--config', 'spec/truffle.mspec']
    Dir.chdir(TRUFFLERUBY_DIR) do
      ruby env_vars, *mspec_args, '-t', ruby_launcher, *args
    end
  end

  def newer?(input, output)
    return true unless File.exist? output
    File.mtime(input) > File.mtime(output)
  end
end

module Commands
  include Utilities

  def help
    # <<~ cannot be used since idea thinks TR is 2.1 and displays it as error
    puts <<-TXT.gsub(/^ {6}/, '')
      Usage: jt [options] COMMAND [command-options]
          Where options are:
          --build                   Runs `jt build` before the command
          --rebuild                 Runs `jt rebuild` before the command
          --use|-u [RUBY_SELECTOR]  Specifies which Ruby interpreter should be used in a given command. jt will apply
                                    options based on the given Ruby interpreter. Allowed values are:
                                    * name given to the --name option during build
                                    * absolute path of a Ruby interpreter
                                    * 'ruby' which uses the current Ruby executable in the PATH
                                    Default value is --use jvm, therefore all commands run on truffleruby-jvm by default.
                                    The default can be changed with `export RUBY_BIN=RUBY_SELECTOR`
          --silent                  Does not print the command and which Ruby is used

      jt build [graalvm|parser|options] ...   by default it builds graalvm
        jt build [parser|options] [options]
            parser                            build the parser
            options                           build the options
        jt build graalvm [options] [mx_options] [-- mx_build_options]
            graalvm                           build a GraalVM based on the given env file, the default is a minimal
                                              GraalVM with JVM and Truffleruby only available in mxbuild/truffleruby-jvm,
                                              the Ruby is symlinked into rbenv or chruby if available
            options:
              --[no-]sforceimports            run sforceimports before building (default: !ci?)
              --[no-]ee-checkout             checkout graal-enterprise when necessary (default: !ci?)
              --env|-e                        mx env file used to build the GraalVM, default is "jvm"
              --name|-n NAME                  specify the name of the build "mxbuild/truffleruby-NAME",
                                              it is also linked in your ruby manager (if found) under the same name,
                                              by default it is the name of the mx env file,
                                              the named build stays until it is rebuilt or deleted manually
            mx_options                        options passed directly to mx
            mx_build_options                  options passed to the build command of mx

      jt build_stats [--json] <attribute>            prints attribute's value from build process (e.g., binary size)
      jt clean                                       clean
      jt env                                         prints the current environment
      jt rebuild [build options]                     clean, sforceimports, and build
      jt dis <file>                                  finds the bc file in the project, disassembles, and returns new filename
      jt ruby [jt options] [--] [ruby options] args...
                                                     run TruffleRuby with args
          --stress        stress the compiler (compile immediately, foreground compilation, compilation exceptions are fatal)
          --reveal        enable assertions, show core Ruby files in backtrace
          --asm           show assembly
          --igv           make sure IGV is running and dump Graal graphs after partial escape
          --igv-full      show all phases, not just up to the Truffle partial escape
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
      #{MRI_TEST_MODULES.map { |k, h| format ' ' * 10 + '%-16s%s', k, h[:help] }.join("\n")}
      jt test mri test/mri/tests/test_find.rb [-- <MRI runner options>]
                                                     run tests in given file, -n option of the runner can be used to further
                                                     limit executed test methods
      jt test specs [fast] [mspec arguments] [-- ruby options]
      jt test specs                                  run all specs
      jt test specs fast                             run all specs except sub-processes, GC, sleep, ...
      jt test spec/ruby/language                     run specs in this directory
      jt test spec/ruby/language/while_spec.rb       run specs in this file
      jt test compiler                               run compiler tests
      jt test integration [TESTS]                    run integration tests
      jt test bundle [--jdebug]                      tests using bundler
      jt test gems [TESTS]                           tests using gems
      jt test ecosystem [TESTS] [options]            tests using the wider ecosystem such as bundler, Rails, etc
          --no-gem-test-pack                         run without gem test pack
      jt test cexts [--no-openssl] [--no-gems] [test_names...]
                                                     run C extension tests (set GEM_HOME)
      jt test unit                                   run Java unittests
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
      jt benchmark [options] args...                 run benchmark-interface
                                       note that to run most MRI benchmarks, you should translate them first with normal
                                       Ruby and cache the result, such as benchmark bench/mri/bm_vm1_not.rb --cache
                                       jt benchmark bench/mri/bm_vm1_not.rb --use-cache
      jt profile                                    profiles an application, including the TruffleRuby runtime, and generates a flamegraph
      jt where repos ...                            find these repositories
      jt next                                       tell you what to work on next (give you a random core library spec)
      jt install jvmci                              install a JVMCI JDK in the parent directory
      jt docker                                     build a Docker image - see doc/contributor/docker.md
      jt sync                                       continuously synchronize changes from the Ruby source files to the GraalVM build
      jt format                                     run eclipse code formatter

      you can also put --build or --rebuild in front of any command to build or rebuild first

      recognised environment variables:

        RUBY_BIN                                     The TruffleRuby executable to use (normally just bin/truffleruby)
        JVMCI_HOME                                   Path to the JVMCI JDK used for building with mx
        OPENSSL_PREFIX                               Where to find OpenSSL headers and libraries
    TXT
  end

  def truffle_version
    puts get_truffle_version
  end

  def mx(*args)
    super(*args)
  end

  def launcher
    puts ruby_launcher
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
        next if %w(. ..).include? File.basename(path)
        sh 'rm', '-rf', path
      end
    else
      raise ArgumentError, project
    end
  end

  def dis(file)
    dis = `which llvm-dis-3.8 llvm-dis 2>/dev/null`.lines.first.chomp
    file = `find #{TRUFFLERUBY_DIR} -name "#{file}"`.lines.first.chomp
    raise ArgumentError, "file not found:`#{file}`" if file.empty?
    sh dis, file
    puts Pathname(file).sub_ext('.ll')
  end

  def env
    puts 'Environment'
    env_vars = %w[JAVA_HOME JVMCI_HOME PATH RUBY_BIN
                  TRUFFLERUBY_RESILIENT_GEM_HOME
                  OPENSSL_PREFIX
                  TRUFFLERUBYOPT RUBYOPT]
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
        vm_args += %w[--vm.ea --vm.esa] unless truffleruby_native?
        add_experimental_options.call
        vm_args += %w[--backtraces-hide-core-files=false]
      when '--stress'
        vm_args << '--vm.Dgraal.TruffleCompileImmediately=true'
        vm_args << '--vm.Dgraal.TruffleBackgroundCompilation=false'
        vm_args << '--vm.Dgraal.TruffleCompilationExceptionsAreFatal=true'
      when '--asm'
        vm_args += %w[--vm.XX:+UnlockDiagnosticVMOptions --vm.XX:CompileCommand=print,*::callRoot]
      when '--jdebug'
        vm_args << JDEBUG
      when '--jexception', '--jexceptions'
        add_experimental_options.call
        vm_args << '--exceptions-print-uncaught-java=true'
      when '--infopoints'
        vm_args << '--vm.XX:+UnlockDiagnosticVMOptions' << '--vm.XX:+DebugNonSafepoints'
        vm_args << '--vm.Dgraal.TruffleEnableInfopoints=true'
      when '--fg'
        vm_args << '--vm.Dgraal.TruffleBackgroundCompilation=false'
      when '--trace'
        truffleruby_compiler!
        vm_args << '--vm.Dgraal.TraceTruffleCompilation=true'
      when '--igv', '--igv-full'
        truffleruby_compiler!
        vm_args << (arg == '--igv-full' ? '--vm.Dgraal.Dump=:2' : '--vm.Dgraal.Dump=TruffleTree,PartialEscape:2')
        vm_args << '--vm.Dgraal.PrintGraphFile=true' unless igv_running?
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

    if core_load_path && truffleruby? && !truffleruby_native?
      add_experimental_options.call
      vm_args << "--core-load-path=#{TRUFFLERUBY_DIR}/src/main/ruby/truffleruby"
    end

    [vm_args, ruby_args + args, options]
  end

  private def run_ruby(*args)
    env_vars = args.first.is_a?(Hash) ? args.shift : {}
    options = args.last.is_a?(Hash) ? args.pop : {}

    vm_args, ruby_args, options = ruby_options(options, args)

    options[:no_print_cmd] = true if @silent

    if truffleruby? && !truffleruby_native?
      %w[TRUFFLERUBYOPT RUBYOPT].each do |env_name|
        if ENV[env_name] && !ENV[env_name].empty?
          vm_options = ENV[env_name].split(' ').select { |v| v =~ /^--(jvm|vm)/ }
          $stderr.print "Not a native build, applying --jvm and --vm options #{vm_options.inspect} from #{env_name} \"#{ENV[env_name]}\" directly as options. "
          $stderr.puts 'Otherwise they would be ignored since jvm process cannot apply them to itself.'
          vm_args += vm_options
        end
      end
    end

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
    target = "#{cext_dir}/lib/#{name}/#{name}.su"
    compile_cext(name, ext_dir, target, clang_opts)
  end

  private def compile_cext(name, ext_dir, target, clang_opts, env: {})
    extconf = "#{ext_dir}/extconf.rb"
    raise "#{extconf} does not exist" unless File.exist?(extconf)

    chdir(ext_dir) do
      run_ruby(env, '-rmkmf', "#{ext_dir}/extconf.rb") # -rmkmf is required for C ext tests
      if File.exist?('Makefile')
        raw_sh('make')
        FileUtils::Verbose.cp("#{name}.su", target) if target
      else
        STDERR.puts "Makefile not found in #{ext_dir}, skipping make."
      end
    end
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
        out = raw_sh 'git', '-C', dir, 'remote', capture: true, no_print_cmd: true
        out.split.map do |remote|
          url = raw_sh 'git', '-C', dir, 'config', '--get', "remote.#{remote}.url", capture: true, no_print_cmd: true
          [remote, url.chomp]
        end
      end
    end

    def url(remote_name, dir = TRUFFLERUBY_DIR)
      remote_urls(dir).find { |r, _u| r == remote_name }.last
    end

    def try_fetch(repo)
      remote = github(repo) || bitbucket(repo) || 'origin'
      raw_sh 'git', '-C', repo, 'fetch', remote, continue_on_failure: true
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
      tests = rest.empty? ? ['org.truffleruby'] : rest
      # TODO (eregon, 4 Feb 2019): This should run on GraalVM, not development jars
      # The home needs to be set, otherwise TruffleFile does not allow access to files in the TruffleRuby home,
      # because it cannot find the correct home.
      home = "-Druby.home=#{TRUFFLERUBY_DIR}/mxbuild/truffleruby-jvm/#{language_dir}/ruby"
      mx 'unittest', home, *tests
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
      glob = Dir.glob(pattern)
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
      'RUBYGEMS_TEST_PATH' => MRI_TEST_PREFIX,
      'RUBYOPT' => [*ENV['RUBYOPT'], '--disable-gems'].join(' '),
      'TRUFFLERUBY_RESILIENT_GEM_HOME' => nil,
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
        compile_dir = if cext_name.include?('/')
                        if Dir.exist?("#{MRI_TEST_CEXT_DIR}/#{cext_name}")
                          "#{MRI_TEST_CEXT_DIR}/#{cext_name}"
                        else
                          "#{MRI_TEST_CEXT_DIR}/#{File.dirname(cext_name)}"
                        end
                      else
                        if Dir.exist?("#{MRI_TEST_CEXT_DIR}/#{cext_name}")
                          "#{MRI_TEST_CEXT_DIR}/#{cext_name}"
                        else
                          "#{MRI_TEST_CEXT_DIR}/#{cext_name.gsub('_', '-')}"
                        end
                      end
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
    raise unless test_files.size == 1
    test_file = test_files[0]
    test_classes = File.read(test_file).scan(/class ([\w:]+) < .+TestCase/)
    test_classes.each do |test_class,|
      prefix = "test/mri/excludes/#{test_class.gsub('::', '/')}"
      FileUtils::Verbose.rm_f "#{prefix}.rb"
      FileUtils::Verbose.rm_rf prefix
    end

    puts '1. Tagging tests'
    output_file = 'mri_tests.txt'
    run_mri_tests(options, test_files, [], out: output_file, continue_on_failure: true)

    puts '2. Parsing errors'
    sh 'ruby', 'tool/parse_mri_errors.rb', output_file

    puts '3. Verifying tests pass'
    run_mri_tests(options, test_files, [], use_exec: true)
  end

  private def test_compiler(*args)
    truffleruby_compiler!
    env = {}

    env['TRUFFLERUBYOPT'] = [*ENV['TRUFFLERUBYOPT'], '--experimental-options', '--exceptions-print-java=true'].join(' ')

    Dir["#{TRUFFLERUBY_DIR}/test/truffle/compiler/*.sh"].sort.each do |test_script|
      if args.empty? or args.include?(File.basename(test_script, '.*'))
        sh env, test_script
      end
    end
  end

  private def test_cexts(*args)
    all_tests = %w(tools openssl minimum method module globals backtraces xopenssl postinstallhook gems)
    no_openssl = args.delete('--no-openssl')
    no_gems = args.delete('--no-gems')
    tests = args.empty? ? all_tests : all_tests & args
    tests -= %w[openssl xopenssl] if no_openssl
    tests.delete 'gems' if no_gems

    if ENV['TRUFFLERUBY_CI'] && ON_MAC
      raise 'no system clang' unless which('clang')
      %w[opt llvm-link].each do |tool|
        raise "should not have #{tool} in PATH in TruffleRuby CI" if which(tool)
      end
    end

    tests.each do |test_name|
      case test_name
      when 'tools'
        # Test tools
        run_ruby 'test/truffle/cexts/test-preprocess.rb'

      when 'openssl'
        # Test that we can compile and run some basic C code that uses openssl
        if ENV['OPENSSL_PREFIX']
          openssl_cflags = ['-I', "#{ENV['OPENSSL_PREFIX']}/include"]
          openssl_lib = "#{ENV['OPENSSL_PREFIX']}/lib/libssl.#{SOEXT}"
        else
          openssl_cflags = []
          openssl_lib = "libssl.#{SOEXT}"
        end

        sh 'clang', '-c', '-emit-llvm', *openssl_cflags, 'test/truffle/cexts/xopenssl/main.c', '-o', 'test/truffle/cexts/xopenssl/main.bc'
        mx 'build', '--dependencies', 'SULONG_LAUNCHER' # For mx lli
        out = mx('lli', "-Dpolyglot.llvm.libraries=#{openssl_lib}", 'test/truffle/cexts/xopenssl/main.bc', capture: true)
        raise out.inspect unless out == "5d41402abc4b2a76b9719d911017c592\n"

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
            abort <<-EOS
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
        sh({'TRUFFLERUBY_RECOMPILE_OPENSSL' => 'true'}, "mxbuild/truffleruby-jvm/#{language_dir}/ruby/lib/truffle/post_install_hook.sh")

      when 'gems'
        # Test that we can compile and run some real C extensions

        gem_home = "#{gem_test_pack}/gems"

        tests = [
            ['oily_png', ['chunky_png-1.3.6', 'oily_png-1.2.0'], ['oily_png']],
            ['psd_native', ['chunky_png-1.3.6', 'oily_png-1.2.0', 'bindata-2.3.1', 'hashie-3.4.4', 'psd-enginedata-1.1.1', 'psd-2.1.2', 'psd_native-1.1.3'], ['oily_png', 'psd_native']],
            ['nokogiri', [], ['nokogiri']]
        ]

        tests.each do |gem_name, dependencies, libs|
          puts '', gem_name
          next if gem_name == 'nokogiri' # nokogiri totally excluded
          gem_root = "#{TRUFFLERUBY_DIR}/test/truffle/cexts/#{gem_name}"
          ext_dir = Dir.glob("#{gem_home}/gems/#{gem_name}*/")[0] + "ext/#{gem_name}"

          compile_cext gem_name, ext_dir, "#{gem_root}/lib/#{gem_name}/#{gem_name}.su", ['-Werror=implicit-function-declaration']

          next if gem_name == 'psd_native' # psd_native is excluded just for running
          run_ruby(*dependencies.map { |d| "-I#{gem_home}/gems/#{d}/lib" },
                   *libs.map { |l| "-I#{TRUFFLERUBY_DIR}/test/truffle/cexts/#{l}/lib" },
                   "#{TRUFFLERUBY_DIR}/test/truffle/cexts/#{gem_name}/test.rb", gem_root)
        end

          # Tests using gem install to compile the cexts
        sh 'test/truffle/cexts/puma/puma.sh'
        sh 'test/truffle/cexts/sqlite3/sqlite3.sh'
        sh 'test/truffle/cexts/unf_ext/unf_ext.sh'
        sh 'test/truffle/cexts/json/json.sh'

          # Test a gem dynamically compiling a C extension
        sh 'test/truffle/cexts/RubyInline/RubyInline.sh'

          # Test cexts used by many projects
        sh 'test/truffle/cexts/msgpack/msgpack.sh'
      else
        raise "unknown test: #{test_name}"
      end
    end
  end

  private def test_integration(*args)
    tests_path             = "#{TRUFFLERUBY_DIR}/test/truffle/integration"
    single_test            = !args.empty?
    test_names             = single_test ? '{' + args.join(',') + '}' : '*'

    Dir["#{tests_path}/#{test_names}.sh"].sort.each do |test_script|
      sh test_script
    end
  end

  private def test_gems(*args)
    gem_test_pack

    tests_path             = "#{TRUFFLERUBY_DIR}/test/truffle/gems"
    single_test            = !args.empty?
    test_names             = single_test ? '{' + args.join(',') + '}' : '*'

    Dir["#{tests_path}/#{test_names}.sh"].sort.each do |test_script|
      sh test_script
    end
  end

  private def test_ecosystem(*args)
    use_gem_test_pack = !args.delete('--no-gem-test-pack')
    gem_test_pack if use_gem_test_pack

    tests_path             = "#{TRUFFLERUBY_DIR}/test/truffle/ecosystem"
    single_test            = !args.empty?
    test_names             = single_test ? '{' + args.join(',') + '}' : '*'

    candidates = Dir["#{tests_path}/#{test_names}.sh"].sort
    if candidates.empty?
      targets = Dir["#{tests_path}/*.sh"].sort.map { |f| File.basename(f, '.*') }
      puts "No targets found by pattern #{test_names}. Available targets: "
      targets.each { |t| puts " * #{t}" }
      exit 1
    end
    success = candidates.all? do |test_script|
      sh test_script, *('--no-gem-test-pack' unless use_gem_test_pack), continue_on_failure: true
    end
    exit success
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
            FileUtils.remove_entry_secure temp_dir
          end
        end
      end
    ensure
      Process.kill :INT, gem_server
      Process.wait gem_server
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

    delimiter_index = args.index('--')
    args, ruby_args = if delimiter_index
                        [args[0...delimiter_index], args[(delimiter_index + 1)..-1]]
                      else
                        [args, []]
                      end

    vm_args, ruby_args, parsed_options = ruby_options({}, ['--reveal', *ruby_args])
    vm_args += ['--vm.Xmx2G', *('--polyglot' unless truffleruby_native?)]

    raise "unsupported options #{parsed_options}" unless parsed_options.empty?

    prefixed_ruby_args = [*(vm_args if truffleruby?), *ruby_args].map { |v| "-T#{v}" }
    run_mspec env_vars, command, *options, *prefixed_ruby_args, *args
  end

  def gem_test_pack
    name = 'truffleruby-gem-test-pack'
    gem_test_pack = File.expand_path(name, TRUFFLERUBY_DIR)

    unless Dir.exist?(gem_test_pack)
      $stderr.puts 'Cloning the truffleruby-gem-test-pack repository'
      unless Remotes.bitbucket
        abort 'Need a git remote in truffleruby with the internal repository URL or use --no-gem-test-pack option'
      end
      url = Remotes.url(Remotes.bitbucket).sub('truffleruby', name)
      sh 'git', 'clone', url
    end

    # Unset variable set by the pre-commit hook which confuses git
    ENV.delete 'GIT_INDEX_FILE'

    current = `git -C #{gem_test_pack} rev-parse HEAD`.chomp
    unless current == TRUFFLERUBY_GEM_TEST_PACK_VERSION
      has_commit = raw_sh 'git', '-C', gem_test_pack, 'cat-file', '-e', TRUFFLERUBY_GEM_TEST_PACK_VERSION, continue_on_failure: true
      Remotes.try_fetch(gem_test_pack) unless has_commit
      raw_sh 'git', '-C', gem_test_pack, 'checkout', '-q', TRUFFLERUBY_GEM_TEST_PACK_VERSION
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
      out = run_ruby '--vm.Dtruffleruby.metrics.memory_used_on_exit=true', '--vm.verbose:gc', *args, capture: true, :err => :out, no_print_cmd: true
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

      max_rss_in_mb = if ON_LINUX
                        out = raw_sh('/usr/bin/time', '-v', '--', ruby_launcher, *args, capture: true, :err => :out, no_print_cmd: true)
                        out =~ /Maximum resident set size \(kbytes\): (?<max_rss_in_kb>\d+)/m
                        Integer($~[:max_rss_in_kb]) / 1024.0
                      elsif ON_MAC
                        out = raw_sh('/usr/bin/time', '-l', '--', ruby_launcher, *args, capture: true, :err => :out, no_print_cmd: true)
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

    out = raw_sh('perf', 'stat', '-e', 'instructions', '--', ruby_launcher, *args, capture: true, :err => :out, no_print_cmd: true)

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

    samples = METRICS_REPS.times.map do
      log '.', "sampling\n"
      start = Time.now
      out = run_ruby(*args, capture: true, no_print_cmd: true, :err => :out)
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

  def command_format(*args)
    mx 'eclipseformat', '--no-backup', '--primary', *args, continue_on_failure: true
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
          $stderr.puts line
        end
      # [Full GC (Metadata GC Threshold)  23928K->23265K(216064K), 0.1168747 secs]
      elsif line =~ /^\[(.+), (\d+\.\d+) secs\]$/
        name = $1
        time = Float($2)
        stack << [name, time]
        result[stack.map(&:first)] += (time * 1000.0)
        stack.pop
      else
        $stderr.puts line
      end
    end

    result[%w[total jvm]] = total - result[%w[total main]]
    result
  end

  def benchmark(*args)
    require_ruby_launcher!

    args.map! do |a|
      if a.include?('.rb')
        benchmark = find_benchmark(a)
        raise 'benchmark not found' unless File.exist?(benchmark)
        benchmark
      else
        a
      end
    end

    run_args = []

    if truffleruby?
      run_args.push '--vm.Dgraal.TruffleCompilationExceptionsAreFatal=true'
    end

    run_args.push "-I#{find_gem('benchmark-ips')}/lib" rescue nil
    run_args.push "#{TRUFFLERUBY_DIR}/bench/benchmark-interface/bin/benchmark"
    run_args.push(*args)

    run_ruby(*run_args, use_exec: true)
  end

  def profile(*args)
    require_ruby_launcher!
    env = args.first.is_a?(Hash) ? args.shift : {}

    require 'tempfile'

    repo = find_or_clone_repo('https://github.com/eregon/FlameGraph.git', 'graalvm')

    run_args = *DEFAULT_PROFILE_OPTIONS + args

    begin
      profile_data = run_ruby(env, *run_args, capture: true)

      profile_data_file = Tempfile.new %w[truffleruby-profile .json]
      profile_data_file.write(profile_data)
      profile_data_file.close

      flamegraph_data = raw_sh "#{repo}/stackcollapse-graalvm.rb", profile_data_file.path, capture: true

      flamegraph_data_file = Tempfile.new 'truffleruby-flamegraph-data'
      flamegraph_data_file.write(flamegraph_data)
      flamegraph_data_file.close

      svg_data = raw_sh "#{repo}/flamegraph.pl", flamegraph_data_file.path, capture: true

      Dir.mkdir(PROFILES_DIR) unless Dir.exist?(PROFILES_DIR)
      svg_filename = "#{PROFILES_DIR}/flamegraph_#{Time.now.strftime("%Y%m%d-%H%M%S")}.svg"
      File.open(svg_filename, 'w') { |f| f.write(svg_data) }
      app_open svg_filename
    ensure
      flamegraph_data_file.close! if flamegraph_data_file
      profile_data_file.close! if profile_data_file
    end
  end

  def where(*args)
    case args.shift
    when 'repos'
      args.each do |a|
        puts find_repo(a)
      end
    end
  end

  def install(name, *options)
    case name
    when 'jvmci'
      install_jvmci
    else
      raise "Unknown how to install #{what}"
    end
  end

  private def install_jvmci
    raise 'Installing JVMCI is only available on Linux and macOS currently' unless ON_LINUX || ON_MAC

    update, jvmci_version = jvmci_update_and_version
    dir = File.expand_path('..', TRUFFLERUBY_DIR)
    java_home = chdir(dir) do
      dir_pattern = "#{dir}/openjdk1.8.0*#{jvmci_version}"
      if Dir[dir_pattern].empty?
        puts 'Downloading JDK8 with JVMCI'
        jvmci_releases = 'https://github.com/graalvm/openjdk8-jvmci-builder/releases/download'
        filename = "openjdk-8u#{update}-#{jvmci_version}-#{mx_os}-amd64.tar.gz"
        raw_sh 'curl', '-L', "#{jvmci_releases}/#{jvmci_version}/#{filename}", '-o', filename
        raw_sh 'tar', 'xf', filename
      end
      dirs = Dir[dir_pattern]
      abort "ambiguous JVMCI directories:\n#{dirs.join("\n")}" if dirs.length != 1
      extracted = dirs.first
      ON_MAC ? "#{extracted}/Contents/Home" : extracted
    end

    abort 'Could not find the extracted JDK' unless java_home
    java_home = File.expand_path(java_home)

    java = "#{java_home}/bin/java"
    abort "#{java_home} does not exist" unless File.executable?(java)

    puts java_home
    java_home
  end

  def checkout_enterprise_revision
    ee_path = File.expand_path File.join(TRUFFLERUBY_DIR, '..', 'graal-enterprise')
    graal_path = File.expand_path File.join(TRUFFLERUBY_DIR, '..', 'graal')
    unless File.directory?(ee_path)
      github_ee_url = 'https://github.com/graalvm/graal-enterprise.git'
      bitbucket_ee_url = raw_sh('mx', 'urlrewrite', github_ee_url, capture: true).chomp
      if bitbucket_ee_url == github_ee_url
        raise "#{ee_path} is missing and could not be cloned using urlrewrite, clone the repository manually or setup the urlrewrite rules"
      end
      raw_sh 'git', 'clone', bitbucket_ee_url, ee_path
    end

    raw_sh 'git', '-C', ee_path, 'fetch', 'origin'

    suite_file = File.join ee_path, 'vm-enterprise/mx.vm-enterprise/suite.py'
    # Find the latest merge commit of a pull request in the graal repo, equal or older than our graal import.
    merge_commit_in_graal = raw_sh(
        'git', '-C', graal_path, 'log', '--pretty=%H', '--grep=PullRequest:', '--merges', '--max-count=1', get_truffle_version,
        capture: true).chomp
    # Find the commit importing that version of graal in graal-enterprise by looking at the suite file.
    # The suite file is automatically updated on every graal PR merged.
    graal_enterprise_commit = raw_sh(
        'git', '-C', ee_path, 'log', 'origin/master', '--pretty=%H', '--grep=PullRequest:', '--reverse', '-m',
        '-S', merge_commit_in_graal, '--', suite_file, capture: true).lines.first.chomp
    raw_sh('git', '-C', ee_path, 'checkout', graal_enterprise_commit)
  end

  private def build_graalvm(*options)
    raise 'use --env jvm-ce instead' if options.delete('--graal')
    raise 'use --env native instead' if options.delete('--native')

    env = if (i = options.index('--env') || options.index('-e'))
            options.delete_at i
            options.delete_at i
          else
            'jvm'
          end
    native = env.include? 'native'
    name = 'truffleruby-' + if (i = options.index('--name') || options.index('-n'))
                              options.delete_at i
                              options.delete_at i
                            else
                              env
                            end

    sforceimports = options.delete('--sforceimports') || (options.delete('--no-sforceimports') ? false : !ci?)
    mx('-p', TRUFFLERUBY_DIR, 'sforceimports') if sforceimports && !ci?

    ee_checkout = options.delete('--ee-checkout') || (options.delete('--no-ee-checkout') ? false : !ci?)
    checkout_enterprise_revision if env.include?('ee') && ee_checkout

    delimiter_index = options.index '--'
    mx_options, mx_build_options = if delimiter_index
                                     [options[0...delimiter_index], options[(delimiter_index + 1)..-1]]
                                   else
                                     [options, nil]
                                   end

    mx_args = ['-p', TRUFFLERUBY_DIR, '--env', env, *mx_options]

    mx(*mx_args, 'build', *mx_build_options)
    build_dir = mx(*mx_args, 'graalvm-home', capture: true).lines.last.chomp

    dest = "#{TRUFFLERUBY_DIR}/mxbuild/#{name}"
    dest_ruby = "#{dest}/#{language_dir}/ruby"
    dest_bin = "#{dest_ruby}/bin"
    FileUtils.rm_rf dest
    FileUtils.cp_r build_dir, dest

    # Insert native wrapper around the bash launcher
    # since nested shebang does not work on macOS when fish shell is used.
    if ON_MAC && !native
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
        puts "Deleted old link: #{link} -> #{target}"
      end

      link_path = "#{rubies_dir}/#{name}"
      File.delete link_path if File.exist? link_path
      File.symlink dest_ruby, link_path
    end
  end

  def next(*args)
    puts `cat spec/tags/core/**/**.txt | grep 'fails:'`.lines.sample
  end

  def native_launcher
    sh 'cc', '-o', 'tool/native_launcher_darwin', 'tool/native_launcher_darwin.c'
  end
  alias :'native-launcher' :native_launcher

  private def check_dsl_usage
    # Change the annotation retention policy in Truffle so we can inspect specializations.
    raw_sh("find ../graal/truffle/ -type f -name '*.java' -exec grep -q 'RetentionPolicy\.CLASS' '{}' \\; -exec sed -i.jtbak 's/RetentionPolicy\.CLASS/RetentionPolicy\.RUNTIME/g' '{}' \\;")
    begin
      mx 'clean', '--dependencies', 'org.truffleruby'
      # We need to build with -parameters to get parameter names.
      mx 'build', '--dependencies', 'org.truffleruby', '--force-javac', '-A-parameters'
      # Re-build GraalVM to run the check
      build_graalvm
      run_ruby({ 'TRUFFLE_CHECK_DSL_USAGE' => 'true' }, '--lazy-default=false', '-e', 'exit')
    ensure
      # Revert the changes we made to the Truffle source.
      raw_sh("find ../graal/truffle/ -name '*.jtbak' -exec sh -c 'mv -f $0 ${0%.jtbak}' '{}' \\;")
    end
  end

  def rubocop(*args)
    gem_home = "#{gem_test_pack}/rubocop-gems"
    env = {
      'GEM_HOME' => gem_home,
      'GEM_PATH' => gem_home,
      'PATH' => "#{gem_home}/bin:#{ENV['PATH']}"
    }
    if args.empty? or args.all? { |arg| arg.start_with?('-') }
      args += RUBOCOP_INCLUDE_LIST
    end
    sh env, 'ruby', "#{gem_home}/bin/rubocop", *args
  end

  private def check_filename_length
    # For eCryptfs, see https://bugs.launchpad.net/ecryptfs/+bug/344878
    max_length = 143

    too_long = []
    Dir.chdir(TRUFFLERUBY_DIR) do
      Dir.glob('**/*') do |f|
        if File.basename(f).size > max_length
          too_long << f
        end
      end
    end

    unless too_long.empty?
      abort "Too long filenames for eCryptfs:\n#{too_long.join "\n"}"
    end
  end

  private def check_parser
    build('parser')
    diff = sh 'git', 'diff', 'src/main/java/org/truffleruby/parser/parser/RubyParser.java', capture: true
    unless diff.empty?
      STDERR.puts 'DIFF:'
      STDERR.puts diff
      abort "RubyParser.y must be modified and RubyParser.java regenerated by 'jt build parser'"
    end
  end

  private def check_documentation_urls
    url_base = 'https://github.com/oracle/truffleruby/blob/master/doc/'
    # Explicit list of URLs, so they can be added manually
    # Notably, Ruby installers reference the LLVM urls
    known_hardcoded_urls = %w[
      https://github.com/oracle/truffleruby/blob/master/doc/user/installing-libssl.md
      https://github.com/oracle/truffleruby/blob/master/doc/user/installing-llvm.md
      https://github.com/oracle/truffleruby/blob/master/doc/user/installing-zlib.md
    ]

    known_hardcoded_urls.each do |url|
      file = url[url_base.size..-1]
      path = "#{TRUFFLERUBY_DIR}/doc/#{file}"
      unless File.file?(path)
        abort "#{path} could not be found but is referenced in code"
      end
    end

    hardcoded_urls = `git -C #{TRUFFLERUBY_DIR} grep -Fn #{url_base.inspect}`
    hardcoded_urls.each_line do |line|
      abort "Could not parse #{line.inspect}" unless /(.+?):(\d+):.+?(https:.+?)[ "'\n]/ =~ line
      file, line, url = $1, $2, $3
      if file != 'tool/jt.rb' and !known_hardcoded_urls.include?(url)
        abort "Found unknown hardcoded url #{url} in #{file}:#{line}, add it in tool/jt.rb"
      end
    end
  end

  def checkstyle
    mx 'checkstyle', '-f', '--primary'
  end

  def make_specializations_protected
    changed = false
    count_braces = -> line { line.count('(') - line.count(')') }

    Dir.glob(File.join(TRUFFLERUBY_DIR, 'src', '**', '*.java')) do |file|
      content = File.read file
      new_content = ''

      looking = false
      braces = 0
      content.lines.each do |line|
        if looking
          if braces == 0
            if line =~ /\w+(\[\])? \w+\(/
              new_line = line.
                  # change to protected
                  gsub(/^( *)(public |protected |private |)/, '\1protected ')
              new_content << new_line
              looking = false
            else
              new_content << line
            end
          else
            braces += count_braces.(line)
            new_content << line
          end
        else
          looking = line.match?(/^ *@(Specialization|Fallback|CreateCast)/)
          new_content << line
          if looking
            braces = count_braces.(line)
          end
        end
      end

      if content != new_content
        puts "#{file} updated"
        changed = true
        File.write file, new_content
      end
    end
    changed
  end

  def lint(*args)
    check_filename_length
    rubocop
    sh 'tool/lint.sh'
    mx 'gate', '--tags', 'style'

    # TODO (pitr-ch 11-Aug-2019): consider running all tasks in the `mx gate --tags fullbuild`
    #  - includes verifylibraryurls though
    #  - building with jdt in the ci definition could be dropped since fullbuild builds with JDT
    mx 'spotbugs'

    check_dsl_usage unless args.delete '--no-build'

    check_parser
    check_documentation_urls
    abort 'Some Specializations were not protected.' if make_specializations_protected
  end

  def sync
    exec(RbConfig.ruby, "#{TRUFFLERUBY_DIR}/tool/sync.rb")
  end

  def docker(*args)
    command = args.shift
    case command
    when 'build'
      docker_build(*args)
    when nil, 'test'
      docker_test(*args)
    when 'print'
      puts dockerfile(*args)
    else
      abort "Unkown jt docker command #{command}"
    end
  end

  private def docker_build(*args)
    if args.first.nil? || args.first.start_with?('--')
      image_name = 'truffleruby-test'
    else
      image_name = args.shift
    end
    docker_dir = File.join(TRUFFLERUBY_DIR, 'tool', 'docker')
    File.write(File.join(docker_dir, 'Dockerfile'), dockerfile(*args))
    sh 'docker', 'build', '-t', image_name, '.', chdir: docker_dir
  end

  private def docker_test(*args)
    distros = ['--ol7', '--ubuntu1804', '--ubuntu1604', '--fedora28']
    managers = ['--no-manager', '--rbenv', '--chruby', '--rvm']

    distros.each do |distro|
      managers.each do |manager|
        puts '**********************************'
        puts '**********************************'
        puts '**********************************'
        distros.each do |d|
          managers.each do |m|
            print "#{d} #{m}"
            print '     <---' if [d, m] == [distro, manager]
            puts
          end
        end
        puts '**********************************'
        puts '**********************************'
        puts '**********************************'

        docker 'build', distro, manager, *args
      end
    end
  end

  private def dockerfile(*args)
    config = @config ||= YAML.load_file(File.join(TRUFFLERUBY_DIR, 'tool', 'docker-configs.yaml'))

    truffleruby_repo = 'https://github.com/oracle/truffleruby.git'
    distro = 'ol7'
    install_method = :public
    public_version = '1.0.0-rc14'
    rebuild_images = false
    rebuild_openssl = true
    manager = :none
    basic_test = false
    full_test = false

    until args.empty?
      arg = args.shift
      case arg
      when '--repo'
        truffleruby_repo = args.shift
      when '--ol7', '--ubuntu1804', '--ubuntu1604', '--fedora28'
        distro = arg[2..-1]
      when '--public'
        install_method = :public
        public_version = args.shift
      when '--graalvm'
        install_method = :graalvm
        graalvm_tarball = args.shift
        graalvm_component = args.shift
      when '--standalone'
        install_method = :standalone
        standalone_tarball = args.shift
      when '--source'
        install_method = :source
        source_branch = args.shift
      when '--rebuild-images'
        rebuild_images = true
        native_component = args.shift
      when '--no-rebuild-openssl'
        rebuild_openssl = false
      when '--no-manager'
        manager = :none
      when '--rbenv', '--chruby', '--rvm'
        manager = arg[2..-1].to_sym
      when '--basic-test'
        basic_test = true
      when '--test'
        full_test = true
        test_branch = args.shift
      else
        abort "unknown option #{arg}"
      end
    end

    distro = config.fetch(distro)
    run_post_install_hook = rebuild_openssl

    lines = []

    lines.push "FROM #{distro.fetch('base')}"

    lines.push(*distro.fetch('setup'))

    lines.push(*distro.fetch('locale'))

    lines.push(*distro.fetch('curl')) if install_method == :public
    lines.push(*distro.fetch('git')) if install_method == :source || manager != :none || full_test
    lines.push(*distro.fetch('which')) if manager == :rvm || full_test
    lines.push(*distro.fetch('find')) if full_test
    lines.push(*distro.fetch('rvm')) if manager == :rvm
    lines.push(*distro.fetch('source')) if install_method == :source
    lines.push(*distro.fetch('images')) if rebuild_images

    lines.push(*distro.fetch('zlib'))
    lines.push(*distro.fetch('openssl'))
    lines.push(*distro.fetch('cext'))
    lines.push(*distro.fetch('cppext'))

    lines.push 'WORKDIR /test'
    lines.push 'RUN useradd -ms /bin/bash test'
    lines.push 'RUN chown test /test'
    lines.push 'USER test'

    docker_dir = File.join(TRUFFLERUBY_DIR, 'tool', 'docker')

    check_post_install_message = [
      "RUN grep 'The Ruby openssl C extension needs to be recompiled on your system to work with the installed libssl' install.log",
      "RUN grep '/#{language_dir}/ruby/lib/truffle/post_install_hook.sh' install.log"
    ]

    case install_method
    when :public
      graalvm_tarball = "graalvm-ce-#{public_version}-linux-amd64.tar.gz"
      lines.push "RUN curl -OL https://github.com/oracle/graal/releases/download/vm-#{public_version}/#{graalvm_tarball}"
      graalvm_base = '/test/graalvm'
      lines.push "RUN mkdir #{graalvm_base}"
      lines.push "RUN tar -zxf #{graalvm_tarball} -C #{graalvm_base} --strip-components=1"
      lines.push "RUN #{graalvm_base}/bin/gu install org.graalvm.ruby | tee install.log"
      lines.push(*check_post_install_message)
      ruby_base = "#{graalvm_base}/#{language_dir}/ruby"
      graalvm_bin = "#{graalvm_base}/bin"
      ruby_bin = graalvm_bin
      lines.push "RUN #{ruby_base}/lib/truffle/post_install_hook.sh" if run_post_install_hook
    when :graalvm
      FileUtils.copy graalvm_tarball, docker_dir
      FileUtils.copy graalvm_component, docker_dir
      graalvm_tarball = File.basename(graalvm_tarball)
      graalvm_component = File.basename(graalvm_component)
      lines.push "COPY #{graalvm_tarball} /test/"
      lines.push "COPY #{graalvm_component} /test/"
      graalvm_base = '/test/graalvm'
      lines.push "RUN mkdir #{graalvm_base}"
      lines.push "RUN tar -zxf #{graalvm_tarball} -C #{graalvm_base} --strip-components=1"
      ruby_base = "#{graalvm_base}/#{language_dir}/ruby"
      graalvm_bin = "#{graalvm_base}/bin"
      ruby_bin = graalvm_bin
      lines.push "RUN #{graalvm_bin}/gu install --file /test/#{graalvm_component} | tee install.log"
      lines.push(*check_post_install_message)
      lines.push "RUN #{ruby_base}/lib/truffle/post_install_hook.sh" if run_post_install_hook
    when :standalone
      FileUtils.copy standalone_tarball, docker_dir
      standalone_tarball = File.basename(standalone_tarball)
      lines.push "COPY #{standalone_tarball} /test/"
      ruby_base = '/test/truffleruby-standalone'
      lines.push "RUN mkdir #{ruby_base}"
      lines.push "RUN tar -zxf #{standalone_tarball} -C #{ruby_base} --strip-components=1"
      ruby_bin = "#{ruby_base}/bin"
      lines.push "RUN #{ruby_base}/lib/truffle/post_install_hook.sh" if run_post_install_hook
    when :source
      lines.push 'RUN git clone --depth 1 https://github.com/graalvm/mx.git'
      lines.push 'ENV PATH=$PATH:/test/mx'
      lines.push 'RUN git clone --depth 1 https://github.com/graalvm/graal-jvmci-8.git'

      # Disable compiler warnings as errors, as we may be using a more recent compiler
      lines.push "RUN sed -i 's/WARNINGS_ARE_ERRORS = -Werror/WARNINGS_ARE_ERRORS = /g' graal-jvmci-8/make/linux/makefiles/gcc.make"

      lines.push 'RUN cd graal-jvmci-8 && JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac)))) mx build'
      lines.push "ENV JAVA_HOME=/test/graal-jvmci-8/#{distro.fetch('jdk')}/linux-amd64/product"
      lines.push 'ENV PATH=$JAVA_HOME/bin:$PATH'
      lines.push 'ENV JVMCI_VERSION_CHECK=ignore'
      lines.push 'RUN java -version'
      lines.push 'RUN git clone https://github.com/oracle/graal.git'
      lines.push "RUN git clone --depth 1 --branch #{source_branch} #{truffleruby_repo}"
      lines.push 'RUN cd truffleruby && mx build'
      lines.push 'RUN cd graal/compiler && mx build'
      lines.push "ENV JAVA_OPTS='-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -Djvmci.class.path.append=/test/graal/compiler/mxbuild/dists/jdk1.8/graal.jar'"
      ruby_base = '/test/truffleruby'
      ruby_bin = "#{ruby_base}/bin"
    end

    if rebuild_images
      if [:public, :graalvm].include?(install_method)
        FileUtils.copy native_component, docker_dir
        native_component = File.basename(native_component)
        lines.push "COPY #{native_component} /test/"
        lines.push "RUN #{graalvm_bin}/gu install --file /test/#{native_component} | tee install.log"
        lines.push "RUN #{graalvm_base}/bin/gu rebuild-images polyglot libpolyglot"
      else
        abort "can't rebuild images for a build not from public or from local GraalVM components"
      end
    end

    case manager
    when :none
      lines.push "ENV PATH=#{ruby_bin}:$PATH"

      setup_env = ->(command) do
        command
      end
    when :rbenv
      lines.push 'RUN git clone --depth 1 https://github.com/rbenv/rbenv.git /home/test/.rbenv'
      lines.push 'RUN mkdir /home/test/.rbenv/versions'
      lines.push 'ENV PATH=/home/test/.rbenv/bin:$PATH'
      lines.push 'RUN rbenv --version'

      lines.push "RUN ln -s #{ruby_base} /home/test/.rbenv/versions/truffleruby"
      lines.push 'RUN rbenv versions'

      setup_env = ->(command) do
        "eval \"$(rbenv init -)\" && rbenv shell truffleruby && #{command}"
      end
    when :chruby
      lines.push 'RUN git clone --depth 1 https://github.com/postmodern/chruby.git'
      lines.push 'ENV CRUBY_SH=/test/chruby/share/chruby/chruby.sh'
      lines.push "RUN bash -c 'source $CRUBY_SH && chruby --version'"

      lines.push 'RUN mkdir /home/test/.rubies'
      lines.push "RUN ln -s #{ruby_base} /home/test/.rubies/truffleruby"
      lines.push "RUN bash -c 'source $CRUBY_SH && chruby'"

      setup_env = ->(command) do
        "bash -c 'source $CRUBY_SH && chruby truffleruby && #{command.gsub("'", "'\\\\''")}'"
      end
    when :rvm
      lines.push 'RUN git clone --depth 1 https://github.com/rvm/rvm.git'
      lines.push 'ENV RVM_SCRIPT=/test/rvm/scripts/rvm'
      lines.push "RUN bash -c 'source $RVM_SCRIPT && rvm --version'"

      lines.push "RUN bash -c 'source $RVM_SCRIPT && rvm mount #{ruby_base} -n truffleruby'"
      lines.push "RUN bash -c 'source $RVM_SCRIPT && rvm list'"

      setup_env = ->(command) do
        "bash -c 'source $RVM_SCRIPT && rvm use ext-truffleruby && #{command.gsub("'", "'\\\\''")}'"
      end
    end

    configs = ['']
    configs += ['--jvm'] if [:public, :graalvm].include?(install_method)
    configs += ['--native'] if [:public, :graalvm, :standalone].include?(install_method)

    configs.each do |c|
      lines.push 'RUN ' + setup_env["ruby #{c} --version"]
    end

    if basic_test || full_test
      configs.each do |c|
        lines.push "RUN cp -r #{ruby_base}/lib/gems /test/clean-gems"

        if c == '' && install_method != :source
          gem = 'gem'
        else
          gem = "ruby #{c} -Sgem"
        end

        lines.push 'RUN ' + setup_env["#{gem} install color"]
        lines.push 'RUN ' + setup_env["ruby #{c} -rcolor -e 'raise unless defined?(Color)'"]

        lines.push 'RUN ' + setup_env["#{gem} install oily_png"]
        lines.push 'RUN ' + setup_env["ruby #{c} -roily_png -e 'raise unless defined?(OilyPNG::Color)'"]

        lines.push 'RUN ' + setup_env["#{gem} install unf"]
        lines.push 'RUN ' + setup_env["ruby #{c} -runf -e 'raise unless defined?(UNF)'"]

        lines.push "RUN rm -rf #{ruby_base}/lib/gems"
        lines.push "RUN mv /test/clean-gems #{ruby_base}/lib/gems"
      end
    end

    if full_test
      lines.push "RUN git clone #{truffleruby_repo} truffleruby-tests"
      lines.push "RUN cd truffleruby-tests && git checkout #{test_branch}"
      lines.push 'RUN cp -r truffleruby-tests/spec .'
      lines.push 'RUN cp -r truffleruby-tests/test/truffle/compiler/pe .'
      lines.push 'RUN rm -rf truffleruby-tests'

      configs.each do |c|
        excludes = ['fails', 'slow', 'ci']
        excludes += ['graalvm'] if [:public, :graalvm].include?(install_method)
        excludes += ['aot'] if ['', '--native'].include?(c)

        [':command_line', ':security', ':language', ':core', ':library', ':capi', ':library_cext', ':truffle', ':truffle_capi'].each do |set|
          t_config = c.empty? ? '' : '-T' + c
          t_excludes = excludes.map { |e| '--excl-tag ' + e }.join(' ')
          lines.push 'RUN ' + setup_env["ruby spec/mspec/bin/mspec --config spec/truffle.mspec -t #{ruby_bin}/ruby #{t_config} #{t_excludes} #{set}"]
        end
      end

      configs.each do |c|
        lines.push 'RUN ' + setup_env["ruby #{c} --vm.Dgraal.TruffleCompilationExceptionsAreThrown=true --vm.Dgraal.TruffleIterativePartialEscape=true pe/pe.rb"]
      end
    end

    lines.push 'CMD ' + setup_env['bash']

    lines.join("\n") + "\n"
  end

end

class JT
  include Commands

  def main(args)
    args = args.dup

    if args.empty? or %w[-h -help --help].include? args.first
      help
      exit
    end

    if args.first =~ /^--((?:re)?build)$/
      send $1
      args.shift
    end

    if args.first == '--use' || args.first == '-u'
      args.shift
      @ruby_name = args.shift
    end

    @silent = !!args.delete('--silent')

    commands = Commands.public_instance_methods(false).map(&:to_s)

    command, *rest = args
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
