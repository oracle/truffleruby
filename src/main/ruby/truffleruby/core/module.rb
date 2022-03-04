# frozen_string_literal: true

# Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Copyright (c) 2007-2015, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

##
# Some terminology notes:
#
# [Encloser] The Class or Module inside which this one is defined or, in the
#            event we are at top-level, Object.
#
# [Direct superclass] Whatever is next in the chain of superclass invocations.
#                     This may be either an included Module, a Class or nil.
#
# [Superclass] The real semantic superclass and thus only applies to Class
#              objects.

class Module

  # Copy methods from Kernel that should also be defined on Module like on MRI
  alias_method :==, :==
  alias_method :freeze, :freeze

  def include?(mod)
    if !Primitive.object_kind_of?(mod, Module) or Primitive.object_kind_of?(mod, Class)
      raise TypeError, "wrong argument type #{mod.class} (expected Module)"
    end

    return false if self.equal?(mod)
    ancestors.any? { |m| mod.equal?(m) }
  end

  private def method_added(name)
  end

  private def method_removed(name)
  end

  private def method_undefined(name)
  end

  private def prepended(mod)
  end

  def include(*modules)
    raise ArgumentError, 'wrong number of arguments (given 0, expected 1+)' if modules.empty?
    modules.reverse_each do |mod|
      if !Primitive.object_kind_of?(mod, Module) or Primitive.object_kind_of?(mod, Class)
        raise TypeError, "wrong argument type #{mod.class} (expected Module)"
      end

      mod.__send__ :append_features, self
      mod.__send__ :included, self
    end
    self
  end

  def prepend(*modules)
    raise ArgumentError, 'wrong number of arguments (given 0, expected 1+)' if modules.empty?
    modules.reverse_each do |mod|
      if !Primitive.object_kind_of?(mod, Module) or Primitive.object_kind_of?(mod, Class)
        raise TypeError, "wrong argument type #{mod.class} (expected Module)"
      end

      mod.__send__ :prepend_features, self
      mod.__send__ :prepended, self
    end
    self
  end

  def const_defined?(name, inherit = true)
    Primitive.module_const_defined?(self, name, inherit, true)
  end

  def const_get(name, inherit = true)
    inherit = Primitive.as_boolean(inherit)
    value = Primitive.module_const_get self, name, inherit, true, true
    unless Primitive.undefined?(value)
      return value
    end

    names = name.split('::') # name is always String
    top_level = if !names.empty? && '' == names.first
                  names.shift
                  true
                else
                  false
                end
    raise NameError, "wrong constant name #{name}" if names.empty? || names.include?('')

    res = if top_level
            Object
          else
            self
          end

    names.each_with_index do |s, i|
      if Primitive.object_kind_of?(res, Module)
        res = if !inherit
                Primitive.module_const_get res, s, false, false, true
              elsif i == 0
                Primitive.module_const_get res, s, true, true, true
              else
                Primitive.module_const_get res, s, true, false, true
              end
      else
        raise TypeError, "#{name} does not refer to a class/module"
      end
    end
    res
  end

  private def remove_const(name)
    Primitive.module_remove_const(self, name)
  end

  def self.constants(inherited = undefined)
    if Primitive.undefined?(inherited)
      Object.constants
    else
      super(inherited)
    end
  end
end
