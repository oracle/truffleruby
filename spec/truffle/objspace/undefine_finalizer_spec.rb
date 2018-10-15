# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "ObjectSpace.undefine_finalizer" do

  it "if not used leaves the finalizer in place" do
    finalized = 0
    finalizer = proc {
     finalized += 1
    }
    started = Time.now
    until Time.now > started + 3
      object = Object.new
      ObjectSpace.define_finalizer object, finalizer
    end
    finalized.should > 0
  end

  it "successfully unregisters a finalizer" do
    finalized = 0
    finalizer = proc {
     finalized += 1
    }
    started = Time.now
    until Time.now > started + 3
      object = Object.new
      ObjectSpace.define_finalizer object, finalizer
      ObjectSpace.undefine_finalizer object
    end
    finalized.should == 0
  end

end
