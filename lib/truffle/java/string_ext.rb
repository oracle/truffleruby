class String
  def to_java_bytes
    a = self.bytes
    ba = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, a.size)
    a.each_with_index { |b, i| ba[i] = b }
    ba
  end

  def self.from_java_bytes(ba)
    a = Array.new(ba.size)
    ba.each_with_index { |b, i| a[i] = b }
    a.pack('c*')
  end
end
