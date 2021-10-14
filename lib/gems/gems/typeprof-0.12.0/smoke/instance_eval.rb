class C
  def self.foo(&blk)
    new.instance_eval(&blk)
  end
  def log(n)
  end
end

C.foo do
  log(42)
end

__END__
# Classes
class C
  def self.foo: { (C) -> nil } -> C
  def log: (Integer n) -> nil
end
