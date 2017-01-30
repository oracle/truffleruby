# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

class ArrayJavaProxy < java.lang.Object
  include Enumerable
  
  def each
    (0...size).each do |i|
      yield(self[i])
    end
    self
  end

  def inspect
    s = "[" + self.map do |x|
      x.to_s
    end.join(", ") + "]"
  end
end
