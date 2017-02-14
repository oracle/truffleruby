# Ensure the proxy mechanism has created the module.
java.lang.Comparable

module ::Java::JavaLang::Comparable
  def <=>(another)
    case another
    when nil
      nil
    else
      begin
        self.compare_to another
      rescue java.lang.Exception
        raise TypeError
      end
    end
  end
end
