# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative 'common_patches'

class ZLibPatches < CommonPatches
  PATCHES = {
    gem: 'zlib',
    patches: {
      'zlib.c' => [
        {
          match: /&z->stream(?=[,);])/,
          replacement: 'polyglot_get_member(z, "stream")'
        },
        {
          match: /&gz->z\.stream(?=[,);])/,
          replacement: 'polyglot_get_member(polyglot_get_member(gz, "z"), "stream")'
        },
        {
          match: /&gz->z(?=[,);])/,
          replacement: 'polyglot_as_zstream(polyglot_get_member(gz, "z"))'
        },
      ]
    }
  }
end
