# frozen_string_literal: true

# Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Copyright (c) 2007-2015 Evan Phoenix and contributors
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

class Module
  # :internal:
  # Basic version of #include used in core
  # Redefined in core/module.rb
  def include(mod)
    mod.__send__ :append_features, self
    mod.__send__ :included, self
    self
  end

  private def ruby2_keywords(*methods)
    methods.each do |name|
      method = instance_method(name)
      kinds = method.parameters.map(&:first)
      if !kinds.include?(:rest)
        warn "Skipping set of ruby2_keywords flag for #{name} (method does not accept argument splat)", uplevel: 1
      elsif kinds.include?(:key) || kinds.include?(:keyreq) || kinds.include?(:keyrest)
        warn "Skipping set of ruby2_keywords flag for #{name} (method accepts keywords)", uplevel: 1
      end
    end
    warn "ruby2_keywords(#{methods.join(', ')}) was ignored", uplevel: 1 if $DEBUG
    nil
  end
end

class Proc
  def ruby2_keywords
    kinds = parameters.map(&:first)
    if !kinds.include?(:rest)
      warn 'Skipping set of ruby2_keywords flag for proc (proc does not accept argument splat)', uplevel: 1
    elsif kinds.include?(:key) || kinds.include?(:keyreq) || kinds.include?(:keyrest)
      warn 'Skipping set of ruby2_keywords flag for proc (proc accepts keywords)', uplevel: 1
    end
    warn 'Proc#ruby2_keywords was ignored', uplevel: 1 if $DEBUG
    self
  end
end

module Kernel
  def extend(mod)
    mod.__send__ :extend_object, self
    mod.__send__ :extended, self
    self
  end
  # Methods from BasicObject with a different name
  alias_method :eql?, :equal?
  alias_method :object_id, :__id__
  alias_method :send, :__send__
end

class Symbol
  def to_sym
    self
  end
end

module Truffle::Boot
  if preinitializing?
    TO_RUN_AT_INIT = []

    def self.delay(&block)
      TO_RUN_AT_INIT << block
    end

    def self.redo(&block)
      yield
      TO_RUN_AT_INIT << block
    end
  else
    def self.delay
      yield
    end

    def self.redo
      yield
    end
  end

  # Return default if pre-initializing. Should only be used in rare cases where
  # using the default during pre-initialization does not change semantics.
  def self.get_option_or_default(name, default)
    if preinitializing?
      default
    else
      get_option(name)
    end
  end

  # To load Java classes eagerly, so a real StackOverflowError will not result in
  # 'NoClassDefFoundError: Could not initialize class SomeExceptionRelatedClass'
  def self.throw_stack_overflow_error
    Primitive.vm_stack_overflow_error_to_init_classes
  end
end

unless TruffleRuby.native?
  begin
    Truffle::Boot.throw_stack_overflow_error
  rescue SystemStackError
    nil # expected
  end
end
