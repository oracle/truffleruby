# Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
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

# Copyright (c) 2011, Evan Phoenix
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
# * Neither the name of the Evan Phoenix nor the names of its contributors
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

class Thread

  # Utilities

  # Implementation note: ideally, the recursive_objects
  # lookup table would be different per method call.
  # Currently it doesn't cause problems, but if ever
  # a method :foo calls a method :bar which could
  # recurse back to :foo, it could require making
  # the tables independent.
  def self.recursion_guard(obj)
    id = obj.object_id
    objects = current.recursive_objects

    objects[id] = true
    begin
      yield
    ensure
      objects.delete id
    end
  end

  def self.guarding?(obj)
    current.recursive_objects[obj.object_id]
  end

  # detect_recursion will return if there's a recursion
  # on obj (or the pair obj+paired_obj).
  # If there is one, it returns true.
  # Otherwise, it will yield once and return false.
  def self.detect_recursion(obj, paired_obj=nil)
    unless Truffle.invoke_primitive :object_can_contain_object, obj
      yield
      return false
    end

    id = obj.object_id
    pair_id = paired_obj.object_id
    objects = current.recursive_objects

    case objects[id]

    # Default case, we haven't seen +obj+ yet, so we add it and run the block.
    when nil
      objects[id] = pair_id
      begin
        yield
      ensure
        objects.delete id
      end

    # We've seen +obj+ before and it's got multiple paired objects associated
    # with it, so check the pair and yield if there is no recursion.
    when Hash
      return true if objects[id][pair_id]

      objects[id][pair_id] = true
      begin
        yield
      ensure
        objects[id].delete pair_id
      end

    # We've seen +obj+ with one paired object, so check the stored one for
    # recursion.
    #
    # This promotes the value to a Hash since there is another new paired
    # object.
    else
      previous = objects[id]
      return true if previous == pair_id

      objects[id] = { previous => true, pair_id => true }
      begin
        yield
      ensure
        objects[id] = previous
      end
    end

    false
  end
  Truffle::Graal.always_split method(:detect_recursion)

  class InnerRecursionDetected < Exception; end # rubocop:disable Lint/InheritException

  # Similar to detect_recursion, but will short circuit all inner recursion levels
  def self.detect_outermost_recursion(obj, paired_obj=nil, &block)
    rec = current.recursive_objects

    if rec[:__detect_outermost_recursion__]
      if detect_recursion(obj, paired_obj, &block)
        raise InnerRecursionDetected
      end
      false
    else
      rec[:__detect_outermost_recursion__] = true
      begin
        begin
          detect_recursion(obj, paired_obj, &block)
        rescue InnerRecursionDetected
          return true
        end
        return nil
      ensure
        rec.delete :__detect_outermost_recursion__
      end
    end
  end

  # Class methods

  def self.exit
    Thread.current.kill
  end

  def self.kill(thread)
    thread.kill
  end

  def self.stop
    sleep
    nil
  end

  MUTEX_FOR_THREAD_EXCLUSIVE = Mutex.new

  def self.exclusive
    MUTEX_FOR_THREAD_EXCLUSIVE.synchronize { yield }
  end

  def self.handle_interrupt(config, &block)
    unless config.is_a?(Hash) and config.size == 1
      raise ArgumentError, 'unknown mask signature'
    end
    exception, timing = config.first
    Truffle.privately do
      current.handle_interrupt(exception, timing, &block)
    end
  end

  @abort_on_exception = false

  class << self
    attr_accessor :abort_on_exception

    def new(*args, &block)
      thread = Truffle.invoke_primitive(:thread_allocate, self)
      thread.send(:initialize, *args, &block)
      unless Truffle.invoke_primitive(:thread_initialized?, thread)
        Kernel.raise ThreadError, "uninitialized thread - check `#{thread.class}#initialize'"
      end
      thread
    end

    def start(*args, &block)
      Kernel.raise ArgumentError, 'tried to create Proc object without a block' unless block

      thread = Truffle.invoke_primitive(:thread_allocate, self)
      thread.send(:internal_thread_initialize)
      Truffle.invoke_primitive(:thread_initialize, thread, args, block)
      thread
    end
    alias_method :fork, :start
  end

  # Instance methods

  attr_reader :recursive_objects, :randomizer

  def initialize(*args, &block)
    Kernel.raise ThreadError, 'must be called with a block' unless block
    if Truffle.invoke_primitive(:thread_initialized?, self)
      Kernel.raise ThreadError, 'already initialized thread'
    end
    internal_thread_initialize
    Truffle.invoke_primitive(:thread_initialize, self, args, block)
  end

  private def internal_thread_initialize
    @thread_local_variables = {}
    @recursive_objects = {}
    @randomizer = Truffle::Randomizer.new
  end

  def freeze
    Truffle::System.synchronized(self) { @thread_local_variables.freeze }
    super
  end

  def name
    Truffle.primitive :thread_get_name
    Kernel.raise ThreadError, 'Thread#name primitive failed'
  end

  def name=(val)
    unless val.nil?
      val = Truffle::Type.check_null_safe(StringValue(val))
      raise ArgumentError, "ASCII incompatible encoding #{val.encoding.name}" unless val.encoding.ascii_compatible?
      # TODO BJF Aug 27, 2016 Need to rb_str_new_frozen the val here and SET_ANOTHER_THREAD_NAME
    end
    Truffle.invoke_primitive :thread_set_name, self, val
    val
  end

  # Java goes from 1 to 10 (default 5), Ruby from -3 to 3 (default 0)
  # Use Array instead of Hash as Hash needs re-hashing with context pre-initialization.
  #                         -3 -2 -1  0  1  2   3
  PRIORITIES_RUBY_TO_JAVA = [1, 2, 4, 5, 7, 8, 10]
  #                           1   2   3   4  5  6  7  8  9 10
  PRIORITIES_JAVA_TO_RUBY = [-3, -2, -1, -1, 0, 1, 1, 2, 2, 3]

  def priority
    java_priority = Truffle.invoke_primitive :thread_get_priority, self
    PRIORITIES_JAVA_TO_RUBY[java_priority-1]
  end

  def priority=(priority)
    Kernel.raise TypeError, 'priority must be an Integer' unless priority.kind_of? Integer
    priority = -3 if priority < -3
    priority = 3 if priority > 3
    java_priority = PRIORITIES_RUBY_TO_JAVA[priority+3]
    Truffle.invoke_primitive :thread_set_priority, self, java_priority
    priority
  end

  def inspect
    stat = status()
    stat = 'dead' unless stat
    loc = Truffle.invoke_primitive(:thread_source_location, self)
    "#<#{self.class}:0x#{object_id.to_s(16)}@#{loc} #{stat}>"
  end
  alias_method :to_s, :inspect

  def raise(exc=undefined, msg=nil, ctx=nil)
    return self unless alive?

    if undefined.equal? exc
      no_argument = true
      exc         = nil
    end

    if exc.respond_to? :exception
      exc = exc.exception msg
      Kernel.raise TypeError, 'exception class/object expected' unless Exception === exc
    elsif no_argument
      exc = RuntimeError.exception nil
    elsif exc.kind_of? String
      exc = RuntimeError.exception exc
    else
      Kernel.raise TypeError, 'exception class/object expected'
    end

    exc.set_context ctx if ctx
    exc.capture_backtrace!(1) unless exc.backtrace?

    if $DEBUG
      STDERR.puts "Exception: `#{exc.class}' - #{exc.message}"
    end

    if self == Thread.current
      Truffle.invoke_primitive :vm_raise_exception, exc, false
    else
      Truffle.invoke_primitive :thread_raise, self, exc
    end
  end

  def safe_level
    0
  end

  # Fiber-local variables

  private def convert_to_local_name(name)
    if Symbol === name
      name
    elsif String === name
      name.to_sym
    else
      Kernel.raise TypeError, "#{name.inspect} is not a symbol nor a string"
    end
  end

  def [](name)
    var = convert_to_local_name(name)
    Truffle::System.synchronized(self) do
      locals = Truffle.invoke_primitive :thread_get_fiber_locals, self
      Truffle.invoke_primitive :object_ivar_get, locals, var
    end
  end

  def []=(name, value)
    var = convert_to_local_name(name)
    Truffle::System.synchronized(self) do
      Truffle.check_frozen
      locals = Truffle.invoke_primitive :thread_get_fiber_locals, self
      Truffle.invoke_primitive :object_ivar_set, locals, var, value
    end
  end

  def key?(name)
    var = convert_to_local_name(name)
    Truffle::System.synchronized(self) do
      locals = Truffle.invoke_primitive :thread_get_fiber_locals, self
      Truffle.invoke_primitive :object_ivar_defined?, locals, var
    end
  end

  def keys
    Truffle::System.synchronized(self) do
      locals = Truffle.invoke_primitive :thread_get_fiber_locals, self
      locals.instance_variables
    end
  end

  # Thread-local variables

  def thread_variable_get(name)
    var = convert_to_local_name(name)
    Truffle::System.synchronized(self) { @thread_local_variables[var] }
  end

  def thread_variable_set(name, value)
    var = convert_to_local_name(name)
    Truffle::System.synchronized(self) { @thread_local_variables[var] = value }
  end

  def thread_variable?(name)
    var = convert_to_local_name(name)
    Truffle::System.synchronized(self) { @thread_local_variables.key? var }
  end

  def thread_variables
    Truffle::System.synchronized(self) { @thread_local_variables.keys }
  end
