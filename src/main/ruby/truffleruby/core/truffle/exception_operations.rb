# frozen_string_literal: true

# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module ExceptionOperations
    def self.class_name(receiver)
      Truffle::Type.object_class(receiver).name
    end

    def self.receiver_string(exception)
      receiver = exception.receiver
      ret = begin
        if Truffle::Type.object_respond_to?(receiver, :inspect)
          if class_name = class_name(receiver)
            "#{receiver.inspect}:#{class_name}"
          else
            "#{receiver.inspect}"
          end
        else
          Truffle::Type.rb_any_to_s(receiver)
        end
      rescue Exception # rubocop:disable Lint/RescueException
        Truffle::Type.rb_any_to_s(receiver)
      end
      ret
    end

    def self.message_and_class(exception)
      message = exception.message.to_s
      if i = message.index("\n")
        "#{message[0...i]} (#{exception.class})#{message[i..-1]}"
      else
        "#{message} (#{exception.class})"
      end
    end

    NO_METHOD_ERROR = Proc.new do |exception|
      format("undefined method `%s' for %s", exception.name, receiver_string(exception))
    end

    NO_LOCAL_VARIABLE_OR_METHOD_ERROR = Proc.new do |exception|
      format("undefined local variable or method `%s' for %s", exception.name, receiver_string(exception))
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
