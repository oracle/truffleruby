def foo
  Truffle::Debug.throw_java_exception 'custom message'
end

foo
