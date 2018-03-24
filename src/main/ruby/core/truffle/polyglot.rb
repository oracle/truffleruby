# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Polyglot

  def self.export(name, value)
    Truffle::Interop.export name, value
  end
  
  def self.export_method(name)
    Truffle::Interop.export_method name
  end
  
  def self.import(name)
    Truffle::Interop.import(name)
  end
  
  def self.import_method(name)
    Truffle::Interop.import_method name
  end
  
  def self.as_enumerable(object)
    Truffle::Interop.enumerable(object)
  end

end
