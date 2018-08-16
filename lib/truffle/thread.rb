# Truffle: Last version of lib/thread.rb in MRI @ r42801 (324df61e).

#
#               thread.rb - thread support classes
#                       by Yukihiro Matsumoto <matz@netlab.co.jp>
#
# Copyright (C) 2001  Yukihiro Matsumoto
# Copyright (C) 2000  Network Applied Communication Laboratory, Inc.
# Copyright (C) 2000  Information-technology Promotion Agency, Japan
#

unless defined? Thread
  raise 'Thread not available for this ruby interpreter'
end

unless defined? ThreadError
  class ThreadError < StandardError
  end
end

if $DEBUG
  Thread.abort_on_exception = true
end

#
# ConditionVariable objects augment class Mutex. Using condition variables,
# it is possible to suspend while in the middle of a critical section until a
# resource becomes available.
#
# Example:
#
#   require 'thread'
#
#   mutex = Mutex.new
#   resource = ConditionVariable.new
#
#   a = Thread.new {
#     mutex.synchronize {
#       # Thread 'a' now needs the resource
#       resource.wait(mutex)
#       # 'a' can now have the resource
#     }
#   }
#
#   b = Thread.new {
#     mutex.synchronize {
#       # Thread 'b' has finished using the resource
#       resource.signal
#     }
#   }
#
class ConditionVariable
  #
  # Creates a new ConditionVariable
  #
  def initialize
    Truffle.primitive :condition_variable_initialize
    raise PrimitiveFailure
  end

  #
  # Releases the lock held in +mutex+ and waits; reacquires the lock on wakeup.
  #
  # If +timeout+ is given, this method returns after +timeout+ seconds passed,
  # even if no other thread has signaled.
  #
  def wait(mutex, timeout=nil)
    Truffle.primitive :condition_variable_wait
    timeout = Truffle::Type.rb_num2long(timeout) if timeout
    raise ArgumentError, "#{mutex} must be a Mutex" unless mutex.kind_of? Mutex
    wait(mutex, timeout)
  end

  #
  # Wakes up the first thread in line waiting for this lock.
  #
  def signal
    Truffle.primitive :condition_variable_signal
    raise PrimitiveFailure
  end

  #
  # Wakes up all threads waiting for this lock.
  #
  def broadcast
    Truffle.primitive :condition_variable_broadcast
    raise PrimitiveFailure
  end

  # Truffle: define marshal_dump as MRI tests expect it
  def marshal_dump
    raise TypeError, "can't dump #{self.class}"
  end
end

# Truffle: Queue and SizedQueue are defined in Java
