def foo(&blk)
  [blk]
end

foo { }
foo { }

__END__
# Classes
class Object
  private
  def foo: { -> nil } -> [^-> nil]
end