end

Thread.current.send :internal_thread_initialize

class ThreadGroup
  def initialize
    @enclosed = false
  end

  def enclose
    @enclosed = true
  end

  def enclosed?
    @enclosed
  end

  def add(thread)
    raise ThreadError, "can't move to the frozen thread group" if self.frozen?
    raise ThreadError, "can't move to the enclosed thread group" if self.enclosed?

    from_tg = thread.group
    return nil unless from_tg
    raise ThreadError, "can't move from the frozen thread group" if from_tg.frozen?
    raise ThreadError, "can't move from the enclosed thread group" if from_tg.enclosed?

    Truffle.invoke_primitive :thread_set_group, thread, self
    self
  end

  def list
    Thread.list.select { |th| th.group == self }
  end

  Default = ThreadGroup.new
end

Truffle.invoke_primitive :thread_set_group, Thread.current, ThreadGroup::Default

class Thread::Backtrace::Location
  def inspect
    to_s.inspect
  end
end

class ConditionVariable

  def wait(mutex, timeout=nil)
    if timeout
      raise ArgumentError 'Timeout must be positive' if timeout < 0
      timeout = timeout * 1_000_000_000
      timeout = Truffle::Type.rb_num2long(timeout)
    end

    if defined?(::Mutex_m) && mutex.kind_of?(Mutex_m)
      raw_mutex = mutex.instance_variable_get(:@_mutex)
    else
      raw_mutex = mutex
    end

    raise ArgumentError, "#{mutex} must be a Mutex or Mutex_m" unless raw_mutex.kind_of? Mutex

    Truffle.invoke_primitive(:condition_variable_wait, self, raw_mutex, timeout)
  end

  def signal
    Truffle.primitive :condition_variable_signal
    raise PrimitiveFailure
  end

  def broadcast
    Truffle.primitive :condition_variable_broadcast
    raise PrimitiveFailure
  end

  def marshal_dump
    raise TypeError, "can't dump #{self.class}"
  end
end

Truffle::KernelOperations.define_hooked_variable(
  :$SAFE,
  -> { Thread.current.safe_level },
  -> level {
    raise SecurityError, 'Setting $SAFE is no longer supported.' unless level == 0
  }
)

Truffle::KernelOperations.define_read_only_global(:$!, -> { Truffle.invoke_primitive(:thread_get_exception) })
Truffle::KernelOperations.define_read_only_global(:$?, -> { Truffle.invoke_primitive(:thread_get_return_code) })

Truffle::KernelOperations.define_hooked_variable(
  :$@,
  -> { $!.backtrace if $! },
  -> value { raise ArgumentError, '$! not set' unless $!
             $!.set_backtrace value }
)
