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

PATCHED_FILES = {
  'xml_node_set.c' => {
    gem: 'nokogiri',
    patches: [
      {
        match: /[[:blank:]]*?switch\s*?\(.*?Qnil:/m,
        replacement: XML_NODE_SET_PATCH
      }
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
