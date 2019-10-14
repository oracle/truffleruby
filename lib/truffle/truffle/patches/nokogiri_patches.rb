# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Patches nokogiri 1.8.1

class NokogiriPatches

  XML_NODE_SET_PATCH = <<-EOF
  switch (rb_tr_to_int_const(rb_range_beg_len(arg, &beg, &len, (long)node_set->nodeNr, 0))) {
  case Qfalse_int_const:
    break;
  case Qnil_int_const:
EOF

  NOKOGIRI_DEALLOC_DECL_ORIG = <<-EOF
static int dealloc_node_i(xmlNodePtr key, xmlNodePtr node, xmlDocPtr doc)
{
EOF

  NOKOGIRI_DEALLOC_DECL_NEW = <<-EOF
static int dealloc_node_i(st_data_t a, st_data_t b, st_data_t c, int errorState)
{
  xmlNodePtr key = (xmlNodePtr)a;
  xmlNodePtr node = (xmlNodePtr)b;
  xmlDocPtr doc = (xmlDocPtr)c;
EOF

  PATCHES = {
    gem: 'nokogiri',
    patches: {
      'xml_node_set.c' => [
        {
          match: /[[:blank:]]*?switch\s*?\(.*?Qnil:/m,
          replacement: XML_NODE_SET_PATCH
        },
        { # Nokogiri declares the function with more arguments than it
          # is called with. This works on MRI but causes an error in
          # TruffleRuby.
          match: 'static VALUE to_array(VALUE self, VALUE rb_node)',
          replacement: 'static VALUE to_array(VALUE self)'
        },
      ],
      'xml_io.c' => [
      ],
      'xslt_stylesheet.c' => [
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: 'va_list args;',
          replacement: 'va_list args; rb_str_cat2(ctx, "Generic error"); return;'
        }
      ],
      'xml_document.c' => [
        {
          match: NOKOGIRI_DEALLOC_DECL_ORIG,
          replacement: NOKOGIRI_DEALLOC_DECL_NEW
        }
      ],
      'xml_sax_parser.c' => [
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: /va_list args;[^}]*id_warning, 1, ruby_message\);/,
          replacement: 'rb_funcall(doc, id_warning, 1, NOKOGIRI_STR_NEW2("Warning."));'
        },
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: /va_list args;[^}]*id_error, 1, ruby_message\);/,
          replacement: 'rb_funcall(doc, id_error, 1, NOKOGIRI_STR_NEW2("Warning."));'
        }
      ],
      'xml_xpath_context.c' => [
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: 'va_list args;',
          replacement: 'va_list args; rb_raise(rb_eRuntimeError, "%s", "Exception:"); return;'
        },
        {
          match: 'VALUE thing = Qnil;',
          replacement: "VALUE thing = Qnil;\n  VALUE errors = rb_ary_new();"
        },
        {
          match: 'xmlSetStructuredErrorFunc(NULL, Nokogiri_error_raise);',
          replacement: 'xmlSetStructuredErrorFunc(errors, Nokogiri_error_array_pusher);'
        },
        {
          match: 'if(xpath == NULL)',
          replacement: "if (RARRAY_LEN(errors) > 0) { rb_exc_raise(rb_ary_entry(errors, 0)); }\nif(xpath == NULL)"
        },
      ],
    }
  }
end
