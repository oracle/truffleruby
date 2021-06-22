# frozen_string_literal: true

# Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
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

module Truffle::Internal
  def self.get_data(path, offset)
    file = File.open(path)
    file.seek(offset)

    # flock() does not work on Solaris if the File is not open in write mode
    unless Truffle::Platform.solaris?
      if Truffle::POSIX.respond_to? :flock
        # I think if the file can't be locked then we just silently ignore
        if file.flock(File::LOCK_EX | File::LOCK_NB)
          Truffle::KernelOperations.at_exit true do
            file.flock(File::LOCK_UN)
          end
        end
      end
    end

    file
  end

  def self.load_arguments_from_array_kw_helper(array, kwrest_name, binding)
    array = array.dup

    last_arg = array.pop

    if last_arg.respond_to?(:to_hash)
      kwargs = last_arg.to_hash

      if Primitive.nil? kwargs
        array.push last_arg
        return array
      end

      raise TypeError, "can't convert #{last_arg.class} to Hash (#{last_arg.class}#to_hash gives #{kwargs.class})" unless Primitive.object_kind_of?(kwargs, Hash)

      return array + [kwargs] unless kwargs.keys.any? { |k| Primitive.object_kind_of?(k, Symbol) }

      kwargs.select! do |key, value|
        symbol = Primitive.object_kind_of?(key, Symbol)
        array.push({key => value}) unless symbol
        symbol
      end
    else
      kwargs = {}
    end

    binding.local_variable_set(kwrest_name, kwargs) if kwrest_name
    array
  end

  def self.when_splat(cases, expression)
    cases.any? do |c|
      c === expression
    end
  end

  def self.array_pattern_matches?(pattern, expression)
    return false unless pattern.length == expression.length

    pattern.zip(expression).all? do |a, b|
      a === b
    end
  end

  def self.hash_pattern_matches?(pattern, expression)
    pattern.all? do |key, value|
      expression.has_key?(key) && value === expression.fetch(key)
    end
  end
end
