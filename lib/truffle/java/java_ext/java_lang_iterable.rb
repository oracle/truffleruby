# Ensure the proxy mechanism has created the module.
java.lang.Iterable

module ::Java::JavaLang::Iterable
  include Enumerable

  def each
    iterator = self.iterator
    while (iterator.has_next) do
      yield iterator.next
    end
  end

  def each_with_index
    i = 0
    iterator = self.iterator
    while (iterator.has_next) do
      yield iterator.next, i
      i += 1
    end
  end
end
