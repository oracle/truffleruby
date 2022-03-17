class Human
  define_method(:fo) { }
  
  [:a, :b].each { |m|
    define_method(m) { }
  }
end

Human.new.a
Human.new.b

__END__
# Classes
class Human
  def fo: -> nil
  def a: -> nil
  def b: -> nil
end
