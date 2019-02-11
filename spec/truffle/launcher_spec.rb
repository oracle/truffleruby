# Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'
require 'benchmark'

describe "The launcher" do

  it 'is in the bindir' do
    bindir = File.expand_path(RbConfig::CONFIG['bindir'])
    File.expand_path(File.dirname(RbConfig.ruby)).should == bindir
  end

  launchers = { gem:         /^2\.6\.14\.1$/,
                irb:         /^irb 0\.9\.6/,
                rake:        /^rake, version [0-9.]+/,
                rdoc:        /^5\.0\.0$/,
                ri:          /^ri 5\.0\.0$/,
                ruby:        /truffleruby .* like ruby 2\.4\.4/,
                testrb:      [/^testrb: version unknown$/, true],
                truffleruby: /truffleruby .* like ruby 2\.4\.4/ }

  launchers.each do |launcher, (test, skip_success)|
    extra_bin_dirs_described = RbConfig::CONFIG['extra_bindirs'].
        split(File::PATH_SEPARATOR).
        each_with_index.
        reduce({}) { |h, (dir, i)| h.update "RbConfig::CONFIG['extra_bindirs'][#{i}]" => dir }
    bin_dirs = { "RbConfig::CONFIG['bindir']" => RbConfig::CONFIG['bindir'] }.merge extra_bin_dirs_described

    bin_dirs.each do |name, bin_dir|
      it "'#{launcher}' in `#{name}` directory runs when symlinked" do
        require "tmpdir"
        # Use the system tmp dir to not be under the Ruby home dir
        Dir.mktmpdir do |path|
          Dir.chdir(path) do
            linkname = "linkto#{launcher}"
            File.symlink("#{bin_dir}/#{launcher}", linkname)
            out = `./#{linkname} --version 2>&1`
            out.should =~ test
            $?.success?.should == true unless skip_success
          end
        end
      end
    end
  end

  def should_print_full_java_command(options, env: {})
    out = ruby_exe(nil, options: options, env: env)
    parts = out.split(' ')
    parts[0].should == "$"
    parts[1].should =~ /(java|graalvm)$/
    $?.success?.should == true
  end

  guard -> { !TruffleRuby.native? } do
    it "prints the full java command with -J-cmd" do
      should_print_full_java_command "-J-cmd --version"
    end

    it "prints the full java command with --jvm.cmd" do
      should_print_full_java_command "--jvm.cmd --version"
    end

    it "prints the full java command with -cmd in JAVA_OPTS" do
      should_print_full_java_command "--version", env: { "JAVA_OPTS" => "-cmd" }
    end

    it "adds options from $JAVA_OPTS to the command" do
      option = '-Dfoo.bar=baz'
      out = ruby_exe(nil, options: "-J-cmd --version", env: { "JAVA_OPTS" => option })
      parts = out.lines[0].split(' ')
      parts.find { |part| part =~ /^#{option}$/ }.should_not be_nil
      $?.success?.should == true
    end
  end

  it "does not create context on --version and -v" do
    v = ruby_exe(nil, options: "--log.ruby.level=FINE -v", args: "2>&1")
    v.should_not include("createContext()")
    v.should_not include("patchContext()")
    v.should include("truffleruby ")

    version = ruby_exe(nil, options: "--log.ruby.level=FINE --version", args: "2>&1")
    version.should_not include("createContext()")
    version.should_not include("patchContext()")
    version.should include("truffleruby ")
  end

  it "preserve spaces in options" do
    out = ruby_exe("print Truffle::System.get_java_property('foo')",
                   options: (TruffleRuby.native? ? '--native.' : '-J-') + 'Dfoo="value with spaces"')
    $?.success?.should == true
    out.should == "value with spaces"
  end

  it "takes options from TRUFFLERUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "TRUFFLERUBYOPT" => "-W2" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "takes options from RUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "RUBYOPT" => "-W2" })
    $?.success?.should == true
    out.should == "true\n"
  end

  guard -> { !TruffleRuby.native? } do
    it "takes options from system properties set in JAVA_OPTS" do
      out = ruby_exe("puts $VERBOSE", env: { "JAVA_OPTS" => "-Dpolyglot.ruby.verbosity=true" })
      $?.success?.should == true
      out.should == "true\n"
    end

    it "takes options from system properties set on the command line using -J" do
      out = ruby_exe("puts $VERBOSE", options: "-J-Dpolyglot.ruby.verbosity=true")
      $?.success?.should == true
      out.should == "true\n"
    end

    it "takes options from system properties set on the command line using --jvm" do
      out = ruby_exe("puts $VERBOSE", options: "--jvm.Dpolyglot.ruby.verbosity=true")
      $?.success?.should == true
      out.should == "true\n"
    end
  end

  guard -> { TruffleRuby.native? } do
    it "takes options from system properties set on the command line using --native" do
      out = ruby_exe("puts $VERBOSE", options: "--native.Dpolyglot.ruby.verbosity=true")
      $?.success?.should == true
      out.should == "true\n"
    end
  end

  it "takes options from system properties set on the command line using -X" do
    out = ruby_exe("puts $VERBOSE", options: "-Xverbosity=true")
    $?.success?.should == true
    out.should == "true\n"
  end

  it "prioritises options on the command line over system properties" do
    out = ruby_exe("puts $VERBOSE", options: "-W2", env: { "JAVA_OPTS" => "-Dpolyglot.ruby.verbosity=nil" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "prioritises options on the command line using -X over system properties" do
    out = ruby_exe("puts $VERBOSE", options: "-Xverbosity=true", env: { "JAVA_OPTS" => "-Dpolyglot.ruby.verbosity=nil" })
    $?.success?.should == true
    out.should == "true\n"
  end

  guard -> { !TruffleRuby.native? } do
    it "allows -cp in JAVA_OPTS" do
      out = ruby_exe("puts 14", options: "-J-cmd", env: { "JAVA_OPTS" => "-cp does-not-exist.jar" })
      $?.success?.should == true
      out.lines[0].should include(":does-not-exist.jar")
      out.lines[1].should == "14\n"
    end

    it "allows -classpath in JAVA_OPTS" do
      out = ruby_exe("puts 14", options: "-J-cmd", env: { "JAVA_OPTS" => "-classpath does-not-exist.jar" })
      $?.success?.should == true
      out.lines[0].should include(":does-not-exist.jar")
      out.lines[1].should == "14\n"
    end

    ['-J-classpath ',
     '-J-cp ',
     '--jvm.classpath=',
     '--jvm.cp='
    ].each do |option|
      it "'#{option}' adds the jar" do
        out = ruby_exe("puts 14", options: "#{option}does-not-exist.jar -J-cmd")
        $?.success?.should == true
        out.lines[0].should include(":does-not-exist.jar")
        out.lines[1].should == "14\n"
      end
    end
  end

  it "prints available options for -Xoptions" do
    out = ruby_exe(nil, options: "-Xoptions")
    $?.success?.should == true
    out.should include("-Xverbosity=")
  end

  it "prints available user options for --help:languages" do
    out = ruby_exe(nil, options: "--help:languages")
    $?.success?.should == true
    out.should include("--ruby.verbosity")
  end

  it "prints available expert options for --help:languages --help:expert" do
    out = ruby_exe(nil, options: "--help:languages --help:expert")
    $?.success?.should == true
    out.should include("--ruby.home")
  end

  it "prints available debug options for --help:languages --help:debug" do
    out = ruby_exe(nil, options: "--help:languages --help:debug")
    $?.success?.should == true
    out.should include("--ruby.exceptions.print_java")
  end

  it "logs options if -Xoptions.log is set" do
    out = ruby_exe("14", options: "--log.ruby.level=CONFIG", args: "2>&1")
    $?.success?.should == true
    out.should include("[ruby] FINE")
  end

  it "prints an error for an unknown option" do
    out = ruby_exe(nil, options: "-Xunknown=value", args: "2>&1")
    $?.success?.should == false
    out.should include("invalid option --ruby.unknown=value (-Xunknown=value)")
    out = ruby_exe(nil, options: "--unknown=value", args: "2>&1")
    $?.success?.should == false
    out.should include("invalid option --unknown=value")
  end

  it "sets the log level using -Xlog=" do
    out = ruby_exe("14", options: "-Xoptions.log -Xlog=CONFIG", args: "2>&1")
    $?.success?.should == true
    out.should include("CONFIG: option home=")
  end

  it "sets the log level using --log.ruby.level=" do
    out = ruby_exe("14", options: "-Xoptions.log --log.ruby.level=CONFIG", args: "2>&1")
    $?.success?.should == true
    out.should include("CONFIG: option home=")
  end

  describe 'StringArray option' do
    it 'appends multiple options' do
      out = ruby_exe("puts $LOAD_PATH", options: "-I a -I b")
      $?.success?.should == true
      out.lines[0].should == "#{Dir.pwd}/a\n"
      out.lines[1].should == "#{Dir.pwd}/b\n"
    end

    it 'parses ,' do
      out = ruby_exe("puts $LOAD_PATH", options: "-Xload_paths=a,b")
      $?.success?.should == true
      out.lines[0].should == "#{Dir.pwd}/a\n"
      out.lines[1].should == "#{Dir.pwd}/b\n"
    end

    it 'parses , respecting escaping' do
      # \\\\ translates to one \
      out = ruby_exe("puts $LOAD_PATH", options: "-Xload_paths=a\\\\,b,,\\\\c")
      $?.success?.should == true
      out.lines[0].should == "#{Dir.pwd}/a,b\n"
      out.lines[1].should == "#{Dir.pwd}\n"
      out.lines[2].should == "#{Dir.pwd}/\\c\n"
    end
  end

  it "enables deterministic hashing if -Xhashing.deterministic is set" do
    out = ruby_exe("puts 14.hash", options: "-Xhashing.deterministic", args: "2>&1")
    $?.success?.should == true
    out.should include("SEVERE: deterministic hashing is enabled - this may make you vulnerable to denial of service attacks")
    out.should include("7141275149799654099")
  end

  it "prints help containing runtime options" do
    out = ruby_exe(nil, options: "--help", args: "2>&1")
    $?.success?.should == true
    out.should include("--native")

    if Truffle::System.get_java_property 'org.graalvm.home'
      # These options are only shown in GraalVM, as they are not available in a standalone distribution
      out.should include("--polyglot")
      out.should include("--jvm")
    end
  end

  it "prints help:languages containing ruby language options" do
    out = ruby_exe(nil, options: "--help:languages", args: "2>&1")
    $?.success?.should == true
    out.should =~ /language options/i
    out.should include("Ruby:")
    out.should include("--ruby.load_paths=")
  end

  guard -> { TruffleRuby.sulong? } do
    it "prints help:languages containing llvm language options" do
      out = ruby_exe(nil, options: "--help:languages", args: "2>&1")
      $?.success?.should == true
      out.should =~ /language options/i
      out.should include("LLVM:")
      out.should include("--llvm.libraryPath=")
    end
  end

  it "understands ruby polyglot options" do
    out = ruby_exe(nil, options: "--ruby.show_version=true --ruby.to_execute=p:b --ruby.execution_action=INLINE", args: "2>&1")
    $?.success?.should == true
    out.should include(RUBY_DESCRIPTION)
    out.should include(':b')
  end

  it "understands ruby polyglot options without ruby. prefix" do
    out = ruby_exe(nil, options: "--show_version=true --to_execute=p:b --execution_action=INLINE", args: "2>&1")
    $?.success?.should == true
    out.should include(RUBY_DESCRIPTION)
    out.should include(':b')
  end

  it "does not print a Java backtrace for an -S file that's not found" do
    out = ruby_exe(nil, options: "-S does_not_exist", args: "2>&1")
    $?.success?.should == false
    out.should include('truffleruby: No such file or directory -- does_not_exist (LoadError)')
    out.should_not include('boot.rb')
    out.should_not include('RubyLauncher.main')
  end

  guard -> { TruffleRuby.graal? } do
    it "applies Truffle options" do
      prefix = TruffleRuby.native? ? '--native.' : '--jvm.'
      options = [
        "#{prefix}Dgraal.TraceTruffleCompilation=true",
        "#{prefix}Dgraal.TruffleBackgroundCompilation=false",
      ].join(" ")
      out = ruby_exe("2000.times {}", options: options, args: "2>&1")
      out.should include("[truffle] opt done")
    end
  end
end
