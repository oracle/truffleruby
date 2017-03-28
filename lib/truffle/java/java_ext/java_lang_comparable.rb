# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Ensure the proxy mechanism has created the module.
java.lang.Comparable

module ::Java::JavaLang::Comparable
  def <=>(another)
    case another
    when nil
      nil
    else
      begin
        self.compare_to another
      rescue java.lang.Exception
        raise TypeError
      end
    end
  end
end
