# truffleruby_primitives: true

# Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'objspace'

require_relative '../../ruby/spec_helper'

describe "ObjectSpace.undefine_finalizer" do

  # See comment in define_finalizer_spec.rb

  it "successfully unregisters a finalizer" do
    channel = Truffle::Channel.new
    Object.new.tap do |object|
      finalizer = proc {
        channel.send :finalized
      }
      ObjectSpace.define_finalizer object, finalizer
      ObjectSpace.reachable_objects_from(object).should include(finalizer)
      ObjectSpace.undefine_finalizer object
      ObjectSpace.reachable_objects_from(object).should_not include(finalizer)
    end
    Primitive.gc_force
    Truffle::Debug.drain_finalization_queue               # Not needed for correctness
    channel.try_receive.should be_nil
  end

end
