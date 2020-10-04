# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

example "Thread.detect_recursion(Object.new) { }", false
example "Thread.detect_recursion([]) { }", false
example "Thread.detect_recursion({}) { }", false

example "y = nil; Thread.detect_recursion(Object.new) { y = Thread.detect_recursion(Object.new) { } }; y", false
example "x = Object.new; y = nil; Thread.detect_recursion(x) { y = Thread.detect_recursion(x) { } }; y", true

def detect_recursion_recursive(method, object)
  Thread.detect_recursion(object) do
    object.send(method) do |child|
      return detect_recursion_recursive(method, child)
    end
  end
end

example "detect_recursion_recursive(:each, [])", false
example "detect_recursion_recursive(:each_value, {})", false
example "a = []; a << a; detect_recursion_recursive(:each, a)", true
example "a = {}; a[:a] = a; detect_recursion_recursive(:each_value, a)", true
