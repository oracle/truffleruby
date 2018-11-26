# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle" do

  it "has no common constants with Object" do
    known_offenders = [
      :Gem, # Truffle::Gem::BCrypt, to remove with truffleruby-tool
      :Binding, # Truffle::Binding.of_caller, should move under TruffleRuby
    ]
    code = "puts((Truffle.constants & Object.constants) - #{known_offenders.inspect})"
    ruby_exe(code).should == ""
  end

end
