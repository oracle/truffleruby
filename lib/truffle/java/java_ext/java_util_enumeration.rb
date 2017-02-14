java.util.Enumeration

module ::Java::JavaUtil::Enumeration
  def each
    while has_more_elements do
      yield next_element
    end
  end
end
