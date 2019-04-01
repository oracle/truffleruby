# frozen_string_literal: true

# Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
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

module Kernel
  def Array(obj)
    ary = Truffle::Type.rb_check_convert_type obj, Array, :to_ary

    return ary if ary

    if array = Truffle::Type.rb_check_convert_type(obj, Array, :to_a)
      array
    else
      [obj]
    end
  end
  module_function :Array

  def Complex(*args)
    Truffle.privately do
      Complex.convert(*args)
    end
  end
  module_function :Complex

  def Float(obj)
    obj = Truffle::Interop.unbox_if_needed(obj)

    case obj
    when String
      value = Truffle.invoke_primitive :string_to_f, obj, true
      raise ArgumentError, 'invalid string for Float' unless value
      value
    when Float
      obj
    when nil
      raise TypeError, "can't convert nil into Float"
    when Complex
      if obj.respond_to?(:imag) && obj.imag.equal?(0)
        Truffle::Type.coerce_to obj, Float, :to_f
      else
        raise RangeError, "can't convert #{obj} into Float"
      end
    else
      Truffle::Type.coerce_to obj, Float, :to_f
    end
  end
  module_function :Float

  def Hash(obj)
    return {} if obj.equal?(nil) || obj == []

    if hash = Truffle::Type.rb_check_convert_type(obj, Hash, :to_hash)
      return hash
    end

    raise TypeError, "can't convert #{Truffle::Type.object_class(obj)} into Hash"
  end
  module_function :Hash

  def Integer(obj, base=nil)
    obj = Truffle::Interop.unbox_if_needed(obj)

    if obj.kind_of? String
      if obj.empty?
        raise ArgumentError, 'invalid value for Integer: (empty string)'
      else
        base ||= 0
        return Truffle.invoke_primitive(:string_to_inum, obj, base, true)
      end
    end

    if base
      raise ArgumentError, 'base is only valid for String values'
    end

    case obj
    when Integer
      obj
    when Float
      if obj.nan? or obj.infinite?
        raise FloatDomainError, "unable to coerce #{obj} to Integer"
      else
        obj.to_int
      end
    when NilClass
      raise TypeError, "can't convert nil into Integer"
    else
      # Can't use coerce_to or try_convert because I think there is an
      # MRI bug here where it will return the value without checking
      # the return type.
      if obj.respond_to? :to_int
        if val = obj.to_int
          return val
        end
      end

      Truffle::Type.coerce_to obj, Integer, :to_i
    end
  end
  module_function :Integer

  def Rational(a, b = 1)
    Truffle.privately do
      Rational.convert a, b
    end
  end
  module_function :Rational

  def String(obj)
    str = Truffle::Type.rb_check_convert_type(obj, String, :to_str)
    if str.nil?
      str = Truffle::Type.rb_convert_type(obj, String, :to_s)
    end
    str
  end
  module_function :String

  ##
  # MRI uses a macro named StringValue which has essentially the same
  # semantics as obj.coerce_to(String, :to_str), but rather than using that
  # long construction everywhere, we define a private method similar to
  # String().
  #
  # Another possibility would be to change String() as follows:
  #
  #   String(obj, sym=:to_s)
  #
  # and use String(obj, :to_str) instead of StringValue(obj)

  def StringValue(obj)
    Truffle::Type.coerce_to obj, String, :to_str
  end
  module_function :StringValue

  def `(str) #`
    str = StringValue(str) unless str.kind_of?(String)

    io = IO.popen(str)
    output = io.read
    io.close

    Truffle::Type.external_string output
  end
  module_function :` # `

  def =~(other)
    nil
  end

  def !~(other)
    r = self =~ other ? false : true
    Truffle::RegexpOperations.set_last_match($~, Truffle.invoke_primitive(:caller_binding))
    r
  end

  def itself
    self
  end

  def abort(msg=nil)
    Process.abort msg
  end
  module_function :abort

  def autoload(name, file)
    nesting = Truffle.invoke_primitive(:caller_binding).eval('Module.nesting')
    mod = nesting.first || (Kernel.equal?(self) ? Kernel : Object)
    if mod.equal?(self)
      super(name, file) # Avoid recursion
    else
      mod.autoload(name, file)
    end
  end
  module_function :autoload

  def autoload?(name)
    if Kernel.equal?(self)
      super(name) # Avoid recursion
    else
      Object.autoload?(name)
    end
  end
  module_function :autoload?

  alias_method :iterator?, :block_given?

  def define_singleton_method(*args, &block)
    Truffle.privately do
      singleton_class.define_method(*args, &block)
    end
  end

  def display(port=$>)
    port.write self
  end

  def eval(str, a_binding=nil, file=nil, line=nil)
    file = '(eval)' unless file
    line = 1 unless line
    str = str.to_str unless str.class == String
    file = file.to_str unless file.class == String
    line = line.to_i unless line.is_a?(Integer)
    unless a_binding
      receiver = self
      a_binding = Truffle.invoke_primitive(:caller_binding)
    else
      unless a_binding.class == Binding
        raise TypeError, "Wrong argument type #{a_binding.class} (expected binding)"
      end
      receiver = a_binding.receiver
    end

    Truffle.invoke_primitive(:kernel_eval, receiver, str, a_binding, file, line)
  end
  module_function :eval

  # It is important that eval is always cloned so that the primitive
  # inside can be specialised efficiently.
  Truffle::Graal.always_split(method(:eval))

  def exec(*args)
    Process.exec(*args)
  end
  module_function :exec

  def exit(code=0)
    Process.exit(code)
  end
  module_function :exit

  def exit!(code=1)
    Process.exit!(code)
  end
  module_function :exit!

  def extend(*modules)
    raise ArgumentError, 'wrong number of arguments (0 for 1+)' if modules.empty?

    modules.reverse_each do |mod|
      if !mod.kind_of?(Module) or mod.kind_of?(Class)
        raise TypeError, "wrong argument type #{mod.class} (expected Module)"
      end

      Truffle.privately do
        mod.extend_object self
      end

      Truffle.privately do
        mod.extended self
      end
    end
    self
  end

  def getc
    $stdin.getc
  end
  module_function :getc

  def gets(*args)
    line = ARGF.gets(*args)
    Truffle::IOOperations.set_last_line(line, Truffle.invoke_primitive(:caller_binding)) if line
    line
  end
  module_function :gets

  def inspect
    prefix = "#<#{self.class}:0x#{self.__id__.to_s(16)}"

    # The protocol here seems odd, but it's to match MRI.
    #
    # MRI side-calls to the C function that implements Kernel#to_s. If that
    # method is overridden, the new Ruby method is never called. So, we inline
    # the code for Kernel#to_s here because we simply dispatch to Ruby
    # methods.
    ivars = Truffle.invoke_primitive :object_ivars, self

    if ivars.empty?
      return Truffle::Type.infect "#{prefix}>", self
    end

    # Otherwise, if it's already been inspected, return the ...
    return "#{prefix} ...>" if Thread.guarding? self

    # Otherwise, gather the ivars and show them.
    parts = []

    Thread.recursion_guard self do
      ivars.each do |var|
        value = Truffle.invoke_primitive :object_ivar_get, self, var
        parts << "#{var}=#{value.inspect}"
      end
    end

    if parts.empty?
      str = "#{prefix}>"
    else
      str = "#{prefix} #{parts.join(', ')}>"
    end

    Truffle::Type.infect(str, self)

    str
  end

  def load(filename, wrap = false)
    filename = Truffle::Type.coerce_to_path filename

    # load absolute path
    if filename.start_with? File::SEPARATOR
      return Truffle::KernelOperations.load File.expand_path(filename), wrap
    end

    # if path starts with . only try relative paths
    if filename.start_with? '.'
      return Truffle::KernelOperations.load File.expand_path(filename), wrap
    end

    # try to resolve with current working directory
    if File.exist? filename
      return Truffle::KernelOperations.load File.expand_path(filename), wrap
    end

    # try to find relative path in $LOAD_PATH
    $LOAD_PATH.each do |dir|
      path = File.expand_path(File.join(dir, filename))
      if File.exist? path
        return Truffle::KernelOperations.load path, wrap
      end
    end

    # file not found trigger an error
    Truffle::KernelOperations.load filename, wrap
  end
  module_function :load

  def local_variables
    Truffle.invoke_primitive(:caller_binding).local_variables
  end
  module_function :local_variables
  Truffle::Graal.always_split(method(:local_variables))

  def loop
    return to_enum(:loop) { Float::INFINITY } unless block_given?

    begin
      while true # rubocop:disable Lint/LiteralAsCondition
        yield
      end
    rescue StopIteration => si
      si.result
    end
  end
  module_function :loop

  def open(obj, *rest, &block)
    if obj.respond_to?(:to_open)
      obj = obj.to_open(*rest)

      if block_given?
        return yield(obj)
      else
        return obj
      end
    end

    path = Truffle::Type.coerce_to_path obj

    if path.kind_of? String and path.start_with? '|'
      return IO.popen(path[1..-1], *rest, &block)
    end

    File.open(path, *rest, &block)
  end
  module_function :open

  # Kernel#p is in post.rb

  def print(*args)
    args.each do |obj|
      $stdout.write obj.to_s
    end
    nil
  end
  module_function :print

  def putc(int)
    $stdout.putc(int)
  end
  module_function :putc

  def puts(*a)
    $stdout.puts(*a)
    nil
  end
  module_function :puts

  def rand(limit=0)
    if limit == 0
      return Thread.current.randomizer.random_float
    end

    if limit.kind_of?(Range)
      return Thread.current.randomizer.random(limit)
    else
      limit = Integer(limit).abs

      if limit == 0
        Thread.current.randomizer.random_float
      else
        Thread.current.randomizer.random_integer(limit - 1)
      end
    end
  end
  module_function :rand

  def readline(sep=$/)
    ARGF.readline(sep)
  end
  module_function :readline

  def readlines(sep=$/)
    ARGF.readlines(sep)
  end
  module_function :readlines

  def select(*args)
    IO.select(*args)
  end
  module_function :select

  def srand(seed=undefined)
    if undefined.equal? seed
      seed = Thread.current.randomizer.generate_seed
    end

    seed = Truffle::Type.coerce_to seed, Integer, :to_int
    Thread.current.randomizer.swap_seed seed
  end
  module_function :srand

  def tap
    yield self
    self
  end

  def yield_self
    if block_given?
      yield self
    else
      [self].to_enum { 1 }
    end
  end

  def test(cmd, file1, file2=nil)
    case cmd
    when ?d
      File.directory? file1
    when ?e
      File.exist? file1
    when ?f
      File.file? file1
    when ?l
      File.symlink? file1
    when ?r
      File.readable? file1
    when ?R
      File.readable_real? file1
    when ?w
      File.writable? file1
    when ?W
      File.writable_real? file1
    when ?A
      File.atime file1
    when ?C
      File.ctime file1
    when ?M
      File.mtime file1
    else
      raise NotImplementedError, "command ?#{cmd.chr} not implemented"
    end
  end
  module_function :test

  def to_enum(method=:each, *args, &block)
    Enumerator.new(self, method, *args).tap do |enum|
      Truffle.privately { enum.size = block } if block_given?
    end
  end
  alias_method :enum_for, :to_enum

  def trap(sig, prc=nil, &block)
    Signal.trap(sig, prc, &block)
  end
  module_function :trap

  def spawn(*args)
    Process.spawn(*args)
  end
  module_function :spawn

  def syscall(*args)
    raise NotImplementedError
  end
  module_function :syscall

  def system(*args)
    begin
      pid = Process.spawn(*args)
    rescue SystemCallError
      return nil
    end

    Process.waitpid pid
    $?.exitstatus == 0
  end
  module_function :system

  def trace_var(name, cmd = nil, &block)
    if !cmd && !block
      raise ArgumentError,
        'The 2nd argument should be a Proc/String, alternatively use a block'
    end

    # Truffle: not yet implemented
  end
  module_function :trace_var

  def untrace_var(name, cmd=undefined)
    # Truffle: not yet implemented
  end
  module_function :untrace_var

  def warn(*messages)
    Truffle::IOOperations.puts($stderr, *messages) if !$VERBOSE.nil? && !messages.empty?
    nil
  end
  module_function :warn

  def raise(exc=undefined, msg=undefined, ctx=nil)
    Truffle::KernelOperations.internal_raise exc, msg, ctx, false
  end
  module_function :raise

  alias_method :fail, :raise
  module_function :fail

  def __dir__
    path = caller_locations(1, 1).first.absolute_path
    File.dirname(path)
  end
  module_function :__dir__

  def printf(*args)
    print sprintf(*args)
  end
  module_function :printf

  private def pp(*args)
    require 'pp'
    pp(*args)
  end

  alias_method :trust, :untaint
  alias_method :untrust, :taint
  alias_method :untrusted?, :tainted?

  def caller(start = 1, limit = nil)
    args = if start.is_a? Range
             [start.begin + 1, start.size]
           elsif limit.nil?
             [start + 1]
           else
             [start + 1, limit]
           end
    Kernel.caller_locations(*args).map(&:to_s)
  end
  module_function :caller

  def at_exit(&block)
    Truffle::KernelOperations.at_exit false, &block
  end
  module_function :at_exit

  def fork
    raise NotImplementedError, 'fork is not available'
  end
  module_function :fork
  Truffle.invoke_primitive :method_unimplement, method(:fork)
  Truffle.invoke_primitive :method_unimplement, nil.method(:fork)

end
