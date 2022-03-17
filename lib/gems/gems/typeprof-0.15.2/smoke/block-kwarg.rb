def f1
  yield k: 1
end
def log1(n); end
f1 {|k:| log1(k) }

def f2
  yield k: 1
end
def log2(n); end
f2 {|k: :opt| log2(k) }

def f3
  yield k: 1
end
def log3(n); end
f3 {|k: "str"| log3(k) }

def f4
  yield k: 1
end
def log4(kwrest); end
f4 {|**kwrest| log4(kwrest) }

def f5
  yield k: 1
end
f5 {|| }

def f6
  yield
end
f6 {|k:| }

__END__
# Errors
smoke/block-kwarg.rb:26: [error] unknown keyword: k
smoke/block-kwarg.rb:31: [error] no argument for required keywords

# Classes
class Object
  private
  def f1: { -> nil } -> nil
  def log1: (Integer n) -> nil
  def f2: { -> nil } -> nil
  def log2: (:opt | Integer n) -> nil
  def f3: { -> nil } -> nil
  def log3: (Integer | String n) -> nil
  def f4: { -> nil } -> nil
  def log4: ({k: Integer} kwrest) -> nil
  def f5: { -> nil } -> untyped
  def f6: { -> nil } -> untyped
end
