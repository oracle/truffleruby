# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle
  module ExceptionOperations
    def self.class_name(receiver)
      Truffle::Type.object_class(receiver).name
    end

    NO_METHOD_ERROR = Proc.new do |exception|
      if exception.receiver.respond_to?(:inspect)
        format('undefined method `%s\' for %s:%s', exception.name, exception.receiver.inspect, class_name(exception.receiver))
      else
        format('undefined method `%s\' for <#%s:%s>', exception.name, class_name(exception.receiver), exception.receiver.object_id)
      end
    end

    PRIVATE_METHOD_ERROR = Proc.new do |exception|
      format("private method `%s' called for %s", exception.name, class_name(exception.receiver))
    end

    PROTECTED_METHOD_ERROR = Proc.new do |exception|
      format("protected method `%s' called for %s", exception.name, class_name(exception.receiver))
    end

    SUPER_METHOD_ERROR = Proc.new do |exception|
      format("super: no superclass method `%s'", exception.name)
    end
  end
end
