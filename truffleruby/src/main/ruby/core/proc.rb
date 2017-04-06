# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

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

class Proc
  attr_accessor :block
  attr_accessor :bound_method
  attr_accessor :ruby_method

  alias_method :===, :call

  def curry(curried_arity = nil)
    if lambda? && curried_arity

      if arity >= 0 && curried_arity != arity
        raise ArgumentError, "Wrong number of arguments (#{curried_arity} for #{arity})"
      end

      if arity < 0
        max_args = 0
        min_args = 0
        has_rest = false

        parameters.each do |p|
          type = p.first
          min_args += 1 if type == :req
          max_args += 1 if type != :block
          has_rest = true if type == :rest
        end

        if (curried_arity < min_args) ||
           (!has_rest && (curried_arity > max_args))
          if has_rest
            raise ArgumentError, 'Wrong number of arguments ' +
                                 "(given #{curried_arity}, expected #{min_args}+)"
          else
            raise ArgumentError, 'Wrong number of arguments ' +
                                 "(given #{curried_arity}, expected #{min_args}..#{max_args})"
          end
        end
      end
    end

    m = Rubinius::Mirror.reflect self
    f = m.curry self, [], arity

    def f.binding
      raise ArgumentError, 'cannot create binding from curried proc'
    end

    # TODO: set the procs parameters to be :rest to match spec. DMM - 2017-01-19
    # TODO: set the source location to be nil. DMM - 2017-01-19

    f
  end

  def to_s
    sl = source_location
    return super if sl.nil?
    file, line = sl

    l = ' (lambda)' if lambda?
    if file and line
      "#<#{self.class}:0x#{self.object_id.to_s(16)}@#{file}:#{line}#{l}>"
    else
      "#<#{self.class}:0x#{self.object_id.to_s(16)}#{l}>"
    end
  end

  alias_method :inspect, :to_s

  def to_proc
    self
  end
end
