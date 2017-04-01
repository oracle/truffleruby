# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

java.util.List

module ::Java::JavaUtil::List

  def determine_range(a_range)
    first = a_range.first
    last = a_range.last
    last += size if last < 0
    first += size if first < 0
    last += 1 unless a_range.exclude_end?
    [first, last]
  end

  def [](start, length=nil)
    size = self.size
    if start.kind_of?(Range)
      first, last = determine_range(start)
      return nil if first < 0 || first >= size
      sub_list(first, last)
    else
      start += size if start < 0
      return nil if start < 0 || start >= size
      if length == nil
        get(start)
      else
        return nil if length < 0
        last = length + start
        last = size if last > size
        sub_list(start, last)
      end
    end
  end

  def []=(index, value)
    size = self.size
    if index.kind_of?(Range)
      first, last = determine_range(index)
      if first < size
        (first...[last, size].min).each { |_| remove(first) } # Remove elements in list
      else
        (size...first).each { add(nil) } # Pad out the list
      end
      add(first, value)
    else
      index += size if index < 0
      if index >= size # Pad out the list
        (0...(index - size)).each { add(nil) }
        add(value)
      else
        set(index, value)
      end
    end
    value
  end

  def first(count=nil)
    if count == nil
      self[0]
    else
      [0,count].to_a
    end
  end

  def last(count=nil)
    if count == nil
      [-1]
    else
      [-count, count].to_a
    end
  end

  alias :ruby_first :first
  alias :ruby_last :last

  def index(obj = nil)
    if obj == nil
      return to_enum(:index, self) unless block_given?

      i = 0
      iter = self.iterator
      while iter.has_next
        return i if yield iter.next
        i += 1
      end
      nil
    else
      i = 0
      iter = self.iterator
      while iter.has_next
        return i if iter.next == obj
        i += 1
      end
      nil
    end
  end

  def rindex(obj = nil)
    if obj == nil
      return to_enum(:index, self) unless block_given?

      i = size
      iter = self.list_iterator(self.size)
      while iter.has_previous
        i -= 1
        return i if yield iter.previous
      end
      nil
    else
      i = size
      iter = self.list_iterator(self.size)
      while iter.has_previous
        i -= 1
        return i if iter.previous == obj
      end
      nil
    end
  end

  def to_ary
    to_a
  end

  define_method(:ruby_sort, Enumerable.instance_method(:sort))

  #TODO Implement sort! using the underlying Java sort. DMM - 2017-02-14
end
