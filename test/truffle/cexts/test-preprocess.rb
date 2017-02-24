# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../../lib/cext/preprocess'

def test(input, expected)
  got = preprocess(input)
  raise "expected #{expected.inspect}, got #{got.inspect}" unless got == expected
end

test '  rb_scan_args(argc, argv, "11", &v1, &v2);',       '  rb_jt_scan_args_11(argc, argv, "11", &v1, &v2);'
test '  rb_scan_args(argc, argv, runtime, &v1, &v2);',    '  rb_scan_args(argc, argv, runtime, &v1, &v2);'
test '  VALUE args[6], failed, a1, a2, a3, a4, a5, a6;',  '  VALUE failed, a1, a2, a3, a4, a5, a6; VALUE *args = truffle_managed_malloc(6 * sizeof(VALUE));'
test '  VALUE args, failed, a1, a2, a3, a4, a5, a6;',     '  VALUE args, failed, a1, a2, a3, a4, a5, a6;'
