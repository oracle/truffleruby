# Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Patches nokogiri 1.8.1

class NokogiriPatches

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

  def self.using_system_libraries?
    !ARGV.include?('-DNOKOGIRI_PACKAGED_LIBRARIES')
  end

  PATCHES = {
    gem: 'nokogiri',
    patches: {
      'xml_node_set.c' => [
        { # Nokogiri declares the function with more arguments than it
          # is called with. This works on MRI but causes an error in
          # TruffleRuby.
          match: 'static VALUE to_array(VALUE self, VALUE rb_node)',
          replacement: 'static VALUE to_array(VALUE self)',
          predicate: -> { using_system_libraries? }
        },
      ],
      'xslt_stylesheet.c' => [
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: 'va_list args;',
          replacement: 'va_list args; rb_str_cat2(ctx, "Generic error"); return;',
          predicate: -> { using_system_libraries? }
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
          replacement: 'rb_funcall(doc, id_warning, 1, NOKOGIRI_STR_NEW2("Warning."));',
          predicate: -> { using_system_libraries? }
        },
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: /va_list args;[^}]*id_error, 1, ruby_message\);/,
          replacement: 'rb_funcall(doc, id_error, 1, NOKOGIRI_STR_NEW2("Warning."));',
          predicate: -> { using_system_libraries? }
        }
      ],
      'xml_xpath_context.c' => [
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: 'va_list args;',
          replacement: 'va_list args; rb_raise(rb_eRuntimeError, "%s", "Exception:"); return;',
          predicate: -> { using_system_libraries? }
        },
        {
          match: 'VALUE thing = Qnil;',
          replacement: "VALUE thing = Qnil;\nVALUE errors = rb_ary_new();",
          predicate: -> { using_system_libraries? }
        },
        {
          match: 'VALUE retVal = Qnil;',
          replacement: "VALUE retVal = Qnil;\nVALUE errors = rb_ary_new();",
          predicate: -> { using_system_libraries? }
        },
        {
          match: 'xmlSetStructuredErrorFunc(NULL, Nokogiri_error_raise);',
          replacement: 'xmlSetStructuredErrorFunc(errors, Nokogiri_error_array_pusher);',
          predicate: -> { using_system_libraries? }
        },
        {
          match: 'if(xpath == NULL)',
          replacement: "if (RARRAY_LEN(errors) > 0) { rb_exc_raise(rb_ary_entry(errors, 0)); }\nif(xpath == NULL)",
          predicate: -> { using_system_libraries? }
        },
      ],
    }
  }
end
