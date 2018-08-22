# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'common_patches'

class ZLibPatches < CommonPatches
  PATCHES = {
    gem: 'zlib',
    patches: {
      'zlib.c' => [
        {
          match: /&(z|z1|z2)->stream(?=[,);])/,
          replacement: 'polyglot_get_member(\1, "stream")'
        },
        {
          match: /&gz->z\.stream(?=[,);])/,
          replacement: 'polyglot_get_member(polyglot_get_member(gz, "z"), "stream")'
        },
        {
          match: /&gz->z(?=[,);])/,
          replacement: 'polyglot_as_zstream(polyglot_get_member(gz, "z"))'
        },
        {
          match: /TypedData_Get_Struct\((?<args>(klass|obj), struct (?<type>zstream|gzfile), &(\k<type>)_data_type, (?<varname>z|gz))\);/,
          replacement: 'TypedData_Get_Managed_Struct(\k<args>, \k<type>); // Currently we loose interop type info when going from C to Ruby and back'
        },
        {
          match: /(?<lhs>struct gzfile \*gz) = (?<rhs>\(struct gzfile\s*\*\)arg);/,
          replacement: '\k<lhs> = polyglot_as_gzfile(\k<rhs>); // Currently we loose interop type info when going from C to Ruby and back'
        },
      ]
    }
  }
end
