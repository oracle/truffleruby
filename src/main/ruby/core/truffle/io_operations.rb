# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module IOOperations
    def self.last_line(a_binding)
      Truffle::KernelOperations.frame_local_variable_get(:$_, a_binding)
    end

    def self.set_last_line(value, a_binding)
      Truffle::KernelOperations.frame_local_variable_set(:$_, a_binding, value)
    end

    def self.print(io, args, last_line_binding)
      if args.empty?
        raise 'last_line_binding is required' if last_line_binding.nil?
        io.write Truffle::IOOperations.last_line(last_line_binding).to_s
      else
        args.each { |o| io.write o.to_s }
      end

      io.write $\.to_s
      nil
    end

    def self.puts(io, *args)
      if args.empty?
        io.write DEFAULT_RECORD_SEPARATOR
      else
        args.each do |arg|
          if arg.equal? nil
            str = ''
          elsif arg.kind_of?(Array)
            if Thread.guarding? arg
              str = '[...]'
            else
              Thread.recursion_guard arg do
                arg.each do |a|
                  puts(io, a)
                end
              end
            end
          else
            str = arg.to_s
          end

          if str
            # Truffle: write the string + record separator (\n) atomically so multithreaded #puts is bearable
            unless str.end_with?(DEFAULT_RECORD_SEPARATOR)
              str += DEFAULT_RECORD_SEPARATOR
            end
            io.write str
          end
        end
      end

      nil
    end

    Truffle::Graal.always_split(method(:last_line))
    Truffle::Graal.always_split(method(:set_last_line))
  end
end
