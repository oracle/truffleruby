# truffleruby_primitives: true

# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

# Similar tests as in https://github.com/ruby/yarp/blob/main/.github/workflows/truffleruby.yml
describe "YARP" do
  root = File.expand_path("../..", __dir__)

  guard -> { Dir.exist?("#{root}/src/main/ruby/truffleruby") } do
    it "can parse core files" do
      Dir.glob("#{root}/src/main/ruby/truffleruby/**/*.rb") do |file|
        Truffle::Debug.yarp_parse(File.read(file)).should.include?("Node")
      end
    end
  end

  it "can execute simple code" do
    -> {
      -> {
        Truffle::Debug.yarp_execute("p 1+2").should == 3
      }.should output_to_fd(/^YARP AST:.+Truffle AST:/m, STDERR)
    }.should output("3\n")
  end
end
