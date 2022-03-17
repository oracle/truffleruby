def foo(&blk)
  $blk = blk
  nil
end

foo {}
foo {|arg| }

$blk.call(42)

__END__
# Global variables
$blk: ^(?Integer) -> nil

# Classes
class Object
  private
  def foo: { (Integer) -> nil } -> nil
end
