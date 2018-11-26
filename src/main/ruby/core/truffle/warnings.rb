# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module Warnings
    def self.warn(message)
      if !$VERBOSE.nil?
        caller = caller_locations(2, 1)[0]
        Warning.warn "#{caller.path}:#{caller.lineno}: warning: #{message}\n"
      end
    end

    def self.warning(message)
      if $VERBOSE
        caller = caller_locations(2, 1)[0]
        Warning.warn "#{caller.path}:#{caller.lineno}: warning: #{message}\n"
      end
    end
  end
end
