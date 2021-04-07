# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class MyException < StandardError
end

def recurse(n)
  if n == 0
    raise MyException, 'message'
  else
    recurse(n - 1)
  end
end

benchmark 'exception-not-captured-small-stack' do
  begin
    raise MyException, 'message'
  rescue MyException
    nil
  end
end

benchmark 'exception-not-captured-stack-100' do
  begin
    recurse(100)
  rescue MyException
    nil
  end
end

benchmark 'exception-not-captured-stack-1000' do
  begin
    recurse(1000)
  rescue MyException
    nil
  end
end
