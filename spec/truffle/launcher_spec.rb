# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'

describe "bin/truffleruby" do

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
    parts[1].should == "java"
    $?.success?.should == true
  end

  it "adds flags from $JAVA_OPTS to the command" do
    option = '-Dfoo.bar=baz'
    ENV["JAVA_OPTS"] = option
    out = `#{RbConfig.ruby} -J-cmd --version`
    parts = out.lines[0].split(' ')
    parts.should include option
    $?.success?.should == true
  end

end
