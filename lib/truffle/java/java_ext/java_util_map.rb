# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

java.util.Map

module ::Java::JavaUtil::Map
  include Enumerable

  attr_accessor :default
  attr_accessor :default_proc

  def length
    self.size
  end

  def to_h
    iter = self.entry_set.iterator
    res = Hash.new
    while (iter.has_next)
      entry = iter.next
      res[entry.key] = entry.value
    end
  end

  def to_a
    iter = self.entry_set.iterator
    res = Array.new(self.size)
    i = 0
    while (iter.has_next)
      entry = iter.next
      res[i] = [entry.key, entry.value]
      i += 1
    end
  end

  alias :to_hash :to_h

  def to_proc
    h = self
    lambda { |x| h[x] }
  end

  def rehash
  end

  def []=(key, val)
    self.put(key, val)
  end

  alias :store :[]=

  def [](key)
    self.get(key)
  end

  def <(another)
    self.size < another.size && ::Java::JavaUtil::Map.a_contains_b(another, self)

  end

  def <=(another)
    self.size <= another.size && ::Java::JavaUtil::Map.a_contains_b(another, self)
  end

  def >(another)
    !(self <= another)
  end

  def >=(another)
    !(self < another)
  end

  def self.a_contains_b(a, b)
    res = true
    entries = b.entry_set
    while(res && entries.has_next)
      e = entries.next
      res = res & (e.value == a[e.key])
    end
  end

  def fetch(key, default=nil)
    return get(key) if contains_key(key)
    raise KeyError, "key not found: #{key}" unless (default || block_given?)
    default = yield if block_given?
    old_val = pit_if_absent(key, default)
    old_val || default
  end

  def has_key?(key)
    contains_key(key)
  end

  alias :key? :has_key?
  alias :include? :has_key?
  alias :member :has_key?

  def has_value?(val)
    contains_value(val)
  end

  alias :value :has_value?

  def each
    entries = self.entry_set.iterator
    while entries.has_next
      e = entries.next
      yield [ e.key, e.value ]
    end
  end

  alias :each_pair :each

  def each_key
    keys = self.key_set.iterator
    while keys.has_next
      yield keys.next
    end
  end

  def each_value
    values = self.values
    while values.has_next
      yield values.next
    end
  end

  def keep_if
    return to_enum(:keep_if) { size } unless block_given?

    entries = self.entry_set.iterator
    while (entries.has_next)
      e = entries.next
      entries.remove unless yield(e.key, e.value)
    end
    self
  end

  def select
    return to_enum(:select) { size } unless block_given?

    selected = Hash.allocate

    each_pair do |key,value|
      if yield(key, value)
        selected[key] = value
      end
    end

    selected
  end

  def select!
    return to_enum(:select!) { size } unless block_given?

    entries = self.entry_set.iterator
    deleted = false
    while (entries.has_next)
      e = entries.next
      unless yield(e.key, e.value)
        entries.remove
        deleted = true
      end
    end
    return nil unless deleted
    self
  end

  def index(value)
    each_pair do |k,v|
      return k if v == value
    end
    nil
  end

  alias_method :key, :index

  def keys
    ary = []
    keys = self.key_set.iterator
    while (keys.has_next)
      ary << keys.next
    end
    ary
  end

  def shift
    entries = self.entry_set.iterator
    if entries.has_next
      e = entries.next
      entries.remove
      [e.key, e.value]
    else
      nil
    end
  end

  def delete(a_key)
    if has_key?(a_key)
      remove(a_key)
    elsif block_given?
      yield(a_key)
    else
      nil
    end
  end

end
