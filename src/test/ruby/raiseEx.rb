def shortArg(n)
  if n > 65535
    raise ArgumentError, "Number too big", caller
  end
end
def throwMsg(msg)
  raise msg
end
shortArg(10)
