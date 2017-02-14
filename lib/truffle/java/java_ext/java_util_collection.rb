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
