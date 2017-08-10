# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../../lib/cext/preprocess'

def test(input, expected=input)
  got = preprocess(input)
  raise "expected #{expected.inspect}, got #{got.inspect}" unless got == expected
end

def test_patch(file, input, expected)
  got = patch(file, input)
  raise "expected #{expected.inspect}, got #{got.inspect}" unless got == expected
end

test '  VALUE args[6], failed, a1, a2, a3, a4, a5, a6;',        '  VALUE failed, a1, a2, a3, a4, a5, a6; VALUE *args = truffle_managed_malloc(6 * sizeof(VALUE));'
test '  VALUE args, failed, a1, a2, a3, a4, a5, a6;'
test '  VALUE a,b[2],c;',                                       '  VALUE a, c; VALUE *b = truffle_managed_malloc(2 * sizeof(VALUE));'
test '  VALUEx, b, c;'
test '  VALUE *argv = alloca(sizeof(VALUE) * argc);',           '  VALUE *argv = truffle_managed_malloc(sizeof(VALUE) * argc);'
test '  VALUE *arg_v = (VALUE*) alloca(sizeof(VALUE) * argc);', '  VALUE *arg_v = truffle_managed_malloc(sizeof(VALUE) * argc);'
test '  long *argv = alloca(sizeof(long) * argc);'

SWITCH_ONE_ACTUAL = <<-EOF
  switch (rb_range_beg_len(arg, &beg, &len, (long)node_set->nodeNr, 0)) {
  case Qfalse:
    break;
  case Qnil:
EOF

SWITCH_ONE_EXPECTED = <<-EOF
  switch (rb_tr_to_int_const(rb_range_beg_len(arg, &beg, &len, (long)node_set->nodeNr, 0))) {
  case Qfalse_int_const:
    break;
  case Qnil_int_const:
EOF
test_patch 'xml_node_set.c', SWITCH_ONE_ACTUAL, SWITCH_ONE_EXPECTED