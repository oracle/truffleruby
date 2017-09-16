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
      {
        match: /wrapper->active_thread = Qnil;/,
        replacement: 'wrapper->active_thread = rb_tr_handle_for_managed(Qnil);'
      },
      {
        match: /wrapper->encoding = Qnil;/,
        replacement: 'wrapper->encoding = rb_tr_handle_for_managed(Qnil);'
      },
      {
        match: /nogvl_close\(wrapper\);/,
        replacement: MYSQL2_FREE_WRAPPER
      },
      {
        match: /NIL_P\(wrapper->active_thread\)/,
        replacement: 'NIL_P(rb_tr_managed_from_handle(wrapper->active_thread))'
      },
      {
        match: /rb_mysql_result_to_obj\(self, wrapper->encoding, current, result, Qnil\);/,
        replacement: 'rb_mysql_result_to_obj(self, rb_tr_managed_from_handle(wrapper->encoding), current, result, Qnil);'
      },
      {
        match: /rb_iv_get\(async_args->self,/,
        replacement: 'rb_iv_get(rb_tr_managed_from_handle(async_args->self),'
      },
      {
        match: /wrapper->active_thread = thread_current;/,
        replacement: 'wrapper->active_thread = rb_tr_handle_for_managed(thread_current);'
      },
      {
        match: /wrapper->active_thread == thread_current/,
        replacement: 'rb_tr_managed_from_handle(wrapper->active_thread) == thread_current'
      },
      {
        match: /rb_inspect\(wrapper->active_thread\)/,
        replacement: 'rb_inspect(rb_tr_managed_from_handle(wrapper->active_thread))'
      },
      {
        match: /args.sql = rb_str_export_to_enc\(sql, rb_to_encoding\(wrapper->encoding\)\);/,
        replacement: 'args.sql = rb_tr_handle_for_managed_leaking(rb_str_export_to_enc(sql, rb_to_encoding(rb_tr_managed_from_handle(wrapper->encoding)));'
      },
      {
        match: /args.sql = sql/,
        replacement: 'args.sql = rb_tr_handle_for_managed_leaking(sql)'
      },
      {
        match: /args\.sql_ptr = RSTRING_PTR\(args\.sql\);/,
        replacement: 'args.sql_ptr = RSTRING_PTR(rb_tr_managed_from_handle(args.sql));'
      },
      {
        match: /args\.sql_len = RSTRING_LEN\(args\.sql\);/,
        replacement: 'args.sql_len = RSTRING_LEN(rb_tr_managed_from_handle(args.sql));'
      },
      {
        match: /async_args\.self = self;/,
        replacement: 'async_args.self = rb_tr_handle_for_managed_leaking(self);'
      },
      {
        match: /conn_enc = rb_to_encoding\(wrapper->encoding\);/,
        replacement: 'conn_enc = rb_to_encoding(rb_tr_managed_from_handle(wrapper->encoding));'
      },
      {
        match: /wrapper->encoding = rb_enc;/,
        replacement: 'wrapper->encoding = rb_tr_handle_for_managed(rb_enc);'
      }
    ]
  },
  'result.c' => {
    gem: 'mysql2',
    patches: [
      {
        match: /wrapper->statement != Qnil/,
        replacement: 'rb_tr_managed_from_handle(wrapper->statement) != Qnil'
      },
      {
        match: /wrapper->client != Qnil/,
        replacement: 'rb_tr_managed_from_handle(wrapper->client) != Qnil'
      },
      {
        match: /xfree\(wrapper\);/,
        replacement: MYSQL2_FREE_RESULT_WRAPPER
      },
      {
        match: /wrapper->fields == Qnil/,
        replacement: 'rb_tr_managed_from_handle(wrapper->fields) == Qnil'
      },
      {
        match: /wrapper->fields = rb_ary_new2\(wrapper->numberOfFields\);/,
        replacement: 'wrapper->fields = rb_tr_handle_for_managed(rb_ary_new2(wrapper->numberOfFields));'
      },
      {
        match: /rb_ary_entry\(wrapper->fields,/,
        replacement: 'rb_ary_entry(rb_tr_managed_from_handle(wrapper->fields),'
      },
      {
        match: /rb_to_encoding\(wrapper->encoding\)/,
        replacement: 'rb_to_encoding(rb_tr_managed_from_handle(wrapper->encoding))'
      },
      {
        match: /rb_ary_store\(wrapper->fields,/,
        replacement: 'rb_ary_store(rb_tr_managed_from_handle(wrapper->fields),'
      },
      {
        match: /RARRAY_LEN\(wrapper->fields\)/,
        replacement: 'RARRAY_LEN(rb_tr_managed_from_handle(wrapper->fields))'
      },
      {
        match: /return wrapper->fields;/,
        replacement: 'return rb_tr_managed_from_handle(wrapper->fields);'
      },
      {
        match: /wrapper->rows == Qnil/,
        replacement: 'rb_tr_managed_from_handle(wrapper->rows) == Qnil'
      },
      {
        match: /wrapper->rows = rb_ary_new\(\);/,
        replacement: 'wrapper->rows = rb_tr_managed_from_handle(rb_ary_new());'
      },
      {
        match: /rb_yield\(rb_ary_entry\(wrapper->rows, i\)\);/,
        replacement: 'rb_yield(rb_ary_entry(rb_tr_managed_from_handle(wrapper->rows), i));'
      },
      {
        match: /RARRAY_LEN\(wrapper->rows\)/,
        replacement: 'RARRAY_LEN(rb_tr_managed_from_handle(wrapper->rows))'
      },
      {
        match: /rb_ary_entry\(wrapper->rows,/,
        replacement: 'rb_ary_entry(rb_tr_managed_from_handle(wrapper->rows),'
      },
      {
        match: /rb_ary_store\(wrapper->rows,/,
        replacement: 'rb_ary_store(rb_tr_managed_from_handle(wrapper->rows),'
      },
      {
        match: /return wrapper->rows;/,
        replacement: 'return rb_tr_managed_from_handle(wrapper->rows);'
      },
      {
        match: /wrapper->rows = rb_ary_new2\(wrapper->numberOfRows\);/,
        replacement: 'wrapper->rows = rb_tr_handle_for_managed(rb_ary_new2(wrapper->numberOfRows));'
      },
      {
        match: /args\.db_timezone = db_timezone;/,
        replacement: 'args.db_timezone = rb_tr_handle_for_managed_leaking(db_timezone);'
      },
      {
        match: /args\.app_timezone = app_timezone;/,
        replacement: 'args.app_timezone = rb_tr_handle_for_managed_leaking(app_timezone);'
      },
      {
        match: /args\.block_given = block;/,
        replacement: 'args.block_given = rb_tr_handle_for_managed_leaking(block);'
      },
      {
        match: /wrapper->fields = Qnil/,
        replacement: 'wrapper->fields = rb_tr_handle_for_managed(Qnil)'
      },
      {
        match: /wrapper->rows = Qnil/,
        replacement: 'wrapper->rows = rb_tr_handle_for_managed(Qnil)'
      },
      {
        match: /wrapper->encoding = encoding/,
        replacement: 'wrapper->encoding = rb_tr_handle_for_managed(encoding)'
      },
      {
        match: /wrapper->client = client/,
        replacement: 'wrapper->client = rb_tr_handle_for_managed(client)'
      },
      {
        match: /wrapper->statement = statement/,
        replacement: 'wrapper->statement = rb_tr_handle_for_managed_leaking(statement)'
      }
    ]
  },
  'statement.c' => {
    gem: 'mysql2',
    patches: [
      {
        match: /rb_to_encoding\(wrapper->encoding\)/,
        replacement: 'rb_to_encoding(rb_tr_managed_from_handle(wrapper->encoding))'
      },
      {
        match: /wrapper->active_thread = Qnil/,
        replacement: 'wrapper->active_thread = rb_tr_handle_for_managed(Qnil)'
      },
      {
        match: /rb_mysql_result_to_obj\(stmt_wrapper->client, wrapper->encoding, current, metadata, self\)/,
        replacement: 'rb_mysql_result_to_obj(stmt_wrapper->client, rb_tr_managed_from_handle(wrapper->encoding), current, metadata, self)'
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
  if patched_file = PATCHED_FILES[file]
    regexp = /^#{Regexp.escape(patched_file[:gem])}\b/
    if directory.split('/').last(2).any? { |part| part =~ regexp }
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
