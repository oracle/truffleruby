# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

class JavaProxy
  include JavaProxyMethods

  attr_accessor :java_object

  class << self
    attr_writer :java_class
    def java_class
      JavaUtilities::wrap_java_value(@java_class)
    end
  end

  def self.const_missing(name)
    JavaUtilities.get_inner_class(java_class, name)
  end
  
  def to_java_object
    java_object
  end

  def eql?(another)
  end
  
end
