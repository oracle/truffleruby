#!/usr/bin/env ruby
# encoding: utf-8

# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# A workflow tool for JRuby+Truffle development

# Recommended: function jt { ruby tool/jt.rb "$@"; }

abort "Do not run #{$0} with JRuby+Truffle itself, use MRI or some other Ruby." if RUBY_ENGINE == "truffleruby"

require 'fileutils'
require 'json'
require 'timeout'
require 'yaml'
require 'open3'
require 'rbconfig'
require 'pathname'

JRUBY_DIR = File.expand_path('../..', __FILE__)
M2_REPO = File.expand_path('~/.m2/repository')
SULONG_HOME = ENV['SULONG_HOME']

JDEBUG_PORT = 51819
JDEBUG = "-J-agentlib:jdwp=transport=dt_socket,server=y,address=#{JDEBUG_PORT},suspend=y"
JDEBUG_TEST = "-Dmaven.surefire.debug=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=#{JDEBUG_PORT} -Xnoagent -Djava.compiler=NONE"
JEXCEPTION = "-Xexceptions.print_uncaught_java=true"
METRICS_REPS = 10

VERBOSE = ENV.include? 'V'

uname = `uname`.chomp
MAC = uname == 'Darwin'
LINUX = uname == 'Linux'

SO = MAC ? 'dylib' : 'so'

# Expand GEM_HOME relative to cwd so it cannot be misinterpreted later.
ENV['GEM_HOME'] = File.expand_path(ENV['GEM_HOME']) if ENV['GEM_HOME']

LIBXML_HOME = ENV['LIBXML_HOME'] = ENV['LIBXML_HOME'] || '/usr'
LIBXML_LIB_HOME = ENV['LIBXML_LIB_HOME'] = ENV['LIBXML_LIB_HOME'] || "#{LIBXML_HOME}/lib"
LIBXML_INCLUDE = ENV['LIBXML_INCLUDE'] = ENV['LIBXML_INCLUDE'] || "#{LIBXML_HOME}/include/libxml2"
LIBXML_LIB = ENV['LIBXML_LIB'] = ENV['LIBXML_LIB'] || "#{LIBXML_LIB_HOME}/libxml2.#{SO}"

OPENSSL_HOME = ENV['OPENSSL_HOME'] = ENV['OPENSSL_HOME'] || '/usr'
OPENSSL_LIB_HOME = ENV['OPENSSL_LIB_HOME'] = ENV['OPENSSL_LIB_HOME'] || "#{OPENSSL_HOME}/lib"
OPENSSL_INCLUDE = ENV['OPENSSL_INCLUDE'] = ENV['OPENSSL_INCLUDE'] || "#{OPENSSL_HOME}/include"
OPENSSL_LIB = ENV['OPENSSL_LIB'] = ENV['OPENSSL_LIB'] || "#{OPENSSL_LIB_HOME}/libssl.#{SO}"

# wait for sub-processes to handle the interrupt
trap(:INT) {}

module Utilities

  def self.truffle_version
    File.foreach("#{JRUBY_DIR}/truffle/pom.rb") do |line|
      if /'truffle\.version' => '((?:\d+\.\d+|\h+)(?:-SNAPSHOT)?)'/ =~ line
        break $1
      end
    end
  end

  def self.truffle_release?
    !truffle_version.include?('SNAPSHOT')
  end

  def self.find_graal_javacmd_and_options
    graalvm = ENV['GRAALVM_BIN']
    jvmci = ENV['JVMCI_BIN']
    graal_home = ENV['GRAAL_HOME']

    raise "More than one of GRAALVM_BIN, JVMCI_BIN or GRAAL_HOME defined!" if [graalvm, jvmci, graal_home].compact.count > 1

    if graalvm
      javacmd = File.expand_path(graalvm)
      vm_args = []
      options = []
    elsif jvmci
      javacmd = File.expand_path(jvmci)
      jvmci_graal_home = ENV['JVMCI_GRAAL_HOME']
      raise "Also set JVMCI_GRAAL_HOME if you set JVMCI_BIN" unless jvmci_graal_home
      jvmci_graal_home = File.expand_path(jvmci_graal_home)
      vm_args = [
        '-d64',
        '-XX:+UnlockExperimentalVMOptions',
        '-XX:+EnableJVMCI',
        '--add-exports=java.base/jdk.internal.module=com.oracle.graal.graal_core',
        "--module-path=#{jvmci_graal_home}/../truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar:#{jvmci_graal_home}/mxbuild/modules/com.oracle.graal.graal_core.jar"
      ]
      options = ['--no-bootclasspath']
    elsif graal_home
      graal_home = File.expand_path(graal_home)
      mx = find_mx(graal_home)
      output = `#{mx} -v -p #{graal_home} vm -version 2>&1`.lines.to_a
      command_line = output.select { |line| line.include? '-version' }
      if command_line.size == 1
        command_line = command_line[0]
      else
        $stderr.puts "Error in mx for setting up Graal:"
        $stderr.puts output
        abort
      end
      vm_args = command_line.split
      vm_args.pop # Drop "-version"
      javacmd = vm_args.shift
      options = []
    else
      raise 'set one of GRAALVM_BIN or GRAAL_HOME in order to use Graal'
    end
    [javacmd, vm_args.map { |arg| "-J#{arg}" } + options]
  end

  def self.find_mx(graal_home)
    sibling_mx = File.expand_path("../mx/mx", graal_home)
    sibling_parent_mx = File.expand_path("../../mx/mx", graal_home)
    if File.executable?(sibling_mx)
      sibling_mx
    elsif File.executable?(sibling_parent_mx)
      sibling_parent_mx
    else
      "mx"
    end
  end

  def self.find_graal_js
    jar = ENV['GRAAL_JS_JAR']
    return jar if jar
    raise "couldn't find trufflejs.jar - download GraalVM as described in https://github.com/jruby/jruby/wiki/Downloading-GraalVM and find it in there"
  end

  def self.find_sulong_options
    sulong_home = ENV['SULONG_HOME']
    raise 'set $SULONG_HOME to a built checkout of the Sulong repository' unless sulong_home
    sulong_home = File.expand_path(sulong_home)
    mx = find_mx(sulong_home)
    output = `#{mx} -v -p #{sulong_home} su-run 2>&1`.lines.to_a
    command_line = output.select { |line| line.include? 'DynamicNativeLibraryPath' }
    if command_line.size == 1
      command_line = command_line[0]
    else
      $stderr.puts "Error in mx for setting up Sulong:"
      $stderr.puts output
      abort
    end
    sulong_options = command_line.split
    options = []
    until sulong_options.empty?
      option = sulong_options.shift
      if option.start_with?('-') && option != '-d64' && !option.start_with?('-Dgraal.')
        options.push "-J#{option}"
        if option == '-cp'
          cp = sulong_options.shift.split(File::PATH_SEPARATOR)
          cp.delete_if { |p| p.end_with?('truffle-api.jar') }
          options.push cp.join(File::PATH_SEPARATOR)
        end
      end
    end
    options
  end

  def self.find_sl
    jar = ENV['SL_JAR']
    return jar if jar
    raise "couldn't find truffle-sl.jar - build Truffle and find it in there"
  end

  def self.mx?
    mx_jar = "#{JRUBY_DIR}/mxbuild/dists/truffleruby.jar"
    mvn_jar = "#{JRUBY_DIR}/lib/truffleruby.jar"
    mx_time = File.exist?(mx_jar) ? File.mtime(mx_jar) : Time.at(0)
    mvn_time = File.exist?(mvn_jar) ? File.mtime(mvn_jar) : Time.at(0)
    mx_time > mvn_time
  end

  def self.find_ruby
    if ENV["RUBY_BIN"]
      ENV["RUBY_BIN"]
    else
      version = `ruby -e 'print RUBY_VERSION' 2>/dev/null`
      if version.start_with?("2.")
        "ruby"
      else
        find_launcher
      end
    end
  end

  def self.find_launcher
    if ENV['RUBY_BIN']
      ENV['RUBY_BIN']
    else
      "#{JRUBY_DIR}/bin/truffleruby"
    end
  end

  def self.find_repo(name)
    [JRUBY_DIR, "#{JRUBY_DIR}/.."].each do |dir|
      found = Dir.glob("#{dir}/#{name}*").sort.first
      return File.expand_path(found) if found
    end
    raise "Can't find the #{name} repo - clone it into the repository directory or its parent"
  end

  def self.find_benchmark(benchmark)
    if File.exist?(benchmark)
      benchmark
    else
      File.join(JRUBY_DIR, 'bench', benchmark)
    end
  end

  def self.find_gem(name)
    ["#{JRUBY_DIR}/lib/ruby/gems/shared/gems"].each do |dir|
      found = Dir.glob("#{dir}/#{name}*").sort.first
      return File.expand_path(found) if found
    end

    [JRUBY_DIR, "#{JRUBY_DIR}/.."].each do |dir|
      found = Dir.glob("#{dir}/#{name}").sort.first
      return File.expand_path(found) if found
    end
    raise "Can't find the #{name} gem - gem install it in this repository, or put it in the repository directory or its parent"
  end

  def self.git_branch
    @git_branch ||= `GIT_DIR="#{JRUBY_DIR}/.git" git rev-parse --abbrev-ref HEAD`.strip
  end

  def self.igv_running?
    `ps ax`.include?('idealgraphvisualizer')
  end

  def self.ensure_igv_running
    abort "I can't see IGV running - go to your checkout of Graal and run 'mx igv' in a separate shell, then run this command again" unless igv_running?
  end

  def self.jruby_version
    File.read("#{JRUBY_DIR}/VERSION").strip
  end

  def self.human_size(bytes)
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

  def self.log(tty_message, full_message)
    if STDERR.tty?
      STDERR.print tty_message unless tty_message.nil?
    else
      STDERR.print full_message unless full_message.nil?
    end
  end

