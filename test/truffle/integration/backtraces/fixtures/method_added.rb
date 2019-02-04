module TestMethodAdded
  def self.method_added(meth)
    raise meth.to_s
  end

  def foo
  end
end
