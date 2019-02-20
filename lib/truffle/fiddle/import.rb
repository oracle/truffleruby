# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Fiddle
  module Importer

    def dlload(*libs)
      warn 'Fiddle::Importer.dlload does nothing'
    end

    def typealias(alias_type, orig_type)
      warn 'Fiddle::Importer.typealias does nothing'
    end

    def extern(signature, *opts)
      warn 'Fiddle::Importer.extern does nothing'
    end

    def bind(signature, *opts, &blk)
      warn 'Fiddle::Importer.bind does nothing'
    end

    def struct(signature)
      warn 'Fiddle::Importer.struct does nothing'
    end

  end
end
