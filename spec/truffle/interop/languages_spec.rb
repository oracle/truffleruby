# Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.languages" do
  it "returns the public languages" do
    Truffle::Interop.languages.should.include?('ruby')
  end

  it "returns only ruby from the RubyLauncher" do
    # Use RbConfig.ruby to remove a potential --polyglot option
    `#{RbConfig.ruby} -e 'p Truffle::Interop.languages'`.should == "[\"ruby\"]\n"
  end
end
