# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "ObjectSpace.define_finalizer" do

  it "will eventually call the finalizer with enough GC activity" do
    do_break = false
    until do_break
      expected_id = nil
      ObjectSpace.define_finalizer Object.new.tap { |object| expected_id = object.object_id }, proc { |finalized_id|
        finalized_id.should == expected_id
        do_break = true
      }
    end
  end

end
