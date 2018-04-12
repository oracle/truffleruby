# Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'

describe "The launcher" do

  it 'is in the bindir' do
    bindir = File.expand_path(RbConfig::CONFIG['bindir'])
    File.expand_path(File.dirname(RbConfig.ruby)).should == bindir
  end

  launchers = { gem:         /^2\.5\.2\.3$/,
                irb:         /^irb 0\.9\.6/,
                rake:        /^rake, version [0-9.]+/,
                rdoc:        /^4\.2\.1$/,
                ri:          /^ri 4\.2\.1$/,
                ruby:        /truffleruby .* like ruby 2\.3\.7/,
                testrb:      [/^testrb: version unknown$/, true],
                truffleruby: /truffleruby .* like ruby 2\.3\.7/ }

  launchers.each do |launcher, (test, skip_success)|
    extra_bin_dirs_described = RbConfig::CONFIG['extra_bindirs'].
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

  guard -> { !Truffle.native? } do
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

  it "preserve spaces in options" do
    out = ruby_exe("print Truffle::System.get_java_property('foo')",
                   options: (Truffle.native? ? '--native.' : '-J-') + 'Dfoo="value with spaces"')
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

  guard -> { !Truffle.native? } do
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

  guard -> { Truffle.native? } do
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

  guard -> { !Truffle.native? } do
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

  it "logs options if -Xoptions.log is set" do
    out = ruby_exe("14", options: "-Xoptions.log -Xlog=config", args: "2>&1")
    $?.success?.should == true
    out.should include("CONFIG option home=")
  end

  it "prints an error for an unknown option" do
    out = ruby_exe(nil, options: "-Xunknown=value", args: "2>&1")
    $?.success?.should == false
    out.should include("invalid option")
    out.should include("-Xunknown=value")
  end

  describe 'StringArray option' do
    it 'appends multiple options' do
      out = ruby_exe("p $LOAD_PATH", options: "-I a -I b")
      $?.success?.should == true
      out.should include('["a", "b", ')
    end

    it 'parses ,' do
      out = ruby_exe("p $LOAD_PATH", options: "-Xload_paths=a,b")
      $?.success?.should == true
      out.should include('["a", "b", ')
    end

    it 'parses , respecting escaping' do
      # \\\\ translates to one \
      out = ruby_exe("p $LOAD_PATH", options: "-Xload_paths=a\\\\,b,,\\\\c")
      $?.success?.should == true
      out.should include('["a,b", "", "\\\\c", ')
    end
  end

  it "enables deterministic hashing if -Xhashing.deterministic is set" do
    out = ruby_exe("puts 14.hash", options: "-Xhashing.deterministic", args: "2>&1")
    $?.success?.should == true
    out.should include("SEVERE deterministic hashing is enabled - this may make you vulnerable to denial of service attacks")
    out.should include("7141275149799654099")
  end

  it "prints help containing runtime options" do
    out = ruby_exe(nil, options: "--help", args: "2>&1")
    $?.success?.should == true
    out.should include("--polyglot")
    out.should include("--native")
    out.should include("--jvm")
  end

  it "prints help:languages containing ruby language options" do
    out = ruby_exe(nil, options: "--help:languages", args: "2>&1")
    $?.success?.should == true
    out.should include("Language Options:")
    out.should include("Ruby:")
    out.should include("--ruby.load_paths=")
  end

  it "prints help:tools containing tools options" do
    out = ruby_exe(nil, options: "--help:tools", args: "2>&1")
    $?.success?.should == true
    # we assume tools are always available
    out.should include("Tool options:")
    out.should include("Chrome Inspector:")
    out.should include("--inspect.Suspend=")
  end

  guard -> { Truffle.sulong? } do
    it "prints help:languages containing llvm language options" do
      out = ruby_exe(nil, options: "--help:languages", args: "2>&1")
      $?.success?.should == true
      out.should include("Language Options:")
      out.should include("llvm:")
      out.should include("--llvm.libraryPath=")
    end
  end

  it "understands ruby polyglot options" do
    out = ruby_exe(nil, options: "--ruby.show_version=true --ruby.to_execute=p:b --ruby.execution_action=INLINE", args: "2>&1")
    $?.success?.should == true
    out.should include(RUBY_DESCRIPTION)
    out.should include(':b')
  end

end
