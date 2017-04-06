# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module JavaUtilities
  class JavaException < StandardError
    attr_reader :java_exception
    def initialize(java_exception)
      @java_exception = java_exception
      message = java_exception.to_string rescue 'A Java error occurred.'
      super(message)
    end
  end
end
