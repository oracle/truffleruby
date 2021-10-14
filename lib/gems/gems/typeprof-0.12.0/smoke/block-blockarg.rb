def log1(x); end
def log2(x); end
def log3(x); end

def f(&blk)
  log3(
    blk.call do |x|
      log1(x)
      :b
    end
  )
  :d
end

f do |&blk|
  log2(blk.call(:a))
  :c
end

__END__
# Classes
class Object
  private
  def log1: (:a x) -> nil
  def log2: (:b x) -> nil
  def log3: (:c x) -> nil
  def f: { -> :c } -> :d
end
