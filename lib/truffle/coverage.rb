# Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Coverage

  def self.start
    Truffle::Coverage.enable
  end

  def self.result
    result = peek_result
    Truffle::Coverage.disable
    result
  end

  def self.running?
    Truffle::Coverage.enabled?
  end

  def self.peek_result
    Truffle::Coverage.result_array.to_h
  end

end
