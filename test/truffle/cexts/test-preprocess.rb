# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../../lib/truffle/truffle/cext_preprocessor'

def test_patch(file, directory, input, expected)
  got = Truffle::CExt::Preprocessor.patch(file, input, directory)
  abort "expected\n#{expected.inspect}\ngot\n#{got.inspect}" unless got == expected
end

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
test_patch 'xml_node_set.c', 'ext/nokogiri', SWITCH_ONE_ACTUAL, SWITCH_ONE_EXPECTED
