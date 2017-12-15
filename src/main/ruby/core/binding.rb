# Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

class Binding
  def eval(code, file = nil, line = nil)
    Kernel.eval(code, self, file, line)
  end

  def local_variables
    Truffle.invoke_primitive(:local_variable_names, self).dup
  end
  Truffle::Graal.always_split(method(:local_variables))
end
