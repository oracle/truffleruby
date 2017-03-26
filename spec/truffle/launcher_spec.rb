# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'

describe "The launcher" do

  before :each do
    @java_opts = ENV["JAVA_OPTS"]
  end

  after :each do
    ENV["JAVA_OPTS"] = @java_opts
  end

  it "runs when symlinked" do
    require "tmpdir"
    Dir.mktmpdir do |path|
      Dir.chdir(path) do
        `ln -s #{RbConfig.ruby} linktoruby`
        `./linktoruby --version`.should include("truffleruby")
        $?.success?.should == true
      end
    end
  end

  it "prints the full java command with -J-cmd" do
    out = `#{RbConfig.ruby} -J-cmd --version`
    parts = out.split(' ')
    parts[0].should == "$"
    parts[1].end_with?("java").should == true
    $?.success?.should == true
  end

  it "adds options from $JAVA_OPTS to the command" do
    option = '-Dfoo.bar=baz'
    ENV["JAVA_OPTS"] = option
    out = `#{RbConfig.ruby} -J-cmd --version`
    parts = out.lines[0].split(' ')
    parts.should include option
    $?.success?.should == true
  end

  it "preserve spaces in options" do
    out = `#{RbConfig.ruby} -Xgraal.warn_unless=false -J-Dfoo="value with spaces" -e "print Truffle::System.get_java_property('foo')"`
    $?.success?.should == true
    out.should == "value with spaces"
  end

  it "warns when not using Graal" do
    on_graalvm = !RbConfig.ruby.end_with?('/bin/truffleruby')
    out = `#{RbConfig.ruby} -e 'puts "Hello"' 2>&1`
    if on_graalvm
      out.should == "Hello\n"
    else
      out.should == <<-EOS
[ruby] PERFORMANCE this JVM does not have the Graal compiler - performance will be limited - see doc/user/using-graalvm.md
Hello
      EOS
    end
  end

end
