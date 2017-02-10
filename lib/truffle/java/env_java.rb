# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Java
  class SystemPropertyMap

    include Enumerable

    def [](key)
      java.lang.System.getProperty(key)
    end

    def []=(key, value)
      java.lang.System.setProperty(key, value)
    end

    def each
      keys = java.lang.System.getProperties.propertyNames
      while keys.hasMoreElements do
        key = keys.nextElement
        yield [key, self[key]]
      end
    end

    def to_s
      s = "{" + (self.map { |x| '"' + x[0].to_s + '"=>"' + x[1].to_s +  + '"' }).join(", ") + "}"
    end

    def inspect
      self.to_s
    end
  end
end

ENV_JAVA = Java::SystemPropertyMap.new
