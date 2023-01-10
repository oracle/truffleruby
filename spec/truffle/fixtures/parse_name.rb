# Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module M
  class C
    def regular_instance_method
    end

    def self.sdef_class_method
    end

    class << self
      def sclass_method
      end
    end
  end
end

def top_method
end

def self.sdef_method_of_main
end

class << self
  def sclass_method_of_main
    i = 0; i += 1 while i < 10_000_000
  end
end

SOME_OBJECT = Object.new
SOME_OBJECT.instance_exec do
  def unknown_def_singleton_method
  end

  def self.unknown_sdef_singleton_method
  end
end

def String.string_class_method
end

[
  M::C.new.method(:regular_instance_method),
  M::C.method(:sdef_class_method),
  M::C.method(:sclass_method),
  method(:top_method),
  method(:sdef_method_of_main),
  method(:sclass_method_of_main),
  SOME_OBJECT.method(:unknown_def_singleton_method),
  SOME_OBJECT.method(:unknown_sdef_singleton_method),
  String.method(:string_class_method),
  1.method(:+), # Java core method
  method(:then), # Ruby core method
].each do |method|
  puts Truffle::Debug.parse_name_of_method(method)
end
