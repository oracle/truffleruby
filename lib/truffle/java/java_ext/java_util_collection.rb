# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

java.util.Collection

module ::Java::JavaUtil::Collection
  def length
    size
  end

  def include?(an_item)
    self.contains(an_item)
  end

  def first(count=nil)
    return nil if empty?
    iter = self.iterator
    if count == nil
      iter.next
    else
      res = []
      i = 0
      while i < count && iter.has_next
        res << iter.next
      end
      res
    end
  end

  alias :member? :include?

  alias :ruby_first :first

  def <<(an_item)
    add(an_item)
  end

  def +(another)
    another.each { |x| self << x }
  end

  def -(another)
    another.each { |x| self.remove x }
  end

  def join(sep=nil)
    to_a.join(sep)
  end
end
