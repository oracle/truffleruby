# Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Process.daemon" do
  it "is not implemented" do
    Process.respond_to?(:daemon).should == false

    -> { Process.daemon }.should raise_error(NotImplementedError) { |e|
      e.message.should include('daemon')
    }
  end
end
