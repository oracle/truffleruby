# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../../lib/truffle/truffle/cext_preprocessor'

def test_patch(file, directory, input, expected)
  got = Truffle::CExt::Preprocessor.patch(file, input, directory)
  abort "expected\n#{expected}\ngot\n#{got}" unless got == expected
end

original = <<-EOF
static int dealloc_node_i(xmlNodePtr key, xmlNodePtr node, xmlDocPtr doc)
{
}
EOF

modified = <<-EOF
static int dealloc_node_i(st_data_t a, st_data_t b, st_data_t c, int errorState)
{
  xmlNodePtr key = (xmlNodePtr)a;
  xmlNodePtr node = (xmlNodePtr)b;
  xmlDocPtr doc = (xmlDocPtr)c;}
EOF

json_original = <<-EOF
# else
rb_str_resize(*result, RSTRING_LEN(*result));
# endif
EOF

json_patched = <<-EOF
# else

# endif
EOF

test_patch 'xml_document.c', 'ext/nokogiri', original, modified
# Should not patch other files or other gems
test_patch 'other_file.c', 'ext/nokogiri', original, original
test_patch 'xml_sax_parser.c', 'ext/other_gem', original, original

# Tests an empty replacement
test_patch 'parser.c', 'ext/json/ext/parser', json_original, json_patched