end

module ShellUtils
  private

  def system_timeout(timeout, *args)
    begin
      pid = Process.spawn(*args)
    rescue SystemCallError
      return nil
    end

    begin
      Timeout.timeout timeout do
        Process.waitpid pid
        $?.exitstatus == 0
      end
    rescue Timeout::Error
      Process.kill('TERM', pid)
      nil
    end
  end

  def raw_sh(*args)
    options = args.last.is_a?(Hash) ? args.last : {}
    continue_on_failure = options.delete :continue_on_failure
    use_exec = options.delete :use_exec
    timeout = options.delete :timeout
    capture = options.delete :capture

    unless options.delete :no_print_cmd
      STDERR.puts "$ #{printable_cmd(args)}"
    end

    if use_exec
      result = exec(*args)
    elsif timeout
      result = system_timeout(timeout, *args)
    elsif capture
      out, err, status = Open3.capture3(*args)
      result = status.exitstatus == 0
    else
      result = system(*args)
    end

    if result
      if out && err
        [out, err]
      else
        true
      end
    elsif continue_on_failure
      false
    else
      status = $? unless capture
      $stderr.puts "FAILED (#{status}): #{printable_cmd(args)}"

      if capture
        $stderr.puts out
        $stderr.puts err
      end

      if status && status.exitstatus
        exit status.exitstatus
      else
        exit 1
      end
    end
  end

  def printable_cmd(args)
    env = {}
    if Hash === args.first
      env, *args = args
    end
    if Hash === args.last && args.last.empty?
      *args, options = args
    end
    env = env.map { |k,v| "#{k}=#{shellescape(v)}" }.join(' ')
    args = args.map { |a| shellescape(a) }.join(' ')
    env.empty? ? args : "#{env} #{args}"
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
    string.gsub(/\$([A-Z_]+)/) {
      var = $1
      abort "You need to set $#{var}" unless env[var]
      env[var]
    }
  end

  def sh(*args)
    Dir.chdir(JRUBY_DIR) do
      raw_sh(*args)
    end
  end

  def mvn(*args)
    if args.first.is_a? Hash
      options = [args.shift]
    else
      options = []
    end

    args = ['-q', *args] unless VERBOSE

    sh *options, 'mvn', *args
  end

  def mx(dir, *args)
    command = ['mx', '-p', dir]
    command.push *args
    sh *command
  end

  def mx_sulong(*args)
    abort "You need to set SULONG_HOME" unless SULONG_HOME
    mx SULONG_HOME, *args
  end

  def clang(*args)
    clang = ENV['JT_CLANG'] || 'clang'
    sh clang, *args
  end

  def llvm_opt(*args)
    opt = ENV['JT_OPT'] || 'opt'
    sh opt, *args
  end

  def sulong_run(*args)
    mx_sulong 'su-run', *args
  end

  def sulong_link(*args)
    mx_sulong 'su-link', *args
  end

  def mspec(command, *args)
    env_vars = {}
    if command.is_a?(Hash)
      env_vars = command
      command, *args = args
    end

    sh env_vars, Utilities.find_ruby, 'spec/mspec/bin/mspec', command, '--config', 'spec/truffle.mspec', *args
  end

  def newer?(input, output)
    return true unless File.exist? output
    File.mtime(input) > File.mtime(output)
  end
