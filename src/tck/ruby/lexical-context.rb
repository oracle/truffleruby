# Copyright (c) 2019, 2024 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Beware, RubyTCKLanguageProvider use hard-coded line numbers from this file!

-> {
  a = 14
  3.times do
    b = 2
    [6].each do |c|
      x = c
    end
  end
}
