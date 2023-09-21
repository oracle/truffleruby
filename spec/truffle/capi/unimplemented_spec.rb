# Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

extension_path = load_extension("unimplemented")

describe "Unimplemented functions in the C-API" do
  it "abort the process and show an error including the function name" do
    out = ruby_exe('require ARGV[0]; CApiRbTrErrorSpecs.new.not_implemented_function("foo")', args: "#{extension_path} 2>&1", exit_status: 127)
    out.should.include?('undefined symbol: rb_str_shared_replace')
  end
end
