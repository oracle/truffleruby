# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Fiddle
  class Handle

    def initialize(library=nil)
      @handle = Polyglot.eval('nfi', library ? "load #{library}" : 'default')
    rescue RuntimeError
      raise Fiddle::DLError, "#{library}: cannot open shared object file: No such file or directory"
    end

    def sym(symbol)
      @handle[symbol]
    end

    alias_method :[], :sym

  end
end
