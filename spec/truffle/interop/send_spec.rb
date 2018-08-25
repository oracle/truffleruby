# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Interop #__send__" do

  it "can call special forms like outgoing #inspect" do
    Truffle::Debug.foreign_object.__send__(:inspect).should =~ /#<Foreign:0x\h+>/
  end

end
