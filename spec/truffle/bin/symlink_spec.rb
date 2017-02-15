# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../../ruby/spec_helper'

describe "bin/ruby" do

  it "runs when symlinked" do
    require "tmpdir"
    Dir.mktmpdir do |path|
      Dir.chdir(path) do
        `ln -s #{Truffle::Boot.ruby_home}/bin/ruby linktoruby`
        `./linktoruby --version`.should include("truffleruby")
      end
    end
  end

end
