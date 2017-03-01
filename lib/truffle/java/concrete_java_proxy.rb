# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

begin
  # Although we are outside the normal proxy creation process this
  # class does not represent a concrete Java type itself. Setting this
  # thread local will stop the JavaProxy meta-programming hooks from
  # attempting to generate a new Java class.
  Thread.current[:MAKING_JAVA_PROXY] = true
  class ConcreteJavaProxy < JavaProxy

    def to_s
      self.toString
    end
  end
ensure
  Thread.current[:MAKING_JAVA_PROXY] = false
end
