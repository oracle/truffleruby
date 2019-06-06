# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# This is a no-op implementation of Ripper

class Ripper

  def initialize(src, filename = "(ripper)", lineno = 1)
  end

  def parse
  end

  def column
  end

  def state
  end

  def yydebug
    false
  end

  def end_seen?
    false
  end

  def lineno
  end

  class Filter

    def initialize(src, filename='-', lineno=1)
    end

    def parse(init=nil)
      []
    end

  end

end
