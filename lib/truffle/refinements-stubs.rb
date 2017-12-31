# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Refinements stubbed as monkey-patching

Truffle::System.log :PATCH, 'applying refinements-stubs'

class Module
  private def refine(mod, &block)
    mod.class_exec(&block)
    mod
  end

  private def using(mod)
    # Already applied globally
  end
end

class << self
  private def using(mod)
    # Already applied globally
  end
end
