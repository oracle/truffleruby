class Foo
  def id(&blk)
    blk
  end

  def check(v)
    rand < 0.5 ? v : [v]
  end

  def foo
    check(id {}).call # cause "undefined method: [^-> nil?]#call"
  end

  def foo1
    id { }
  end
end

__END__
# Errors
smoke/proc7.rb:11: [error] undefined method: nil#call
smoke/proc7.rb:11: [error] undefined method: [(^(unknown) -> bot | untyped)?]#call
smoke/proc7.rb:11: [error] undefined method: nil#call
smoke/proc7.rb:11: [error] undefined method: [(^-> nil | untyped)?]#call

# Classes
class Foo
  def id: ?{ -> nil } -> ^-> nil?
  def check: ((^-> nil | untyped)? v) -> (([(^-> nil | untyped)?] | ^-> nil | untyped)?)
  def foo: -> untyped?
  def foo1: -> ^-> nil?
end