end

module Commands
  include ShellUtils

  def help
    puts <<-TXT.gsub(/^#{' '*6}/, '')
      jt build [options]                             build
      jt build_stats [--json] <attribute>            prints attribute's value from build process (e.g., binary size)
      jt rebuild [options]                           clean and build
          cexts [--no-openssl]                       build the cext backend (set SULONG_HOME)
          parser                                     build the parser
          options                                    build the options
      jt clean                                       clean
      jt dis <file>                                  finds the bc file in the project, disassembles, and returns new filename
      jt rebuild                                     clean and build
      jt run [options] args...                       run JRuby with args
          --graal         use Graal (set either GRAALVM_BIN or GRAAL_HOME)
              --stress    stress the compiler (compile immediately, foreground compilation, compilation exceptions are fatal)
          --js            add Graal.js to the classpath (set GRAAL_JS_JAR)
          --sulong
          --asm           show assembly (implies --graal)
          --server        run an instrumentation server on port 8080
          --igv           make sure IGV is running and dump Graal graphs after partial escape (implies --graal)
              --full      show all phases, not just up to the Truffle partial escape
          --infopoints    show source location for each node in IGV
          --fg            disable background compilation
          --trace         show compilation information on stdout
          --jdebug        run a JDWP debug server on #{JDEBUG_PORT}
          --jexception[s] print java exceptions
          --exec          use exec rather than system
          --no-print-cmd  don\'t print the command
      jt e 14 + 2                                    evaluate an expression
      jt puts 14 + 2                                 evaluate and print an expression
      jt cextc directory clang-args                  compile the C extension in directory, with optional extra clang arguments
      jt test                                        run all mri tests, specs and integration tests (set SULONG_HOME)
      jt test tck [--jdebug]                         run the Truffle Compatibility Kit tests
      jt test mri                                    run mri tests
          --openssl       runs openssl tests         use with --sulong
          --aot           use AOT TruffleRuby image (set AOT_BIN)
          --graal         use Graal (set either GRAALVM_BIN, JVMCI_BIN or GRAAL_HOME)
      jt test specs                                  run all specs
      jt test specs fast                             run all specs except sub-processes, GC, sleep, ...
      jt test spec/ruby/language                     run specs in this directory
      jt test spec/ruby/language/while_spec.rb       run specs in this file
      jt test compiler                               run compiler tests (uses the same logic as --graal to find Graal)
      jt test integration                            runs all integration tests
      jt test integration [TESTS]                    runs the given integration tests
      jt test bundle                                 tests using bundler
      jt test gems                                   tests using gems
      jt test ecosystem [TESTS]                      tests using the wider ecosystem such as bundler, Rails, etc
      jt test cexts [--no-libxml --no-openssl]       run C extension tests
                                                         (implies --sulong, set SULONG_HOME to a built checkout of Sulong, and set GEM_HOME)
      jt test report :language                       build a report on language specs
                     :core                               (results go into test/target/mspec-html-report)
                     :library
      jt rubocop [rubocop options]                   run rubocop rules (using ruby available in the environment)
      jt tag spec/ruby/language                      tag failing specs in this directory
      jt tag spec/ruby/language/while_spec.rb        tag failing specs in this file
      jt tag all spec/ruby/language                  tag all specs in this file, without running them
      jt untag spec/ruby/language                    untag passing specs in this directory
      jt untag spec/ruby/language/while_spec.rb      untag passing specs in this file
      jt mspec ...                                   run MSpec with the JRuby+Truffle configuration and custom arguments
      jt metrics alloc [--json] ...                  how much memory is allocated running a program (use -Xclassic to test normal JRuby on this metric and others)
      jt metrics instructions ...                    how many CPU instructions are used to run a program
      jt metrics minheap ...                         what is the smallest heap you can use to run an application
      jt metrics time ...                            how long does it take to run a command, broken down into different phases
      jt benchmark [options] args...                 run benchmark-interface (implies --graal)
          --no-graal              don't imply --graal
          --sulong
          JT_BENCHMARK_RUBY=ruby  benchmark some other Ruby, like MRI
          note that to run most MRI benchmarks, you should translate them first with normal Ruby and cache the result, such as
              benchmark bench/mri/bm_vm1_not.rb --cache
              jt benchmark bench/mri/bm_vm1_not.rb --use-cache
      jt where repos ...                            find these repositories
      jt next                                       tell you what to work on next (give you a random core library spec)

      you can also put build or rebuild in front of any command

      recognised environment variables:

        RUBY_BIN                                     The JRuby+Truffle executable to use (normally just bin/truffleruby)
        GRAALVM_BIN                                  GraalVM executable (java command)
        GRAAL_HOME                                   Directory where there is a built checkout of the Graal compiler (make sure mx is on your path)
        JVMCI_BIN                                    JVMCI-enabled (so JDK 9 EA build) java command (aslo set JVMCI_GRAAL_HOME)
        JVMCI_GRAAL_HOME                             Like GRAAL_HOME, but only used for the JARs to run with JVMCI_BIN
        SULONG_HOME                                  The Sulong source repository, if you want to run cexts
        GRAAL_JS_JAR                                 The location of trufflejs.jar
        SL_JAR                                       The location of truffle-sl.jar
        LIBXML_HOME, LIBXML_INCLUDE, LIBXML_LIB      The location of libxml2 (the directory containing include etc), and the direct include directory and library file
        OPENSSL_HOME, OPENSSL_INCLUDE, OPENSSL_LIB               ... OpenSSL ...
        JT_CLANG, JT_OPT                             LLVM binaries to use
        AOT_BIN                                      TruffleRuby/SVM executable
    TXT
  end

  def build(*options)
    project = options.first
    case project
    when 'cexts'
      no_openssl = options.delete('--no-openssl')
      build_ruby_su
      unless no_openssl
        cextc "#{JRUBY_DIR}/truffleruby/src/main/c/openssl"
      end
    when 'parser'
      jay = Utilities.find_repo('jay')
      ENV['PATH'] = "#{jay}/src:#{ENV['PATH']}"
      sh 'bash', 'tool/generate_parser'
      yytables = 'truffleruby/src/main/java/org/truffleruby/parser/parser/YyTables.java'
      File.write(yytables, File.read(yytables).gsub('package org.jruby.parser;', 'package org.truffleruby.parser.parser;'))
    when 'options'
      sh 'tool/generate-options.rb'
    when nil
      mvn 'package'
    else
      raise ArgumentError, project
    end
  end

  def clean
    mx(JRUBY_DIR, 'clean') if Utilities.mx?
    mvn 'clean'
  end

  def dis(file)
    dis = `which llvm-dis-3.8 llvm-dis 2>/dev/null`.lines.first.chomp
    file = `find #{JRUBY_DIR} -name "#{file}"`.lines.first.chomp
    raise ArgumentError, "file not found:`#{file}`" if file.empty?
    sh dis, file
    puts Pathname(file).sub_ext('.ll')
  end

  def rebuild
    clean
    build
  end

  def run(*args)
    env_vars = args.first.is_a?(Hash) ? args.shift : {}
    options = args.last.is_a?(Hash) ? args.pop : {}

    jruby_args = []

    {
      '--asm' => '--graal',
      '--stress' => '--graal',
      '--igv' => '--graal',
      '--trace' => '--graal',
    }.each_pair do |arg, dep|
      args.unshift dep if args.include?(arg)
    end

    unless args.delete('--no-core-load-path')
      jruby_args << "-Xcore.load_path=#{JRUBY_DIR}/truffleruby/src/main/ruby"
    end

    if args.delete('--graal')
      if ENV["RUBY_BIN"]
        # Assume that Graal is automatically set up if RUBY_BIN is set.
        # This will also warn if it's not.
      else
        javacmd, javacmd_options = Utilities.find_graal_javacmd_and_options
        env_vars["JAVACMD"] = javacmd
        jruby_args.push(*javacmd_options)
      end
    else
      jruby_args << '-Xgraal.warn_unless=false'
    end

    if args.delete('--stress')
      jruby_args << '-J-Dgraal.TruffleCompileImmediately=true'
      jruby_args << '-J-Dgraal.TruffleBackgroundCompilation=false'
      jruby_args << '-J-Dgraal.TruffleCompilationExceptionsAreFatal=true'
    end

    if args.delete('--sulong')
      jruby_args.push *Utilities.find_sulong_options
    end

    if args.delete('--js')
      jruby_args << '-J-cp'
      jruby_args << Utilities.find_graal_js
    end

    if args.delete('--asm')
      jruby_args += %w[-J-XX:+UnlockDiagnosticVMOptions -J-XX:CompileCommand=print,*::callRoot]
    end

    if args.delete('--jdebug')
      jruby_args << JDEBUG
    end

    if args.delete('--jexception') || args.delete('--jexceptions')
      jruby_args << JEXCEPTION
    end

    if args.delete('--server')
      jruby_args += %w[-Xinstrumentation_server_port=8080]
    end

    if args.delete('--profile')
      v = Utilities.truffle_version
      jruby_args << "-J-Xbootclasspath/a:#{M2_REPO}/com/oracle/truffle/truffle-debug/#{v}/truffle-debug-#{v}.jar"
      jruby_args << "-J-Dtruffle.profiling.enabled=true"
    end

    if args.delete('--igv')
      Utilities.ensure_igv_running
      if args.delete('--full')
        jruby_args << "-J-Dgraal.Dump=Truffle"
      else
        jruby_args << "-J-Dgraal.Dump=TruffleTree,PartialEscape"
      end
      jruby_args << "-J-Dgraal.PrintBackendCFG=false"
    end

    if args.delete('--infopoints')
      jruby_args << "-J-XX:+UnlockDiagnosticVMOptions" << "-J-XX:+DebugNonSafepoints"
      jruby_args << "-J-Dgraal.TruffleEnableInfopoints=true"
    end

    if args.delete('--fg')
      jruby_args << "-J-Dgraal.TruffleBackgroundCompilation=false"
    end

    if args.delete('--trace')
      jruby_args << "-J-Dgraal.TraceTruffleCompilation=true"
    end

    if args.delete('--no-print-cmd')
      options[:no_print_cmd] = true
    end

    if args.delete('--exec')
      options[:use_exec] = true
    end

    ruby_bin = if args.delete('--aot')
                 verify_aot_bin!
                 ENV['AOT_BIN']
               else
                 Utilities.find_launcher
               end

    raw_sh env_vars, ruby_bin, *jruby_args, *args, options
  end

  # Same as #run but uses exec()
  def ruby(*args)
    run(*args, '--exec')
  end

  def e(*args)
    run '-e', args.join(' ')
  end

  def command_puts(*args)
    e 'puts begin', *args, 'end'
  end

  def command_p(*args)
    e 'p begin', *args, 'end'
  end

  def build_ruby_su(cext_dir=nil)
    abort "You need to set SULONG_HOME" unless SULONG_HOME

    # Ensure ruby.su is up-to-date
    ruby_cext_api = "#{JRUBY_DIR}/truffleruby/src/main/c/cext"
    ruby_c = "#{JRUBY_DIR}/truffleruby/src/main/c/cext/ruby.c"
    ruby_h = "#{JRUBY_DIR}/lib/cext/ruby.h"
    ruby_su = "#{JRUBY_DIR}/lib/cext/ruby.su"
    if cext_dir != ruby_cext_api and (newer?(ruby_h, ruby_su) or newer?(ruby_c, ruby_su))
      puts "Compiling outdated ruby.su"
      cextc ruby_cext_api
    end
  end
  private :build_ruby_su

  def cextc(cext_dir, test_gem=false, *clang_opts)
    build_ruby_su(cext_dir)

    is_ruby = cext_dir == "#{JRUBY_DIR}/truffleruby/src/main/c/cext"
    gem_name = if is_ruby
                 "ruby"
               else
                 File.basename(cext_dir)
               end

    gem_dir = if cext_dir.start_with?("#{JRUBY_DIR}/truffleruby/src/main/c")
                cext_dir
              elsif test_gem
                "#{JRUBY_DIR}/test/truffle/cexts/#{gem_name}/ext/#{gem_name}/"
              elsif cext_dir.start_with?(JRUBY_DIR)
                Dir.glob(ENV['GEM_HOME'] + "/gems/#{gem_name}*/")[0] + "ext/#{gem_name}/"
              else
                cext_dir + "/ext/#{gem_name}/"
              end
    copy_target = if is_ruby
                    "#{JRUBY_DIR}/lib/cext/ruby.su"
                  elsif cext_dir == "#{JRUBY_DIR}/truffleruby/src/main/c/openssl"
                    "#{JRUBY_DIR}/lib/mri/openssl.su"
                  else
                    "#{JRUBY_DIR}/test/truffle/cexts/#{gem_name}/lib/#{gem_name}/#{gem_name}.su"
                  end

    Dir.chdir(gem_dir) do
      STDERR.puts "in #{gem_dir} ..."
      run("extconf.rb")
      raw_sh("make")
      FileUtils.copy_file("#{gem_name}.su", copy_target)
    end
  end

  def test(*args)
    path, *rest = args

    case path
    when nil
      test_tck
      test_specs('run')
      test_mri
      test_integration
      test_gems
      test_ecosystem 'HAS_REDIS' => 'true'
      test_compiler
      test_cexts
    when 'bundle' then test_bundle(*rest)
    when 'compiler' then test_compiler(*rest)
    when 'cexts' then test_cexts(*rest)
    when 'cexts_and_openssl' then
      test('cexts')
      test('specs', '--sulong', ':capi')
      test('specs', '--sulong', '-T-Xpatching=false', ':openssl')
      test('mri', '--openssl', '--sulong')
    when 'report' then test_report(*rest)
    when 'integration' then test_integration({}, *rest)
    when 'gems' then test_gems({}, *rest)
    when 'ecosystem' then test_ecosystem({}, *rest)
    when 'specs' then test_specs('run', *rest)
    when 'tck' then
      args = []
      if rest.include? '--jdebug'
        args << JDEBUG_TEST
      end
      test_tck *args
    when 'mri' then test_mri(*rest)
    else
      if File.expand_path(path).start_with?("#{JRUBY_DIR}/test")
        test_mri(*args)
      else
        test_specs('run', *args)
      end
    end
  end

  def test_mri(*args)
    if args.delete('--openssl')
      include_pattern = "#{JRUBY_DIR}/test/mri/tests/openssl/test_*.rb"
      exclude_file = "#{JRUBY_DIR}/test/mri/openssl.exclude"
    elsif args.all? { |a| a.start_with?('-') }
      include_pattern = "#{JRUBY_DIR}/test/mri/tests/**/test_*.rb"
      exclude_file = "#{JRUBY_DIR}/test/mri/standard.exclude"
    else
      args, files_to_run = args.partition { |a| a.start_with?('-') }
    end

    unless files_to_run
      prefix = "#{JRUBY_DIR}/test/mri/tests/"

      include_files = Dir.glob(include_pattern).map { |f|
        raise unless f.start_with?(prefix)
        f[prefix.size..-1]
      }

      exclude_files = File.readlines(exclude_file).map { |l| l.gsub(/#.*/, '').strip }

      files_to_run = (include_files - exclude_files)
    end

    run_mri_tests(args, files_to_run)
  end
  private :test_mri

  def run_mri_tests(extra_args, test_files, run_options = {})
    truffle_args = %w[-J-Xmx2G -J-ea -J-esa --jexceptions]

    env_vars = {
      "EXCLUDES" => "test/mri/excludes",
      "RUBYOPT" => '--disable-gems'
    }

    command = %w[test/mri/tests/runner.rb -v --color=never --tty=no -q]
    run(env_vars, *truffle_args, *extra_args, *command, *test_files, run_options)
  end
  private :run_mri_tests

  def retag(*args)
    options, test_files = args.partition { |a| a.start_with?('-') }
    raise unless test_files.size == 1
    test_file = test_files[0]
    test_classes = File.read(test_file).scan(/class ([\w:]+) < .+TestCase/)
    test_classes.each do |test_class,|
      prefix = "test/mri/excludes/#{test_class.gsub('::', '/')}"
      FileUtils::Verbose.rm_f "#{prefix}.rb"
      FileUtils::Verbose.rm_rf prefix
    end

    puts "1. Tagging tests"
    output_file = "mri_tests.txt"
    run_mri_tests(options, test_file, out: output_file, continue_on_failure: true)

    puts "2. Parsing errors"
    sh "ruby", "tool/parse_mri_errors.rb", output_file

    puts "3. Verifying tests pass"
    run_mri_tests(options, test_file)
  end

  def test_compiler(*args)
    env = {}

    env['TRUFFLERUBYOPT'] = '-Xexceptions.print_java=true'

    if ENV['GRAAL_JS_JAR']
      env['JAVA_OPTS'] = "-cp #{Utilities.find_graal_js}"
    end

    Dir["#{JRUBY_DIR}/test/truffle/compiler/*.sh"].sort.each do |test_script|
      if args.empty? or args.include?(File.basename(test_script, ".*"))
        sh env, test_script
      end
    end
  end
  private :test_compiler

  def test_cexts(*args)
    no_libxml = args.delete('--no-libxml')
    no_openssl = args.delete('--no-openssl')

    # Test tools

    sh RbConfig.ruby, 'test/truffle/cexts/test-preprocess.rb'

    # Test that we can compile and run some basic C code that uses libxml and openssl

    unless no_libxml
      clang '-c', '-emit-llvm', "-I#{LIBXML_INCLUDE}", 'test/truffle/cexts/xml/main.c', '-o', 'test/truffle/cexts/xml/main.bc'
      out, _ = sulong_run("-l#{LIBXML_LIB}", 'test/truffle/cexts/xml/main.bc', {capture: true})
      raise out.inspect unless out == "7\n"
    end

    unless no_openssl
      clang '-c', '-emit-llvm', "-I#{OPENSSL_INCLUDE}", 'test/truffle/cexts/xopenssl/main.c', '-o', 'test/truffle/cexts/xopenssl/main.bc'
      out, _ = sulong_run("-l#{OPENSSL_LIB}", 'test/truffle/cexts/xopenssl/main.bc', {capture: true})
      raise out.inspect unless out == "5d41402abc4b2a76b9719d911017c592\n"
    end

    # Test that we can run those same test when they're build as a .su and we load the code and libraries from that

    unless no_libxml
      sulong_link '-o', 'test/truffle/cexts/xml/main.su', '-l', "#{LIBXML_LIB}", 'test/truffle/cexts/xml/main.bc'
      out, _ = sulong_run('test/truffle/cexts/xml/main.su', {capture: true})
      raise out.inspect unless out == "7\n"
    end

    unless no_openssl
      sulong_link '-o', 'test/truffle/cexts/xopenssl/main.su', '-l', "#{OPENSSL_LIB}", 'test/truffle/cexts/xopenssl/main.bc'
      out, _ = sulong_run('test/truffle/cexts/xopenssl/main.su', {capture: true})
      raise out.inspect unless out == "5d41402abc4b2a76b9719d911017c592\n"
    end

    # Test that we can compile and run some very basic C extensions

    begin
      output_file = 'cext-output.txt'
      ['minimum', 'method', 'module', 'globals', 'xml', 'xopenssl'].each do |gem_name|
        next if gem_name == 'xml' && no_libxml
        next if gem_name == 'xopenssl' && no_openssl
        dir = "#{JRUBY_DIR}/test/truffle/cexts/#{gem_name}"
        cextc dir, true
        name = File.basename(dir)
        next if gem_name == 'globals' # globals is excluded just for running
        run '--sulong', "-I#{dir}/lib", "#{dir}/bin/#{name}", :out => output_file
        unless File.read(output_file) == File.read("#{dir}/expected.txt")
          abort "c extension #{dir} didn't work as expected"
        end
      end
    ensure
      File.delete output_file rescue nil
    end

    # Test that we can compile and run some real C extensions

    if ENV['GEM_HOME']
      tests = [
          ['oily_png', ['chunky_png-1.3.6', 'oily_png-1.2.0'], ['oily_png']],
          ['psd_native', ['chunky_png-1.3.6', 'oily_png-1.2.0', 'bindata-2.3.1', 'hashie-3.4.4', 'psd-enginedata-1.1.1', 'psd-2.1.2', 'psd_native-1.1.3'], ['oily_png', 'psd_native']],
          ['nokogiri', [], ['nokogiri']]
      ]

      tests.each do |gem_name, dependencies, libs, gem_root|
        next if gem_name == 'nokogiri' # nokogiri totally excluded
        next if gem_name == 'nokogiri' && no_libxml
        gem_root = "#{JRUBY_DIR}/test/truffle/cexts/#{gem_name}"
        cextc gem_root, false, '-Werror=implicit-function-declaration'

        next if gem_name == 'psd_native' # psd_native is excluded just for running
        run '--sulong',
          *dependencies.map { |d| "-I#{ENV['GEM_HOME']}/gems/#{d}/lib" },
          *libs.map { |l| "-I#{JRUBY_DIR}/test/truffle/cexts/#{l}/lib" },
          "#{JRUBY_DIR}/test/truffle/cexts/#{gem_name}/test.rb", gem_root
      end
    end
  end
  private :test_cexts

  def test_report(component)
    test 'specs', '--truffle-formatter', component
    sh 'ant', '-f', 'spec/buildTestReports.xml'
  end
  private :test_report

  def check_test_port
    lsof = `lsof -i :14873`
    unless lsof.empty?
      STDERR.puts 'Someone is already listening on port 14873 - our tests can\'t run'
      STDERR.puts lsof
      exit 1
    end
  end

  def test_integration(env={}, *args)
    env = env.dup

    classpath = []

    if ENV['GRAAL_JS_JAR']
      classpath << Utilities.find_graal_js
    end

    if ENV['SL_JAR']
      classpath << Utilities.find_sl
    end

    unless classpath.empty?
      env['JAVA_OPTS'] = "-cp #{classpath.join(':')}"
    end

    tests_path             = "#{JRUBY_DIR}/test/truffle/integration"
    single_test            = !args.empty?
    test_names             = single_test ? '{' + args.join(',') + '}' : '*'

    Dir["#{tests_path}/#{test_names}.sh"].sort.each do |test_script|
      check_test_port
      sh env, test_script
    end
  end
  private :test_integration

  def test_gems(env={}, *args)
    env = env.dup

    if ENV['GRAAL_JS_JAR']
      env['JAVA_OPTS'] = "-cp #{Utilities.find_graal_js}"
    end

    tests_path             = "#{JRUBY_DIR}/test/truffle/gems"
    single_test            = !args.empty?
    test_names             = single_test ? '{' + args.join(',') + '}' : '*'

    Dir["#{tests_path}/#{test_names}.sh"].sort.each do |test_script|
      next if test_script.end_with?('/install-gems.sh')
      check_test_port
      sh env, test_script
    end
  end
  private :test_gems

  def test_ecosystem(env={}, *args)
    tests_path             = "#{JRUBY_DIR}/test/truffle/ecosystem"
    single_test            = !args.empty?
    test_names             = single_test ? '{' + args.join(',') + '}' : '*'

    success = Dir["#{tests_path}/#{test_names}.sh"].sort.all? do |test_script|
      sh env, test_script, continue_on_failure: true
    end
    exit success ? 0 : 1
  end
  private :test_ecosystem

  def test_bundle(*args)
    if RbConfig::CONFIG['host_os'] =~ /sunos|solaris/i
      # TODO (pitr-ch 08-May-2017): fix workaround using tar, it's broken on Solaris "tar: C: unknown function modifier"
      puts 'skipping on Solaris'
      return
    end

    require 'tmpdir'

    gems = [{ name:   'algebrick',
              url:    'https://github.com/pitr-ch/algebrick.git',
              commit: '89cf71984964ce9cbe6a1f4fb5155144ac56d057' }]

    gems.each do |gem|
      gem_name = gem.fetch(:name)
      temp_dir = Dir.mktmpdir(gem_name)

      begin
        gem_home = File.join(temp_dir, 'gem_home')

        FileUtils.mkpath(gem_home)
        gem_home = File.realpath gem_home # remove symlinks
        puts "Using temporary GEM_HOME:#{gem_home}"

        Dir.chdir(temp_dir) do
          puts "Cloning gem #{gem_name} into temp directory: #{temp_dir}"
          raw_sh('git', 'clone', gem.fetch(:url))
        end

        Dir.chdir(gem_checkout = File.join(temp_dir, gem_name)) do
          raw_sh('git', 'checkout', gem.fetch(:commit)) if gem.key?(:commit)

          environment = { 'GEM_HOME' => gem_home,
                          # add bin from gem_home to PATH
                          'PATH'     => [File.join(gem_home, 'bin'), ENV['PATH']].join(File::PATH_SEPARATOR) }

          run(environment, '-S', 'gem', 'install', 'bundler', '-v', '1.14.6', '--backtrace')
          run(environment, '-S', 'bundle', 'install')
          run(environment, '-S', 'bundle', 'exec', 'rake')
        end
      ensure
        FileUtils.remove_entry temp_dir
      end
    end
  end

  def mspec(*args)
    super(*args)
  end

  def test_specs(command, *args)
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

    if args.delete('--aot')
      verify_aot_bin!

      options += %w[--excl-tag graalvm]
      options << '-t' << ENV['AOT_BIN']
      options << '-T-XX:OldGenerationSize=2G'
      options << "-T-Xhome=#{JRUBY_DIR}"
    end

    if args.delete('--graal')
      javacmd, javacmd_options = Utilities.find_graal_javacmd_and_options
      env_vars["JAVACMD"] = javacmd
      options.concat javacmd_options.map { |o| "-T#{o}" }
    end

    if args.delete('--sulong')
      options.push *Utilities.find_sulong_options.map { |o| "-T#{o}" }
    end

    if args.delete('--jdebug')
      options << "-T#{JDEBUG}"
    end

    if args.delete('--jexception') || args.delete('--jexceptions')
      options << "-T#{JEXCEPTION}"
    end

    if args.delete('--truffle-formatter')
      options += %w[--format spec/truffle_formatter.rb]
    end

    if ENV['CI']
      # Need lots of output to keep Travis happy
      options += %w[--format specdoc]
    end

    mspec env_vars, command, *options, *args
  end
  private :test_specs

  def test_tck(*args)
    if Utilities.mx?
      raw_sh 'mx', 'rubytck'
    else
      mvn *args, '-Ptck'
    end
  end
  private :test_tck

  def tag(path, *args)
    return tag_all(*args) if path == 'all'
    test_specs('tag', path, *args)
  end

  # Add tags to all given examples without running them. Useful to avoid file exclusions.
  def tag_all(*args)
    test_specs('tag_all', *args)
  end
  private :tag_all

  def untag(path, *args)
    puts
    puts "WARNING: untag is currently not very reliable - run `jt test #{[path,*args] * ' '}` after and manually annotate any new failures"
    puts
    test_specs('untag', path, *args)
  end

  def build_stats(attribute, *args)
    use_json = args.delete '--json'

    value = case attribute
      when 'binary-size'
        build_stats_aot_binary_size(*args)
      when 'build-time'
        build_stats_aot_build_time(*args)
      when 'runtime-compilable-methods'
        build_stats_aot_runtime_compilable_methods(*args)
      else
        raise ArgumentError, attribute
      end

    if use_json
      puts JSON.generate({ attribute => value })
    else
      puts "#{attribute}: #{value}"
    end
  end

  def build_stats_aot_binary_size(*args)
    if File.exist?(ENV['AOT_BIN'].to_s)
      File.size(ENV['AOT_BIN']) / 1024.0 / 1024.0
    else
      -1
    end
  end

  def build_stats_aot_build_time(*args)
    if File.exist?('aot-build.log')
      log = File.read('aot-build.log')
      log =~ /\[total\]: (?<build_time>.+) ms/m
      Float($~[:build_time].gsub(',', '')) / 1000.0
    else
      -1
    end
  end

  def build_stats_aot_runtime_compilable_methods(*args)
    if File.exist?('aot-build.log')
      log = File.read('aot-build.log')
      log =~ /(?<method_count>\d+) method\(s\) included for runtime compilation/m
      Integer($~[:method_count])
    else
      -1
    end
  end

  def metrics(command, *args)
    trap(:INT) { puts; exit }
    args = args.dup
    case command
    when 'alloc'
      metrics_alloc *args
    when 'minheap'
      metrics_minheap *args
    when 'instructions'
      metrics_aot_instructions *args
    when 'time'
      metrics_time *args
    else
      raise ArgumentError, command
    end
  end

  def metrics_alloc(*args)
    use_json = args.delete '--json'
    samples = []
    METRICS_REPS.times do
      Utilities.log '.', "sampling\n"
      out, err = run '-J-Dtruffleruby.metrics.memory_used_on_exit=true', '-J-verbose:gc', *args, {capture: true, no_print_cmd: true}
      samples.push memory_allocated(out+err)
    end
    Utilities.log "\n", nil
    range = samples.max - samples.min
    error = range / 2
    median = samples.min + error
    human_readable = "#{Utilities.human_size(median)} ± #{Utilities.human_size(error)}"
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

  def memory_allocated(trace)
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

  def metrics_minheap(*args)
    use_json = args.delete '--json'
    heap = 10
    Utilities.log '>', "Trying #{heap} MB\n"
    until can_run_in_heap(heap, *args)
      heap += 10
      Utilities.log '>', "Trying #{heap} MB\n"
    end
    heap -= 9
    heap = 1 if heap == 0
    successful = 0
    loop do
      if successful > 0
        Utilities.log '?', "Verifying #{heap} MB\n"
      else
        Utilities.log '+', "Trying #{heap} MB\n"
      end
      if can_run_in_heap(heap, *args)
        successful += 1
        break if successful == METRICS_REPS
      else
        heap += 1
        successful = 0
      end
    end
    Utilities.log "\n", nil
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

  def can_run_in_heap(heap, *command)
    run("-J-Xmx#{heap}M", *command, {err: '/dev/null', out: '/dev/null', no_print_cmd: true, continue_on_failure: true, timeout: 60})
  end

  def metrics_aot_instructions(*args)
    verify_aot_bin!

    use_json = args.delete '--json'

    out, err = raw_sh('perf', 'stat', '-e', 'instructions', '--', ENV['AOT_BIN'], *args, {capture: true, no_print_cmd: true})

    err =~ /(?<instruction_count>[\d,]+)\s+instructions/m
    instruction_count = $~[:instruction_count].gsub(',', '')

    Utilities.log "\n", nil
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

  def metrics_time(*args)
    use_json = args.delete '--json'
    samples = []
    METRICS_REPS.times do
      Utilities.log '.', "sampling\n"
      start = Time.now
      out, err = run '-J-Dtruffleruby.metrics.time=true', *args, {capture: true, no_print_cmd: true}
      finish = Time.now
      samples.push get_times(err, finish - start)
    end
    Utilities.log "\n", nil
    results = {}
    samples[0].each_key do |region|
      region_samples = samples.map { |s| s[region] }
      mean = region_samples.inject(:+) / samples.size
      human = "#{region.strip} #{mean.round(2)} s"
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
      puts JSON.generate(Hash[results.map { |key, values| [key.strip, values] }])
    end
  end

  def get_times(trace, total)
    start_times = {}
    times = {}
    depth = 1
    accounted_for = 0
    trace.lines do |line|
      if line =~ /^([a-z\-]+) (\d+\.\d+)$/
        region = $1
        time = $2.to_f
        if region.start_with? 'before-'
          depth += 1
          region = (' ' * depth + region['before-'.size..-1])
          start_times[region] = time
        elsif region.start_with? 'after-'
          region = (' ' * depth + region['after-'.size..-1])
          depth -= 1
          elapsed = time - start_times[region]
          times[region] = elapsed
          accounted_for += elapsed if depth == 2
        end
      end
    end
    times[' jvm'] = total - times['  main'] if times['  main']
    times['total'] = total
    times['unaccounted'] = total - accounted_for if times['    load-core']
    times
  end

  def benchmark(*args)
    args.map! do |a|
      if a.include?('.rb')
        benchmark = Utilities.find_benchmark(a)
        raise 'benchmark not found' unless File.exist?(benchmark)
        benchmark
      else
        a
      end
    end

    benchmark_ruby = ENV['JT_BENCHMARK_RUBY']

    run_args = []

    if args.delete('--sulong')
      run_args.push '--sulong'
    end

    if args.delete('--aot') || (ENV.has_key?('JT_BENCHMARK_RUBY') && (ENV['JT_BENCHMARK_RUBY'] == ENV['AOT_BIN']))
      run_args.push '-XX:YoungGenerationSize=2G'
      run_args.push '-XX:OldGenerationSize=2G'
      run_args.push "-Xhome=#{JRUBY_DIR}"

      # We already have a mechanism for setting the Ruby to benchmark, but elsewhere we use AOT_BIN with the "--aot" flag.
      # Favor JT_BENCHMARK_RUBY to AOT_BIN, but try both.
      benchmark_ruby ||= ENV['AOT_BIN']

      unless File.exist?(benchmark_ruby.to_s)
        raise "JT_BENCHMARK_RUBY or AOT_BIN must point at an AOT build of TruffleRuby"
      end
    end

    unless benchmark_ruby
      run_args.push '--graal' unless args.delete('--no-graal') || args.include?('list')
      run_args.push '-J-Dgraal.TruffleCompilationExceptionsAreFatal=true'
    end

    run_args.push "-I#{Utilities.find_gem('benchmark-ips')}/lib" rescue nil
    run_args.push "#{JRUBY_DIR}/bench/benchmark-interface/bin/benchmark"
    run_args.push *args

    if benchmark_ruby
      sh benchmark_ruby, *run_args
    else
      run *run_args
    end
  end

  def where(*args)
    case args.shift
    when 'repos'
      args.each do |a|
        puts Utilities.find_repo(a)
      end
    end
  end

  def install(name)
    case name
    when "graal"
    when "graal-core"
      install_graal_core
    else
      raise "Unknown how to install #{what}"
    end
  end

  def install_graal_core
    raise "Installing graal is only available on Linux and macOS currently" unless LINUX || MAC

    dir = "#{JRUBY_DIR}/graal"
    Dir.mkdir(dir) unless File.directory?(dir)
    Dir.chdir(dir) do
      unless File.directory?("#{dir}/mx")
        puts "Cloning mx"
        raw_sh "git", "clone", "https://github.com/graalvm/mx.git"
      end

      unless File.directory?("#{dir}/graal")
        puts "Cloning graal"
        raw_sh "git", "clone", "https://github.com/graalvm/graal.git"
      end

      if LINUX
        puts "Downloading JDK8 with JVMCI"
        if Dir["#{dir}/jdk1.8.0*"].empty?
          jvmci_releases = "https://github.com/dougxc/openjdk8-jvmci-builder/releases/download"
          jvmci_version = "jvmci-0.23"
          raw_sh "wget", "#{jvmci_releases}/#{jvmci_version}/jdk1.8.0_111-#{jvmci_version}-linux-amd64.tar.gz", "-O", "jvmci.tar.gz"
          raw_sh "tar", "xf", "jvmci.tar.gz"
          java_home = Dir["#{dir}/jdk1.8.0*"].sort.first
        end
      elsif MAC
        jvmci_version = "jvmci-0.24"
        puts "You need to download manually the latest JVMCI-enabled JDK at"
        puts "http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html"
        puts "Download the file named labsjdk-8u121-#{jvmci_version}-darwin-amd64.tar.gz"
        puts "And move it to the directory #{dir}"
        puts "When done, enter 'done':"
        begin
          print "> "
          done = STDIN.gets
        end until done.chomp == "done"
        archive = Dir["#{dir}/labsjdk-*darwin*.tar.gz"].sort.first
        abort "Could not find the JVMCI-enabled JDK" unless archive
        raw_sh "tar", "xf", archive
        java_home = Dir["#{dir}/labsjdk1.8.0*"].sort.first
      end

      abort "Could not find the extracted JDK" unless java_home
      java_home = File.expand_path(java_home)

      puts "Testing JDK"
      raw_sh "#{java_home}/bin/java", "-version"

      puts "Building graal"
      Dir.chdir("#{dir}/graal/compiler") do
        File.write("mx.compiler/env", "JAVA_HOME=#{java_home}\n")
        raw_sh "../../mx/mx", "build"
      end

      puts "Running with Graal"
      env = { "GRAAL_HOME" => "#{dir}/graal/compiler" }
      sh env, "tool/jt.rb", "ruby", "--graal", "-e", "p Truffle::Graal.graal?"

      puts
      puts "To run with graal, run:"
      puts "GRAAL_HOME=#{dir}/graal/compiler tool/jt.rb ruby --graal ..."
    end
  end

  def next(*args)
    puts `cat spec/tags/core/**/**.txt | grep 'fails:'`.lines.sample
  end

  def check_dsl_usage
    mx(JRUBY_DIR, 'clean')
    # We need to build with -parameters to get parameter names
    mx(JRUBY_DIR, 'build', '-A-parameters')
    run({ "TRUFFLE_CHECK_DSL_USAGE" => "true" }, '-e', 'exit')
  end

  def rubocop(*args)
    version = "0.48.1"
    begin
      require 'rubocop'
    rescue LoadError
      sh "gem", "install", "rubocop", "--version", version
    end
    sh "rubocop", format('_%s_', version), *args
  end

  def lint
    check_dsl_usage
    rubocop
    sh "tool/lint.sh"
  end

  def verify_aot_bin!
    unless File.exist?(ENV['AOT_BIN'].to_s)
      raise "AOT_BIN must point at an AOT build of TruffleRuby"
    end
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

    case args.first
    when "rebuild"
      send(args.shift)
    when "build"
      command = [args.shift]
      while ['cexts', 'parser', 'options', '--no-openssl'].include?(args.first)
        command << args.shift
      end
      send(*command)
    end

    return if args.empty?

    commands = Commands.public_instance_methods(false).map(&:to_s)

    command, *rest = args
    command = "command_#{command}" if %w[p puts].include? command

    abort "no command matched #{command.inspect}" unless commands.include?(command)

    begin
      send(command, *rest)
    rescue
      puts "Error during command: #{args*' '}"
      raise $!
    end
  end
end

JT.new.main(ARGV)
