# Make sure we get the class.
java.lang.Throwable

class ::Java::JavaLang::Throwable
  def self.===(another)
    ( another.kind_of?(::JavaUtilities::JavaException) &&
      another.java_exception.kind_of?(self) ) ||
      super(another)
  end
end
