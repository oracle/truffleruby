# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# This is a no-op implementation of Ripper

class Ripper

  class Filter

    def initialize(src, filename='-', lineno=1)
    end

    def parse(init=nil)
      []
    end

  end

end
