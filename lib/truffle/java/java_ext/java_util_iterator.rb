java.util.Iterator

module ::Java::JavaUtil::Iterator
  def each
    while (has_next) do
      yield self.next
    end
  end
end
