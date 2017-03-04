# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Fiddle
  class Function
    
    DEFAULT = :default
  
    def initialize(ptr, args, ret_type, abi=DEFAULT)
      @function = ptr.bind(Truffle::Interop.to_java_string("(#{args.join(',')}):#{ret_type}"))
    end
    
    def call(*args)
      @function.call(*args)
    end
  
  end
end
