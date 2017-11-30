# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

LOCAL = /\w+\s*(\[\s*\d+\s*\])?/
VALUE_LOCALS = /^(\s+)VALUE\s+(#{LOCAL}(\s*,\s*#{LOCAL})*);\s*$/

ALLOCA_LOCALS = /^(\s+)VALUE\s*\*\s*([a-z_][a-zA-Z_0-9]*)\s*=\s*(\(\s*VALUE\s*\*\s*\)\s*)?alloca\(/

# Found in nokogiri
XML_NODE_SET_PATCH = <<-EOF
  switch (rb_tr_to_int_const(rb_range_beg_len(arg, &beg, &len, (long)node_set->nodeNr, 0))) {
  case Qfalse_int_const:
    break;
  case Qnil_int_const:
EOF

# Found in pg
PG_BINARY_ENCODER_PATCH = <<-EOF
    switch(rb_tr_to_int_const(value)){
        case Qtrue_int_const : mybool = 1; break;
        case Qfalse_int_const : mybool = 0; break;
EOF

# Found in puma
PUMA_HTTP_PARSER_FREE = <<-EOF
void HttpParser_free(puma_parser* hp) {
  TRACE();

  if(hp) {
    rb_tr_release_handle(hp->request);
    rb_tr_release_handle(hp->body);
    xfree(hp);
  }
}
EOF

MYSQL2_FREE_WRAPPER = <<-EOF
    nogvl_close(wrapper);
    truffle_release_handle(wrapper->encoding);
    truffle_release_handle(wrapper->active_thread);
EOF

MYSQL2_FREE_RESULT_WRAPPER = <<-EOF
  truffle_release_handle(wrapper->fields);
  truffle_release_handle(wrapper->rows);
  truffle_release_handle(wrapper->client);
  truffle_release_handle(wrapper->encoding);
  truffle_release_handle(wrapper->statement);
  xfree(wrapper);
EOF

MYSQL2_FREE_STATEMENT = <<-EOF
if (stmt_wrapper->refcount == 0) {
  rb_tr_release_handle(stmt_wrapper->client);
EOF

BYEBUG_BYEBUG_INIT = <<-EOF
Init_byebug()
{
  tracing = Qfalse;
  post_mortem = Qfalse;
  verbose = Qfalse;
  catchpoints = Qnil;
  breakpoints = Qnil;
  tracepoints = Qnil;
  raised_exception = Qnil;
  threads = Qnil;
EOF

BYEBUG_THREADS_INIT = <<-EOF
Init_threads_table(VALUE mByebug)
{
  next_thread = Qnil;
  locker = Qnil;
EOF

PG_CONNECTION_FREE = <<-EOF
pgconn_gc_free( t_pg_connection *this )
{
  rb_tr_release_handle(this->socket_io);
  rb_tr_release_handle(this->notice_receiver);
  rb_tr_release_handle(this->notice_processor);
  rb_tr_release_handle(this->type_map_for_queries);
  rb_tr_release_handle(this->type_map_for_results);
  rb_tr_release_handle(this->trace_stream);
  rb_tr_release_handle(this->external_encoding);
  rb_tr_release_handle(this->encoder_for_put_copy_data);
  rb_tr_release_handle(this->decoder_for_get_copy_data);
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

ID = /([a-zA-Z0-9_]+)/
STRUCT_REF = /#{ID}(->#{ID})*/

NOKOGIRI_DOC_RUBY_OBJECT_ORIG = /(DOC_RUBY_OBJECT\(#{STRUCT_REF}\))/
NOKOGIRI_DOC_RUBY_OBJECT_NEW = '(rb_tr_managed_from_handle_or_null(\1))'

NOKOGIRI_DOC_NODE_CACHE_ORIG = /(DOC_NODE_CACHE\(#{STRUCT_REF}\))/
NOKOGIRI_DOC_NODE_CACHE_NEW = '(rb_tr_managed_from_handle_or_null(\1))'

NO_ASSIGNMENT = /(?:[\),;]|==|!=)/

def read_field(struct_var_name, field_name)
  {
    match: /\b#{struct_var_name}(\.|->)#{field_name}(\s*#{NO_ASSIGNMENT})/,
    replacement: "rb_tr_managed_from_handle(#{struct_var_name}\\1#{field_name})\\2"
  }
end

def write_field(struct_var_name, field_name, leaking)
  leaking_str = leaking ? '_leaking': ''
  {
    match: /\b#{struct_var_name}(\.|->)#{field_name}(\s*=\s*)(\w.+);\s*$/ ,
    replacement: "#{struct_var_name}\\1#{field_name}\\2rb_tr_handle_for_managed#{leaking_str}(\\3);"
  }
end

def read_write_field(struct_var_name, field_name, leaking)
  [read_field(struct_var_name, field_name),
   write_field(struct_var_name, field_name, leaking)]
end

def read_array(name)
  {
    match: /\b#{name}\[(\w+)\](\s*#{NO_ASSIGNMENT})/,
    replacement: "rb_tr_managed_from_handle(#{name}[\\1])\\2"
  }
end

def write_array(name, leaking)
  leaking_str = leaking ? '_leaking': ''
  {
    match: /#{name}\[(\w+)\](\s*=\s*)(\w.+);\s*$/,
    replacement: "#{name}[\\1]\\2rb_tr_handle_for_managed#{leaking_str}(\\3);"
  }
end

def read_write_array(name, leaking)
  [read_array(name),
   write_array(name, leaking)]
end

def cast_value_for_native(name, suffix = '', type = :local)
  # type is not currently used but is there to help preserve the semantic intent.
  { match: /\(void\ \*\)(#{Regexp.quote(name)})#{Regexp.quote(suffix)}/,
    replacement: "rb_tr_handle_for_managed(\\1)#{suffix}"
  }
end

def cast_native_for_value(name, suffix = '', type = :local)
  # type is not currently used but is there to help preserve the semantic intent.
  { match: /\(VALUE\)(#{Regexp.quote(name)})#{Regexp.quote(suffix)}/,
    replacement: "rb_tr_managed_from_handle_or_null(\\1)#{suffix}"
  }
end

def force_cast_native_for_value(name, suffix = '', type = :local)
  # type is not currently used but is there to help preserve the semantic intent.
  { match: /(#{Regexp.quote(name)})#{Regexp.quote(suffix)}/,
    replacement: "rb_tr_managed_from_handle_or_null(\\1)#{suffix}"
  }
end

def tuple_new_patch(ctx, slf)
  { match: "NOKOGIRI_SAX_TUPLE_NEW(#{ctx}, #{slf})",
    replacement: "NOKOGIRI_SAX_TUPLE_NEW(#{ctx}, rb_tr_handle_for_managed(#{slf}))" }
end


PATCHED_FILES = {
  'xml_node_set.c' => {
    gem: 'nokogiri',
    patches: [
      {
        match: /[[:blank:]]*?switch\s*?\(.*?Qnil:/m,
        replacement: XML_NODE_SET_PATCH
      },
      {
        match: 'static VALUE to_array(VALUE self, VALUE rb_node)',
        replacement: 'static VALUE to_array(VALUE self)'
      },
      cast_value_for_native('io')
    ]
  },
  'xml_io.c' => {
    gem: 'nokogiri',
    patches: [
      cast_native_for_value('ctx', ';')
    ]
  },
  'xslt_stylesheet.c' => {
    gem: 'nokogiri',
    patches: [
      {
        match: /[[:blank:]]*?switch\s*?\(.*?Qnil:/m,
        replacement: XML_NODE_SET_PATCH
      },
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
      {
        match: 'va_list args;',
        replacement: 'va_list args; rb_str_cat2(rb_tr_managed_from_handle_or_null(ctx), "Generic error"); return;'
      }
    ]
  },
  'html_document.c' => {
    gem: 'nokogiri',
    patches: [
      cast_value_for_native('error_list'),
      cast_value_for_native('io')

    ]
  },
  'html_sax_push_parser.c' => {
    gem: 'nokogiri',
    patches: [
      tuple_new_patch('ctx', 'self')
    ]
  },
  'xml_sax_push_parser.c' => {
    gem: 'nokogiri',
    patches: [
      tuple_new_patch('ctx', 'self')
    ]
  },
  'xml_document.c' => {
    gem: 'nokogiri',
    patches: [
      cast_value_for_native('error_list'),
      cast_value_for_native('io'),
      cast_native_for_value('ctx'),
      cast_value_for_native('rb_block_proc()', ';'),
      {
        match:'tuple->doc = rb_doc',
        replacement: 'tuple->doc = rb_tr_handle_for_managed(rb_doc)'
      },
      {
        match: 'tuple->node_cache = cache',
        replacement: 'tuple->node_cache = rb_tr_handle_for_managed(cache)'
      },
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
    ]
  },
  'xml_dtd.c' => {
    gem: 'nokogiri',
    patches: [
      cast_value_for_native('error_list'),
      cast_native_for_value('data', ';'),
      cast_value_for_native('hash')
    ]
  },
  'xml_node.c' => {
    gem: 'nokogiri',
    patches: [
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
   ]
  },
  'xml_namespace.c' => {
    gem: 'nokogiri',
    patches: [
      { match: NOKOGIRI_DOC_RUBY_OBJECT_ORIG,
                                  replacement: NOKOGIRI_DOC_RUBY_OBJECT_NEW
                                },
      cast_value_for_native('ns'),
      cast_native_for_value('node->_private', ';')
    ]
  },
  'xml_reader.c' => {
    gem: 'nokogiri',
    patches: [
      cast_value_for_native('error_list'),
      cast_value_for_native('io'),
      cast_value_for_native('rb_io')
    ]
  },
  'xml_sax_parser.c' => {
    gem: 'nokogiri',
    patches: [
      {
        match: 'NOKOGIRI_SAX_SELF(ctx)',
        replacement: 'rb_tr_managed_from_handle_or_null(NOKOGIRI_SAX_SELF(ctx))'
      },
      {
        match: /va_list args;[^}]*id_warning, 1, ruby_message\);/,
        replacement: 'rb_funcall(doc, id_warning, 1, NOKOGIRI_STR_NEW2("Warning."));'
      },
      { match: /va_list args;[^}]*id_error, 1, ruby_message\);/,
        replacement: 'rb_funcall(doc, id_error, 1, NOKOGIRI_STR_NEW2("Warning."));'
      }
    ]
  },
  'xml_sax_parser_context.c' => {
    gem: 'nokogiri',
    patches: [
      cast_value_for_native('error_list'),
      cast_value_for_native('io'),
      tuple_new_patch('ctxt', 'sax_handler')
    ]
  },
  'html_sax_parser_context.c' => {
    gem: 'nokogiri',
    patches: [
      tuple_new_patch('ctxt', 'sax_handler')
    ]
  },
  'xml_syntax_error.c' => {
    gem: 'nokogiri',
    patches: [
      cast_native_for_value('ctx'),
      {
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
    ]
  },
  'xml_xpath_context.c' => {
    gem: 'nokogiri',
    patches: [
      cast_native_for_value('ctx'),
      {
        match: NOKOGIRI_DOC_RUBY_OBJECT_ORIG,
        replacement: NOKOGIRI_DOC_RUBY_OBJECT_NEW
      },
      {
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
      { match: 'VALUE *argv', replacement: 'VALUE argv[32]' },
      { match: /argv =.*$/, replacement: '' },
      { match: 'free(argv)', replacement: '' },
    ]
  },
  'xml_schema.c' => {
    gem: 'nokogiri',
    patches: [
      cast_value_for_native('errors')
    ]
  },
  'xml_relax_ng.c' => {
    gem: 'nokogiri',
    patches: [
      cast_value_for_native('errors')
    ]
  },
  'pg_binary_encoder.c' => {
    gem: 'pg',
    patches: [
      {
        match: /[[:blank:]]*?switch\s*?\(.*?Qfalse\s*?:.*?break;/m,
        replacement: PG_BINARY_ENCODER_PATCH
      }
    ]
  },
  'pg_type_map_by_class.c' => {
    gem: 'pg',
    patches: [
      {
        match: /#define CACHE_LOOKUP\(this, klass\) \( &this->cache_row\[\(klass >> 8\) & 0xff\] \)/,
        replacement: '#define CACHE_LOOKUP(this, klass) ( &this->cache_row[FIX2INT(rb_obj_id(klass)) & 0xff] )'
      }
    ]
  },
  'pg_coder.c' => {
    gem: 'pg',
    patches: [
      *read_write_field('conv','coder_obj', true),
      *read_write_field('this','coder_obj', true)
    ]
  },
  'pg_connection.c' => {
    gem: 'pg',
    patches: [
      *read_write_field('this','socket_io', false),
      *read_write_field('this','notice_receiver', false),
      *read_write_field('this','notice_processor', false),
      *read_write_field('this','type_map_for_queries', false),
      *read_write_field('this','type_map_for_results', false),
      *read_write_field('this','trace_stream', false),
      *read_write_field('this','external_encoding', false),
      *read_write_field('this','encoder_for_put_copy_data', false),
      *read_write_field('this','decoder_for_get_copy_data', false),
      {
        match: /pgconn_gc_free\( t_pg_connection \*this \)\n{/m,
        replacement: PG_CONNECTION_FREE
      }
    ]
  },
  'bytebuffer.c' => {
    gem: 'nio4r',
    patches: [
      {
        match: /(static VALUE .*?) = Qnil;/,
        replacement: '\1;'
      }
    ]
  },
  'monitor.c' => {
    gem: 'nio4r',
    patches: [
      {
        match: /(static VALUE .*?) = Qnil;/,
        replacement: '\1;'
      }
    ]
  },
  'selector.c' => {
    gem: 'nio4r',
    patches: [
      {
        match: /(static VALUE .*?)\s+= Qnil;/,
        replacement: '\1;'
      }
    ]
  },
  'websocket_mask.c' => {
    gem: 'websocket-driver',
    patches: [
      {
        match: /(VALUE .*?)\s+= Qnil;/,
        replacement: '\1;'
      }
    ]
  },
  'http11_parser.c' => {
    gem: 'puma_http11',
    patches: [
      {
        match: /parser->(\w+) = Qnil;/,
        replacement: 'parser->\1 = rb_tr_handle_for_managed(Qnil);'
      }
    ]
  },
  'puma_http11.c' => {
    gem: 'puma_http11',
    patches: [
      # Handles for VALUEs in a static global struct array
      {
        match: /(define\b.*?), Qnil \}/,
        replacement: '\1, NULL }'
      },
      {
        match: /cf->value = (.*?);/,
        replacement: 'cf->value = rb_tr_handle_for_managed(\1);'
      },
      {
        match: /return found \? found->value : Qnil;/,
        replacement: 'return found ? rb_tr_managed_from_handle(found->value) : Qnil;'
      },
      {
        match: /return cf->value;/,
        replacement: 'return rb_tr_managed_from_handle(cf->value);'
      },
      # Handles for puma_parser->request and puma_parser->body
      {
        match: /void HttpParser_free\(void \*data\) {.*?}.*?}/m,
        replacement: PUMA_HTTP_PARSER_FREE
      },
      {
        match: /\(hp->request\b/,
        replacement: '(rb_tr_managed_from_handle(hp->request)'
      },
      {
        match: /(\w+)->request = (.+?);/,
        replacement: '\1->request = rb_tr_handle_for_managed(\2);'
      },
      {
        match: /return http->body;/,
        replacement: 'return rb_tr_managed_from_handle(http->body);'
      },
      {
        match: /(\w+)->body = (.+?);/,
        replacement: '\1->body = rb_tr_handle_for_managed(\2);'
      }
    ]
  },
  'client.c' => {
    gem: 'mysql2',
    patches: [
      *read_write_field('wrapper','active_thread', false),
      *read_write_field('wrapper','encoding', false),
      *read_write_field('async_args','self', true),
      *read_write_field('args','sql', true),
      {
        match: /nogvl_close\(wrapper\);/,
        replacement: MYSQL2_FREE_WRAPPER
      }
    ]
  },
  'result.c' => {
    gem: 'mysql2',
    patches: [
      *read_write_field('wrapper','statement', true),
      *read_write_field('wrapper','client', false),
      *read_write_field('wrapper','encoding', false),
      *read_write_field('wrapper','fields', false),
      *read_write_field('wrapper','rows', false),
      *read_write_field('args','db_timezone', true),
      *read_write_field('args','app_timezone', true),
      *read_write_field('args','block_given', true),
      {
        match: /xfree\(wrapper\);/,
        replacement: MYSQL2_FREE_RESULT_WRAPPER
      }
    ]
  },
  'statement.c' => {
    gem: 'mysql2',
    patches: [
      read_field('wrapper','encoding'),
      write_field('wrapper','active_thread', false),
      *read_write_field('stmt_wrapper','client', false),
      *read_write_field('args','sql', true),
      {
        match: /if \(stmt_wrapper->refcount == 0\) {/,
        replacement: MYSQL2_FREE_STATEMENT
      },
      *read_write_array('params_enc', true)
    ]
  },
  'byebug.c' => {
    gem: 'byebug',
    patches: [
      {
        match: /\b(static VALUE \w+) = (?:Qfalse|Qnil);/,
        replacement: '\1;'
      },
      {
        match: /^VALUE threads = Qnil;/,
        replacement: 'VALUE threads;'
      },
      {
        match: /Init_byebug\(\)\n{/,
        replacement: BYEBUG_BYEBUG_INIT
      }
    ]
  },
  'threads.c' => {
    gem: 'byebug',
    patches: [
      {
        match: /^((?:static )?VALUE \w+) = (?:Qfalse|Qnil);/,
        replacement: '\1;'
      },
      {
        match: /^VALUE next_thread = Qnil;/,
        replacement: 'VALUE next_thread;'
      },
      {
        match: /Init_threads_table\(VALUE mByebug\)\n{/,
        replacement: BYEBUG_THREADS_INIT
      }
    ]
  },
  'parser.c' => {
    gem: 'json',
    ext_dir: 'parser',
    patches: [
      *read_write_field('json','Vsource', false),
      *read_write_field('json','create_id', false),
      *read_write_field('json','object_class', false),
      *read_write_field('json','array_class', false),
      *read_write_field('json','decimal_class', false),
      *read_write_field('json','match_string', false),
      { # cParser_initialize
        match: /if \(rb_tr_managed_from_handle\(json->Vsource\)\)/,
        replacement: 'if (json->Vsource)'
      },
      { # cParser_parse
        match: /VALUE result = Qnil;/,
        replacement: "VALUE result_array_for_address[1];\nresult_array_for_address[0] = Qnil;"
      },
      { # cParser_parse
        match: /char \*np = JSON_parse_value\(json, p, pe, &result, 0\);/,
        replacement: 'char *np = JSON_parse_value(json, p, pe, result_array_for_address, 0);'
      },
      {
        match: /if \(cs >= JSON_first_final && p == pe\) {\s+return result;/m,
        replacement: "if (cs >= JSON_first_final && p == pe) {\n return result_array_for_address[0];"
      },
      { # JSON_parse_object
        match: /VALUE last_name = Qnil;/,
        replacement: "VALUE last_name_array_for_address[1];\nlast_name_array_for_address[0] = Qnil;"
      },
      { # JSON_parse_object
        match: /VALUE v = Qnil;/,
        replacement: "VALUE v_array_for_address[1];\nv_array_for_address[0] = Qnil;"
      },
      { # JSON_parse_object
        match: /np = JSON_parse_string\(json, p, pe, &last_name\);/,
        replacement: 'np = JSON_parse_string(json, p, pe, last_name_array_for_address);'
      },
      { # JSON_parse_object
        match: /rb_hash_aset\(\*result, last_name, v\);/,
        replacement: 'rb_hash_aset(*result, last_name_array_for_address[0], v_array_for_address[0]);'
      },
      { # JSON_parse_object
        match: /rb_funcall\(\*result, i_aset, 2, last_name, v\);/,
        replacement: 'rb_funcall(*result, i_aset, 2, last_name_array_for_address[0], v_array_for_address[0]);'
      },
      { # JSON_parse_object
        match: /char \*np = JSON_parse_value\(json, p, pe, &v, current_nesting\);/,
        replacement: 'char *np = JSON_parse_value(json, p, pe, v_array_for_address, current_nesting);'
      },
      { # JSON_parse_object
        match: /rb_ary_push\(\*result, v\);/,
        replacement: 'rb_ary_push(*result, v_array_for_address[0]);'
      },
      { # JSON_parse_object
        match: /rb_funcall\(\*result, i_leftshift, 1, v\);/,
        replacement: 'rb_funcall(*result, i_leftshift, 1, v_array_for_address[0]);'
      },
      { # JSON_parse_object
        match: /char \*np = JSON_parse_value\(json, p, pe, &v\);/,
        replacement: 'char *np = JSON_parse_value(json, p, pe, v_array_for_address);'
      },
      { # JSON_parse_string
        match: /if \(json->create_additions && RTEST\(match_string = rb_tr_managed_from_handle\(json->match_string\)\)\) {/,
        replacement: 'if (json->create_additions && RTEST(match_string = rb_tr_managed_from_handle_or_null(json->match_string))) {'
      },
      { # JSON_parse_string
        # Work around a bug in the json extension where it tries to call `rb_str_resize` on non-String objects.
        # We remove it entirely because the string resize is an MRI-optimization to reduce large preallocated strings
        # to embedded strings. We don't have that distinction in our implementation and the resize would be a wasteful operation.
        match: /rb_str_resize\(\*result, RSTRING_LEN\(\*result\)\);/,
        replacement: ''
      },
      { # cParser_parse_strict
        match: /np = JSON_parse_array\(json, p, pe, &result\);/,
        replacement: 'np = JSON_parse_array(json, p, pe, result_array_for_address);'
      },
      { # cParser_parse_strict
        match: /np = JSON_parse_object\(json, p, pe, &result\);/,
        replacement: 'np = JSON_parse_object(json, p, pe, result_array_for_address);'
      },
      { # cParser_parse_quirks_mode
        match: /if \(cs >= JSON_quirks_mode_first_final && p == pe\) {\s+return result;/m,
        replacement: "if (cs >= JSON_quirks_mode_first_final && p == pe) {\n return result_array_for_address[0];"
      },
      { # cParser_parse_quirks_mode
        match: /char \*np = JSON_parse_value\(json, p, pe, &result\);/,
        replacement: 'char *np = JSON_parse_value(json, p, pe, result_array_for_address);'
      }
    ]
  },
  'generator.c' => {
    gem: 'json',
    ext_dir: 'generator',
    patches: [
      { # generate_json
        match: /if \(obj == Qnil\)/,
        replacement: 'if (NIL_P(obj))'
      },
      { # generate_json
        match: /if \(obj == Qfalse\)/,
        replacement: 'if (rb_tr_obj_equal(Qfalse, obj))'
      },
      { # generate_json
        match: /if \(obj == Qtrue\)/,
        replacement: 'if (rb_tr_obj_equal(Qtrue, obj))'
      }
    ]
  }
}

def preprocess(line)
  if line =~ VALUE_LOCALS
    # Translate
    #   VALUE args[6], failed, a1, a2, a3, a4, a5, a6;
    #  into
    #   VALUE failed, a1, a2, a3, a4, a5, a6; VALUE *args = truffle_managed_malloc(6 * sizeof(VALUE));

    simple = []
    arrays = []

    line = $1

    $2.split(',').each do |local|
      local.strip!
      if local.end_with?(']')
        raise unless local =~ /(\w+)\s*\[\s*(\d+)\s*\]/
        arrays << [$1, $2.to_i]
      else
        simple << local
      end
    end

    unless simple.empty?
      line += "VALUE #{simple.join(', ')};"
    end

    arrays.each do |name, size|
      line += " VALUE *#{name} = truffle_managed_malloc(#{size} * sizeof(VALUE));"
    end
    line
  elsif line =~ ALLOCA_LOCALS
    # Translate
    #   VALUE *argv = alloca(sizeof(VALUE) * argc);
    # into
    #   VALUE *argv = (VALUE *)truffle_managed_malloc(sizeof(VALUE) * argc);

    line = "#{$1}VALUE *#{$2} = truffle_managed_malloc(#{$'}"
  else
    line
  end
end

def patch(file, contents, directory)
  if patched_file = PATCHED_FILES[File.basename(file)]
    matched = if patched_file[:ext_dir]
                directory.end_with?(File.join(patched_file[:gem], 'ext', patched_file[:ext_dir]))
              else
                regexp = /^#{Regexp.escape(patched_file[:gem])}\b/
                directory.split('/').last(2).any? { |part| part =~ regexp }
              end
    if matched
      patched_file[:patches].each do |patch|
        contents = contents.gsub(patch[:match], patch[:replacement].rstrip)
      end
    end
  end
  contents
end

if __FILE__ == $0
  puts "#line 1 \"#{ARGF.filename}\""

  contents = patch(ARGF.filename, File.read(ARGF.filename), Dir.pwd)
  contents.each_line do |line|
    puts preprocess(line)
  end
end
