# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Fiddle
  class Handle
    
    def initialize(library=nil)
      @handle = Truffle::Interop.eval('application/x-native', library ? "load #{library}" : 'default')
    end
    
    def sym(symbol)
      @handle[symbol]
    end
    
    alias_method :[], :sym
  
  end
end
