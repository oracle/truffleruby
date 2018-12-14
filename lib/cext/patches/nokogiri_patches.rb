# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Patches nokogiri 1.8.1

require_relative 'common_patches'

class NokogiriPatches < CommonPatches

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

  STRUCT_REF = /#{ID}(->#{ID})*/

  NOKOGIRI_DOC_RUBY_OBJECT_ORIG = /(DOC_RUBY_OBJECT\(#{STRUCT_REF}\))/
  NOKOGIRI_DOC_RUBY_OBJECT_NEW = '(rb_tr_managed_from_handle_or_null(\1))'

  NOKOGIRI_DOC_NODE_CACHE_ORIG = /(DOC_NODE_CACHE\(#{STRUCT_REF}\))/
  NOKOGIRI_DOC_NODE_CACHE_NEW = '(rb_tr_managed_from_handle_or_null(\1))'

  def self.tuple_new_patch(ctx, slf)
    {
      match: "NOKOGIRI_SAX_TUPLE_NEW(#{ctx}, #{slf})",
      replacement: "NOKOGIRI_SAX_TUPLE_NEW(#{ctx}, rb_tr_handle_for_managed(#{slf}))" }
  end

  def self.cast_value_for_native(name, suffix = '', type = :local)
    # Generate a patch for a simple cast of a VALUE to a (void *) based
    # on the lifetime of the native use. A type of :local indicates the
    # native use will only be for the duration of the function call,
    # :global indicates a reference must be created that will prevent
    # the VALUE being garbage collected, and :weak indicates a reference
    # must be created that will _not_ prevent the VALUE being garbage
    # collected. Suffix specifies a string following the variable which
    # may be used to limit matches (e.g. if 'err' should be cast, but
    # not 'error_list'.
    #
    # type is not currently used but is there to help preserve the semantic intent.
    {
      match: /\(void\ \*\)(#{Regexp.quote(name)})#{Regexp.quote(suffix)}/,
      replacement: "rb_tr_handle_for_managed(\\1)#{suffix}"
    }
  end

  def self.cast_native_for_value(name, suffix = '', type = :local)
    # Generate a patch to convert a native pointer back into a
    # VALUE. See cast_native_for_value for the meaning of the type
    # argument.

    # Type is not currently used but is there to help preserve the
    # semantic intent.
    {
      match: /\(VALUE\)(#{Regexp.quote(name)})#{Regexp.quote(suffix)}/,
      replacement: "rb_tr_managed_from_handle_or_null(\\1)#{suffix}"
    }
  end

  def self.force_cast_native_for_value(name, suffix = '', type = :local)
    # Like cast_native_for_value but for cases where the original code
    # did not include and explicit cast to VALUE.

    # type is not currently used but is there to help preserve the semantic intent.
    {
      match: /(#{Regexp.quote(name)})#{Regexp.quote(suffix)}/,
      replacement: "rb_tr_managed_from_handle_or_null(\\1)#{suffix}"
    }
  end

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
        cast_value_for_native('io')
      ],
      'xml_io.c' => [
        cast_native_for_value('ctx', ';')
      ],
      'xslt_stylesheet.c' => [
        {
          match: 'rb_ary_new()',
          replacement: 'rb_tr_handle_for_managed(rb_ary_new())'
        },
        cast_value_for_native('self'),
        cast_value_for_native('errstr'),
        cast_value_for_native('inst'),
        cast_native_for_value('xsltGetExtData(transform, functionURI)', ';'),
        force_cast_native_for_value('ctxt->style->_private'),
        {
          match: '(wrapper->func_instances',
          replacement: '(rb_tr_managed_from_handle_or_null(wrapper->func_instances)'
        },
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: 'va_list args;',
          replacement: 'va_list args; rb_str_cat2(rb_tr_managed_from_handle_or_null(ctx), "Generic error"); return;'
        }
      ],
      'html_document.c' => [
        cast_value_for_native('error_list'),
        cast_value_for_native('io')

      ],
      'html_sax_push_parser.c' => [
        tuple_new_patch('ctx', 'self')
      ],
      'xml_sax_push_parser.c' => [
        tuple_new_patch('ctx', 'self')
      ],
      'xml_document.c' => [
        cast_value_for_native('error_list'),
        cast_value_for_native('io'),
        cast_native_for_value('ctx'),
        cast_value_for_native('rb_block_proc()', ';'),
        write_field('tuple', 'doc', false),
        write_field('tuple', 'node_cache', false),
        {
          match: NOKOGIRI_DEALLOC_DECL_ORIG,
          replacement: NOKOGIRI_DEALLOC_DECL_NEW
        },
        {
          match: NOKOGIRI_DOC_RUBY_OBJECT_ORIG,
          replacement: NOKOGIRI_DOC_RUBY_OBJECT_NEW
        },
        {
          match: NOKOGIRI_DOC_NODE_CACHE_ORIG,
          replacement: NOKOGIRI_DOC_NODE_CACHE_NEW
        }
      ],
      'xml_dtd.c' => [
        cast_value_for_native('error_list'),
        cast_native_for_value('data', ';'),
        cast_value_for_native('hash')
      ],
      'xml_node.c' => [
        cast_value_for_native('err', ','),
        cast_value_for_native('error_list'),
        cast_value_for_native('io'),
        {
          match: NOKOGIRI_DOC_RUBY_OBJECT_ORIG,
          replacement: NOKOGIRI_DOC_RUBY_OBJECT_NEW
        },
        {
          match: NOKOGIRI_DOC_NODE_CACHE_ORIG,
          replacement: NOKOGIRI_DOC_NODE_CACHE_NEW
        },
        cast_native_for_value('node->_private', ';'),
        cast_value_for_native('rb_node'),
      ],
      'xml_namespace.c' => [
        {
          match: NOKOGIRI_DOC_RUBY_OBJECT_ORIG,
          replacement: NOKOGIRI_DOC_RUBY_OBJECT_NEW
        },
        cast_value_for_native('ns'),
        cast_native_for_value('node->_private', ';')
      ],
      'xml_reader.c' => [
        cast_value_for_native('error_list'),
        cast_value_for_native('io'),
        cast_value_for_native('rb_io')
      ],
      'xml_sax_parser.c' => [
        {
          match: 'NOKOGIRI_SAX_SELF(ctx)',
          replacement: 'rb_tr_managed_from_handle_or_null(NOKOGIRI_SAX_SELF(ctx))'
        },
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
      'xml_sax_parser_context.c' => [
        cast_value_for_native('error_list'),
        cast_value_for_native('io'),
        tuple_new_patch('ctxt', 'sax_handler')
      ],
      'html_sax_parser_context.c' => [
        tuple_new_patch('ctxt', 'sax_handler')
      ],
      'xml_syntax_error.c' => [
        cast_native_for_value('ctx'),
        { # rb_class_new_instance takes a pointer to an array of arguments, and forcing it to inlined simply pushes the issue to
          match: 'VALUE msg',
          replacement: 'VALUE msg[1]'
        },
        {
          match: 'msg = ',
          replacement: 'msg[0] = '
        },
        {
          match: '&msg',
          replacement: 'msg'
        },
      ],
      'xml_xpath_context.c' => [
        cast_native_for_value('ctx'),
        {
          match: NOKOGIRI_DOC_RUBY_OBJECT_ORIG,
          replacement: NOKOGIRI_DOC_RUBY_OBJECT_NEW
        },
        { # It is not currently possible to pass var args from native
          # functions to sulong, so we work round the issue here.
          match: 'va_list args;',
          replacement: 'va_list args; rb_raise(rb_eRuntimeError, "%s", "Exception:"); return;'
        },
        cast_value_for_native('xpath_handler'),
        {
          match: 'VALUE thing = Qnil;',
          replacement: "VALUE thing = Qnil;\n  VALUE errors = rb_ary_new();"
        },
        {
          match: 'xmlSetStructuredErrorFunc(NULL, Nokogiri_error_raise);',
          replacement: 'xmlSetStructuredErrorFunc(rb_tr_handle_for_managed(errors), Nokogiri_error_array_pusher);'
        },
        {
          match: 'if(xpath == NULL)',
          replacement: "if (RARRAY_LEN(errors) > 0) { rb_exc_raise(rb_ary_entry(errors, 0)); }\nif(xpath == NULL)"
        },
        cast_native_for_value('(ctx->context->userData)', ';'),
        { # The following patches change
          # Nokogiri_marshal_xpath_funcall_and_return_values to marshal
          # arguments into a ruby array and pass call the function using
          # rb_apply. This is the easiest way to handle passing an
          # unknown, and potentially large set of arguments to the ruby
          # function.
          match: 'VALUE *argv;',
          replacement: 'VALUE argv;'
        },
        {
          match: /argv =.*$/,
          replacement: 'argv = rb_ary_new_capa(nargs);'
        },
        {
          match: /rb_gc_register_address.*$/,
          replacement: ''
        },
        {
          match: /rb_gc_unregister_address.*$/,
          replacement: ''
        },
        {
          match: /argv\[i\]\ =\ ([^;]+);/,
          replacement: 'rb_ary_store(argv, i, \1);'
        },
        {
          match: 'rb_funcall2(handler, rb_intern((const char*)function_name), nargs, argv);',
          replacement: 'rb_apply(handler, rb_intern((const char*)function_name), argv);'
        },
        {
          match: 'free(argv)',
          replacement: ''
        },
      ],
      'xml_schema.c' => [
        cast_value_for_native('errors')
      ],
      'xml_relax_ng.c' => [
        cast_value_for_native('errors')
      ]
    }
  }
end
