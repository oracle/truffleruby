# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'
require 'benchmark'
require 'json'

describe "The launcher" do
  @versions = JSON.parse(File.read(File.expand_path('../../../versions.json', __FILE__)))

  @launchers = {
    bundle: /^Bundler version #{Regexp.escape @versions['gems']['default']['bundler']}$/,
    bundler: /^Bundler version #{Regexp.escape @versions['gems']['default']['bundler']}$/,
    gem: /^#{Regexp.escape @versions['gems']['default']['gem']}$/,
    irb: /^irb #{Regexp.escape @versions['gems']['default']['irb']}/,
    rake: /^rake, version #{Regexp.escape @versions['gems']['default']['rake']}/,
    rdoc: /^#{Regexp.escape @versions['gems']['default']['rdoc']}$/,
    ri: /^ri #{Regexp.escape @versions['gems']['default']['rdoc']}$/,
    ruby: /truffleruby .* like ruby #{Regexp.escape RUBY_VERSION}/,
    truffleruby: /truffleruby .* like ruby #{Regexp.escape RUBY_VERSION}/,
  }

  before :all do
    @default_bindir = RbConfig::CONFIG['bindir']
    @bin_dirs = [RbConfig::CONFIG['bindir'], *RbConfig::CONFIG['extra_bindirs'].split(File::PATH_SEPARATOR)]
  end

  before :each do
    @gem_home = ENV['GEM_HOME']
    ENV['GEM_HOME'] = nil
  end

  after :each do
    ENV['GEM_HOME'] = @gem_home
  end

  it "is in the bindir" do
    File.dirname(RbConfig.ruby).should == @default_bindir
  end

  @launchers.each do |launcher, test|
    unless [:ruby, :truffleruby].include?(launcher)
      it "runs #{launcher} as an -S command" do
        out = ruby_exe(nil, options: "-S#{launcher} --version")
        out.should =~ test
        $?.success?.should == true
      end
    end
  end

  @launchers.each do |launcher, test|
    it "supports running #{launcher} in any of the bin/ directories" do
      @bin_dirs.each do |bin_dir|
        out = `#{bin_dir}/#{launcher} --version`
        out.should =~ test
        $?.success?.should == true
      end
    end
  end

  @launchers.each do |launcher, test|
    it "supports running #{launcher} symlinked" do
      require 'tmpdir'
      @bin_dirs.each do |bin_dir|
        # Use the system tmp dir to not be under the Ruby home dir
        Dir.mktmpdir do |path|
          Dir.chdir(path) do
            linkname = "linkto#{launcher}"
            File.symlink("#{bin_dir}/#{launcher}", linkname)
            out = `./#{linkname} --version 2>&1`
            out.should =~ test
            $?.success?.should == true
          end
        end
      end
    end
  end

  it "for gem can install and uninstall the hello-world gem" do
    # install
    Dir.chdir(__dir__ + '/fixtures/hello-world') do
      `"#{@default_bindir}/gem" build hello-world.gemspec`
      $?.success?.should == true
      `"#{@default_bindir}/gem" install --local hello-world-0.0.1.gem`
      $?.success?.should == true
    end

    begin
      # check that hello-world launchers are created and work
      @bin_dirs.each do |bin_dir|
        path = "#{bin_dir}/hello-world.rb"
        shebang = File.binread(path).lines.first.chomp
        if shebang.size > 127
          skip "shebang of #{path} is too long and might fail in execve(): #{shebang.size}\n#{shebang}"
        end
        out = `#{path} 2>&1`
        out.should == "Hello world! from #{RUBY_DESCRIPTION}\n"
      end
    ensure
      # uninstall
      `#{@default_bindir}/gem uninstall hello-world -x`
      $?.success?.should == true
      @bin_dirs.each do |bin_dir|
        File.exist?(bin_dir + '/hello-world.rb').should == false
      end
    end
  end

  it "for gem shows that bundled gems are installed" do
    gem_list = `#{@default_bindir}/gem list`
    $?.success?.should == true
    # see doc/contributor/stdlib.md
    @versions['gems']['bundled'].each_pair do |gem, version|
      gem_list.should =~ /#{Regexp.escape gem}.*#{Regexp.escape version}/
    end
  end

  def should_print_full_java_command(options, env: {})
    out = ruby_exe(nil, options: options, env: env)
    parts = out.split(' ')
    parts[0].should == "$"
    parts[1].should =~ /(java|graalvm)$/
    $?.success?.should == true
  end

  it "does not create context on --version and -v" do
    v = ruby_exe(nil, options: "--log.level=FINE -v", args: "2>&1")
    v.should_not include("createContext()")
    v.should_not include("patchContext()")
    v.should include("truffleruby ")

    version = ruby_exe(nil, options: "--log.level=FINE --version", args: "2>&1")
    version.should_not include("createContext()")
    version.should_not include("patchContext()")
    version.should include("truffleruby ")
  end

  it "preserve spaces in options" do
    out = ruby_exe("print Truffle::System.get_java_property('foo')",
                   options: '--vm.Dfoo="value with spaces"')
    $?.success?.should == true
    out.should == "value with spaces"
  end

  it "takes normal Ruby options from TRUFFLERUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "TRUFFLERUBYOPT" => "-W2" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "takes --option options from TRUFFLERUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "TRUFFLERUBYOPT" => "--verbose=true" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "takes --ruby.option options from TRUFFLERUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "TRUFFLERUBYOPT" => "--ruby.verbose=true" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "takes normal Ruby options from RUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "RUBYOPT" => "-W2" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "takes --option options from RUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "RUBYOPT" => "--verbose=true" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "takes --ruby.option options from RUBYOPT" do
    out = ruby_exe("puts $VERBOSE", env: { "RUBYOPT" => "--ruby.verbose=true" })
    $?.success?.should == true
    out.should == "true\n"
  end

  it "takes options from system properties set on the command line using --vm" do
    out = ruby_exe("puts $VERBOSE", options: "--vm.Dpolyglot.ruby.verbose=true")
    $?.success?.should == true
    out.should == "true\n"
  end

  it "prioritises options on the command line over system properties" do
    out = ruby_exe("puts $VERBOSE", options: '--vm.Dpolyglot.ruby.verbose=nil -W2')
    $?.success?.should == true
    out.should == "true\n"
  end

  guard -> { !TruffleRuby.native? } do
    it "'--vm.cp=' or '--vm.classpath=' add the jar" do
      out = ruby_exe("puts Truffle::System.get_java_property('java.class.path')", options: "--vm.cp=does-not-exist.jar")
      $?.success?.should == true
      out.lines[0].should include(":does-not-exist.jar")

      out = ruby_exe("puts Truffle::System.get_java_property('java.class.path')", options: "--vm.classpath=does-not-exist.jar")
      $?.success?.should == true
      out.lines[0].should include(":does-not-exist.jar")
    end
  end

  it "prints available user options for --help:languages" do
    out = ruby_exe(nil, options: "--help:languages")
    $?.success?.should == true
    out.should include("--ruby.verbose")
  end

  it "prints available expert options for --help:languages --help:expert" do
    out = ruby_exe(nil, options: "--help:languages --help:expert")
    $?.success?.should == true
    out.should include("--ruby.cexts-log-load")
  end

  it "prints available internal options for --help:languages --help:internal" do
    out = ruby_exe(nil, options: "--help:languages --help:internal")
    $?.success?.should == true
    out.should include("--ruby.default-cache")
  end

  it "logs options if --options-log is set" do
    out = ruby_exe("14", options: "--experimental-options --log.level=CONFIG --options-log", args: "2>&1")
    $?.success?.should == true
    out.should include("[ruby] CONFIG")
  end

  it "prints an error for an unknown option" do
    out = ruby_exe(nil, options: "--unknown=value", args: "2>&1")
    $?.success?.should == false
    out.should include("invalid option --unknown=value")

    out = ruby_exe(nil, options: "--ruby.unknown=value", args: "2>&1")
    $?.success?.should == false
    out.should include("invalid option --ruby.unknown=value")
  end

  it "sets the log level using --log.level=" do
    out = ruby_exe("14", options: "--experimental-options --options-log --log.level=CONFIG", args: "2>&1")
    $?.success?.should == true
    out.should include("CONFIG: option default-cache=")
  end

  it "sets the log level using --log.ruby.level=" do
    out = ruby_exe("14", options: "--experimental-options --options-log --log.ruby.level=CONFIG", args: "2>&1")
    $?.success?.should == true
    out.should include("CONFIG: option default-cache=")
  end

  describe 'StringArray option' do
    it "appends multiple options" do
      out = ruby_exe("puts $LOAD_PATH", options: "-I a -I b")
      $?.success?.should == true
      out.lines[0].should == "#{Dir.pwd}/a\n"
      out.lines[1].should == "#{Dir.pwd}/b\n"
    end

    it "parses ," do
      out = ruby_exe("puts $LOAD_PATH", options: "--load-paths=a,b")
      $?.success?.should == true
      out.lines[0].should == "#{Dir.pwd}/a\n"
      out.lines[1].should == "#{Dir.pwd}/b\n"
    end

    it "parses , respecting escaping" do
      # \\\\ translates to one \
      out = ruby_exe("puts $LOAD_PATH", options: "--load-paths=a\\\\,b,,\\\\c")
      $?.success?.should == true
      out.lines[0].should == "#{Dir.pwd}/a,b\n"
      out.lines[1].should == "#{Dir.pwd}\n"
      out.lines[2].should == "#{Dir.pwd}/\\c\n"
    end
  end

  it "enables deterministic hashing if --hashing-deterministic is set" do
    out = ruby_exe("puts 14.hash", options: "--experimental-options --hashing-deterministic", args: "2>&1")
    $?.success?.should == true
    out.should include("SEVERE: deterministic hashing is enabled - this may make you vulnerable to denial of service attacks")
    out.should include("7141275149799654099")
  end

  it "prints help containing runtime options" do
    out = ruby_exe(nil, options: "--help")
    $?.success?.should == true

    if TruffleRuby.native?
      out.should include("--native")
    else
      out.should include("--jvm")
    end

    if Truffle::System.get_java_property 'org.graalvm.home'
      # These options are only shown in GraalVM, as they are not available in a standalone distribution
      out.should include("--polyglot")
    end
  end

  it "prints help:languages containing ruby language options" do
    out = ruby_exe(nil, options: "--help:languages")
    $?.success?.should == true
    out.should =~ /language options/i
    out.should include("Ruby:")
    out.should include("--ruby.load-paths=")
  end

  guard -> { TruffleRuby.cexts? } do
    it "prints help:languages containing llvm language options" do
      out = ruby_exe(nil, options: "--help:languages")
      $?.success?.should == true
      out.should =~ /language options/i
      out.should include("LLVM:")
      out.should include("--llvm.libraryPath=")
    end
  end

  it "prints the version with --version" do
    out = ruby_exe(nil, options: "--version")
    $?.success?.should == true
    out.should include(RUBY_DESCRIPTION)
  end

  it "understands ruby polyglot options" do
    out = ruby_exe("p Truffle::Boot.get_option('rubygems')", options: "--experimental-options --ruby.rubygems=false")
    $?.success?.should == true
    out.should include('false')
  end

  it "understands ruby polyglot options without ruby. prefix" do
    out = ruby_exe("p Truffle::Boot.get_option('rubygems')", options: "--experimental-options --rubygems=false")
    $?.success?.should == true
    out.should include('false')
  end

  it "does not print a Java backtrace for an -S file that's not found" do
    out = ruby_exe(nil, options: "-S does_not_exist", args: "2>&1")
    $?.success?.should == false
    out.should include('truffleruby: No such file or directory -- does_not_exist (LoadError)')
    out.should_not include('boot.rb')
    out.should_not include('RubyLauncher.main')
  end

  it "ignores --jit... options with a warning and a hint to look at Graal documentation" do
    [
      "--jit",
      "--jit-warnings",
      "--jit-debug",
      "--jit-wait",
      "--jit-save-temps",
      "--jit-verbose",
      "--jit-max-cache",
      "--jit-min-calls",
    ].each do |option|
      out = ruby_exe("p 14", options: option, args: "2>&1")
      $?.success?.should == true
      out.should include("JIT options are not supported - see the Graal documentation instead")
      out.should include("14")
    end
  end

  it "warns on ignored options" do
    [
      "-y",
      "--yydebug",
      "--debug-frozen-string-literal",
      "--dump=insns",
    ].each do |option|
      out = ruby_exe("p 14", options: option, args: "2>&1")
      $?.success?.should == true
      out.should include("[ruby] WARNING the #{option} switch is silently ignored as it is an internal development tool")
      out.should include("14")
    end
  end

end
