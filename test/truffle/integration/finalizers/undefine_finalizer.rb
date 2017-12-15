# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Getting MRI to run a finalizer for a test seems problematic, due to the
# conservative GC and the separate C and Ruby stacks.

1_000_000.times do
  object = Object.new
  
  ObjectSpace.define_finalizer object, proc {
    puts 'finalized!'
    exit! 1
  }
  
  ObjectSpace.undefine_finalizer object
end
