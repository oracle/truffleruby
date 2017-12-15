# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Kernel#binding
example "x = 14; binding.local_variable_get(:x)", 14

# get on dup

example "x = 14; binding.dup.local_variable_get(:x)", 14

# Proc#binding
example "x = 14; p = Proc.new { }; p.binding.local_variable_get(:x)", 14

# set + get
example "b = binding; b.local_variable_set(:x, 14); b.local_variable_get(:x)", 14

# set + get on dup
example "b = binding.dup; b.local_variable_set(:x, 14); b.local_variable_get(:x)", 14

# get (2 levels)
example "x = 14; y = nil; 1.times { y = binding.local_variable_get(:x) }; y", 14

# set (2 levels)
example "x = 14; 1.times { binding.local_variable_set(:x, 15) }; x", 15

# get + set (2 levels)
example "x = 14; y = nil; 1.times { binding.local_variable_set(:x, 15); y = binding.local_variable_get(:x) }; y", 15

# defined

example "x = 14; b = binding; b.local_variable_defined?(:x)", true

# not defined

example "x = 14; b = binding; b.local_variable_defined?(:y)", false

# local_variables

tagged example "x = 14; y = 15; binding.local_variables[1]", :x
