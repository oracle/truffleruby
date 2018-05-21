# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'common_patches'

class Nio4RPatches < CommonPatches
  PATCHES = {
    gem: 'nio4r',
    patches: {
      'bytebuffer.c' => [
        {
          match: /(static VALUE .*?) = Qnil;/,
          replacement: '\1;'
        }
      ],
      'monitor.c' => [
        {
          match: /(static VALUE .*?) = Qnil;/,
          replacement: '\1;'
        }
      ],
      'selector.c' => [
        {
          match: /(static VALUE .*?)\s+= Qnil;/,
          replacement: '\1;'
        }
      ]
    },
  }
end
