# Copyright (c) 2016 Benoit Daloze
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

require 'thread'

Thread.abort_on_exception = true

class Actor
  SYNC = Mutex.new
  ALL = []

  def self.await_all_actors
    SYNC.synchronize do
      while thread = ALL.pop
        thread.join
      end
    end
  end

  def self.inherited(subclass)
    subclass.class_exec {
      # avoid polymorphism by forcing specialization of these methods on the subclass
      def send!(message)
        @mailbox << message
      end

      def start!
        Thread.new {
          begin
            while message = @mailbox.pop
              if :exit == process(message)
                break
              end
            end
          rescue Exception => e
            Thread.main.raise e
          end
        }
      end
    }
  end

  def self.new(*args)
    actor = super(*args)
    SYNC.synchronize do
      actor.instance_variable_set :@mailbox, Queue.new
      ALL << actor.start!
    end
    actor
  end
end

class MyRandom
  def initialize(seed)
    @seed = seed
  end

  def next
    @seed = ((@seed * 1309) + 13849) & 65535
  end

  def next_int bound
    # fair enough for low values of bound
    self.next % bound
  end
end

##### ARGV parsing #####
ARGV.each_with_index { |arg, i|
  begin
    n = Integer(arg)
  rescue ArgumentError
    ARGV.shift(i)
    break
  end
  ARGS[i] = n
}
