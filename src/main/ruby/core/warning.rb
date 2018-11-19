# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Warning
  extend self

  def warn(message)
    unless message.is_a?(String)
      raise TypeError, "wrong argument type #{message.class} (expected String)"
    end
    unless message.encoding.ascii_compatible?
      raise Encoding::CompatibilityError, "ASCII incompatible encoding: #{message.encoding}"
    end
    $stderr.write message
    nil
  end

end
