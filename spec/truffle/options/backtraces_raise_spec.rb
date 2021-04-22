# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "The --backtraces-raise option" do
  it "prints a backtrace on #raise" do
    file = fixture __FILE__ , 'raise_rescue.rb'
    out = ruby_exe(file, options: "--experimental-options --backtraces-raise", args: "2>&1")
    out = out.gsub(/:\d+:(in `(?:full_message|get_formatted_backtrace)')/, ':LINE:\1')
    out.should ==  <<~OUTPUT
    raise: <internal:core> core/exception.rb:LINE:in `full_message': foo (RuntimeError)
    \tfrom <internal:core> core/truffle/exception_operations.rb:LINE:in `get_formatted_backtrace'
    \tfrom #{file}:10:in `raise'
    \tfrom #{file}:10:in `<main>'
    OUTPUT
  end
end
