# frozen_string_literal: true

# Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module ExceptionOperations
    def self.build_exception_for_raise(exc, msg)
      if Primitive.undefined? exc
        ::RuntimeError.exception ''
      elsif exc.respond_to? :exception
        if Primitive.undefined? msg
          exc = exc.exception
        else
          exc = exc.exception msg
        end

        exception_class_object_expected! unless Primitive.object_kind_of?(exc, ::Exception)
        exc
      elsif Primitive.object_kind_of?(exc, ::String)
        ::RuntimeError.exception exc
      else
        exception_class_object_expected!
      end
    end

    def self.make_exception(args)
      case args.size
      when 0
        nil
      when 1
        converted = Truffle::Type.rb_check_convert_type(args[0], String, :to_str)
        return RuntimeError.new(converted) unless Primitive.nil?(converted)
        call_exception(args[0])
      when 2
        call_exception(args[0], args[1])
      when 3
        exc = call_exception(args[0], args[1])
        exc.set_backtrace(args[2])
        exc
      else
        Truffle::Type.check_arity(args.size, 0, 3)
      end
    end

    def self.call_exception(exc, *args)
      res = Truffle::Type.check_funcall(exc, :exception, args)
      raise TypeError, 'exception class/object expected' if Primitive.undefined?(res) || !Primitive.object_kind_of?(res, Exception)
      res
    end

    # Avoid using #raise here to prevent infinite recursion
    def self.exception_class_object_expected!
      exc = ::TypeError.new('exception class/object expected')
      Primitive.exception_capture_backtrace(exc, 1)

      show_exception_for_debug(exc, 2) if $DEBUG

      Primitive.vm_raise_exception exc
    end

    def self.show_exception_for_debug(exc, uplevel)
      STDERR.puts "Exception: `#{exc.class}' at #{caller(uplevel + 1, 1)[0]} - #{exc.message}\n"
    end

    def self.class_name(receiver)
      Primitive.object_class(receiver).name
    end

    # MRI: name_err_mesg_to_str
    def self.receiver_string(receiver)
      ret = begin
        if Primitive.object_respond_to?(receiver, :inspect, false)
          Truffle::Type.rb_inspect(receiver)
        else
          nil
        end
      rescue Exception # rubocop:disable Lint/RescueException
        nil
      end
      ret = Primitive.rb_any_to_s(receiver) unless ret && ret.bytesize <= 65
      if ret.start_with?('#')
        ret
      else
        "#{ret}:#{class_name(receiver)}"
      end
    end

    def self.message_and_class(exception, highlight)
      message = StringValue exception.message.to_s

      klass = exception.class.to_s
      if Primitive.object_kind_of?(exception, Polyglot::ForeignException) and
          Truffle::Interop.has_meta_object?(exception)
        klass = "#{klass}: #{Truffle::Interop.meta_qualified_name Truffle::Interop.meta_object(exception)}"
      end

      if highlight
        highlighted_class = " (\e[1;4m#{klass}\e[m\e[1m)"
        if message.include?("\n")
          first = true
          result = +''
          message.each_line do |line|
            if first
              first = false
              result << "\e[1m#{line.chomp}#{highlighted_class}\e[m"
            else
              result << "\n\e[1m#{line.chomp}\e[m"
            end
          end
          result
        else
          "\e[1m#{message}#{highlighted_class}\e[m"
        end
      else
        if i = message.index("\n")
          "#{message[0...i]} (#{klass})#{message[i..-1]}"
        else
          "#{message} (#{klass})"
        end
      end
    end

    def self.full_message(exception, highlight, order)
      highlight = if highlight.equal?(nil)
                    Exception.to_tty?
                  else
                    raise ArgumentError, "expected true of false as highlight: #{highlight}" unless highlight.equal?(true) || highlight.equal?(false)
                    !highlight.equal?(false)
                  end

      raise ArgumentError, "expected :top or :bottom as order: #{order}" unless order.equal?(:top) || order.equal?(:bottom)
      reverse = !order.equal?(:top)

      result = ''.b
      bt = exception.backtrace || caller(2)
      if reverse
        traceback_msg = if highlight
                          "\e[1mTraceback\e[m (most recent call last):\n"
                        else
                          "Traceback (most recent call last):\n"
                        end
        result << traceback_msg
        append_causes(result, exception, {}.compare_by_identity, reverse, highlight)
        backtrace_message = backtrace_message(highlight, reverse, bt, exception)
        if backtrace_message.empty?
          result << message_and_class(exception, highlight)
        else
          result << backtrace_message
        end
      else
        backtrace_message = backtrace_message(highlight, reverse, bt, exception)
        if backtrace_message.empty?
          result << message_and_class(exception, highlight)
        else
          result << backtrace_message
        end
        append_causes(result, exception, {}.compare_by_identity, reverse, highlight)
      end
      result
    end

    def self.backtrace_message(highlight, reverse, bt, exc)
      message = message_and_class(exc, highlight)
      message = message.end_with?("\n") ? message : "#{message}\n"
      return '' if Primitive.nil?(bt) || bt.empty?
      limit = Primitive.exception_backtrace_limit
      limit = limit >= 0 && bt.size - 1 >= limit + 2 ? limit : -1
      result = if reverse
                 bt[1..limit].reverse.map do |l|
                   "\tfrom #{l}\n"
                 end.join
               else
                 "#{bt[0]}: #{message}" + bt[1..limit].map do |l|
                   "\tfrom #{l}\n"
                 end.join
               end
      result + (limit != -1 ? "\t ... #{bt.size - limit - 1} levels...\n" : '') + (reverse ? "#{bt[0]}: #{message}" : '')
    end

    def self.backtrace?(exc)
      result = Primitive.exception_backtrace? exc
      if Primitive.undefined?(result)
        exc.backtrace ? true : false
      else
        result
      end
    end

    def self.append_causes(str, err, causes, reverse, highlight)
      if !Primitive.nil?(err.cause) && Exception === err.cause && !causes.has_key?(err.cause)
        causes[err.cause] = true
        if reverse
          append_causes(str, err.cause, causes, reverse, highlight)
          backtrace_message = backtrace_message(highlight, reverse, err.cause.backtrace, err.cause)
          if backtrace_message.empty?
            str << message_and_class(err, highlight)
          else
            str << backtrace_message
          end
        else
          backtrace_message = backtrace_message(highlight, reverse, err.cause.backtrace, err.cause)
          if backtrace_message.empty?
            str << message_and_class(err, highlight)
          else
            str << backtrace_message
          end
          append_causes(str, err.cause, causes, reverse, highlight)
        end
      end
    end

    IMPLICIT_CONVERSION_METHODS = [:to_int, :to_ary, :to_str, :to_sym, :to_hash, :to_proc, :to_io]

    def self.conversion_error_message(meth, obj, cls)
      message = IMPLICIT_CONVERSION_METHODS.include?(meth) ? 'no implicit conversion of' : "can't convert"
      type_name = to_class_name(obj)
      "#{message} #{type_name} into #{cls}"
    end

    def self.to_class_name(val)
      case val
      when nil
        'nil'
      when true
        'true'
      when false
        'false'
      else
        Primitive.object_class(val).name
      end
    end

    def self.get_formatted_backtrace(exc)
      full_message(exc, nil, :top)
    end

    def self.comparison_error_message(x, y)
      y_classname = if Truffle::Type.is_special_const?(y)
                      y.inspect
                    else
                      y.class
                    end
      "comparison of #{x.class} with #{y_classname} failed"
    end

    NO_METHOD_ERROR = Proc.new do |exception|
      format("undefined method `%s' for %s", exception.name, receiver_string(exception.receiver))
    end

    NO_LOCAL_VARIABLE_OR_METHOD_ERROR = Proc.new do |exception|
      format("undefined local variable or method `%s' for %s", exception.name, receiver_string(exception.receiver))
    end

    PRIVATE_METHOD_ERROR = Proc.new do |exception|
      format("private method `%s' called for %s", exception.name, receiver_string(exception.receiver))
    end

    PROTECTED_METHOD_ERROR = Proc.new do |exception|
      format("protected method `%s' called for %s", exception.name, receiver_string(exception.receiver))
    end

    SUPER_METHOD_ERROR = Proc.new do |exception|
      format("super: no superclass method `%s'", exception.name)
    end

    def self.format_errno_error_message(errno_description, errno, extra_message)
      if Primitive.nil? errno_description
        "unknown error: #{errno} - #{extra_message}"
      else
        "#{errno_description}#{extra_message}"
      end
    end
  end
end
