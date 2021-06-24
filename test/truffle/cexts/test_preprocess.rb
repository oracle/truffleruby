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
static void warning_func(void * ctx, const char *msg, ...)
{
  VALUE self = NOKOGIRI_SAX_SELF(ctx);
  VALUE doc = rb_iv_get(self, "@document");
  char * message;
  VALUE ruby_message;

  va_list args;
  va_start(args, msg);
  vasprintf(&message, msg, args);
  va_end(args);

  ruby_message = NOKOGIRI_STR_NEW2(message);
  vasprintf_free(message);
  rb_funcall(doc, id_warning, 1, ruby_message);
}
EOF

modified = <<-EOF
static void warning_func(void * ctx, const char *msg, ...)
{
  VALUE self = NOKOGIRI_SAX_SELF(ctx);
  VALUE doc = rb_iv_get(self, "@document");
  char * message;
  VALUE ruby_message;

  #ifdef NOKOGIRI_PACKAGED_LIBRARIES
va_list args;
  va_start(args, msg);
  vasprintf(&message, msg, args);
  va_end(args);

  ruby_message = NOKOGIRI_STR_NEW2(message);
  vasprintf_free(message);
  rb_funcall(doc, id_warning, 1, ruby_message);
#else
rb_funcall(doc, id_warning, 1, NOKOGIRI_STR_NEW2("Warning."));
#endif

}
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

test_patch 'xml_sax_parser.c', 'ext/nokogiri', original, modified
# Should not patch other files or other gems
test_patch 'other_file.c', 'ext/nokogiri', original, original
test_patch 'xml_sax_parser.c', 'ext/other_gem', original, original

# Tests an empty replacement
test_patch 'parser.c', 'ext/json/ext/parser', json_original, json_patched
