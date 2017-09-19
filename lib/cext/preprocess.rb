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

def read_field(struct_var_name, field_name)
  { 
    match: /\b#{struct_var_name}(\.|->)#{field_name}\s*([\),;]|==|!=)/,
    replacement: "rb_tr_managed_from_handle(#{struct_var_name}\\1#{field_name}) \\2"
  }
end

def write_field(struct_var_name, field_name, leaking)
  leaking_str = leaking ? '_leaking': ''
  {
    match: /\b#{struct_var_name}(\.|->)#{field_name}\s*=\s*(\w.+);\s*$/ ,
    replacement: "#{struct_var_name}\\1#{field_name} = rb_tr_handle_for_managed#{leaking_str}(\\2);"
  }
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
        read_field('wrapper','active_thread'),
        write_field('wrapper','active_thread', false),
        read_field('wrapper','encoding'),
        write_field('wrapper','encoding', false),
        read_field('async_args','self'),
        write_field('async_args','self', true),
        read_field('args','sql'),
        write_field('args','sql', true),
      {
        match: /nogvl_close\(wrapper\);/,
        replacement: MYSQL2_FREE_WRAPPER
      }
    ]
  },
  'result.c' => {
    gem: 'mysql2',
    patches: [
        read_field('wrapper','statement'),
        write_field('wrapper','statement', true),
        read_field('wrapper','client'),
        write_field('wrapper','client', false),
        read_field('wrapper','encoding'),
        write_field('wrapper','encoding', false),
        read_field('wrapper','fields'),
        write_field('wrapper','fields', false),
        read_field('wrapper','rows'),
        write_field('wrapper','rows', false),
        read_field('args','db_timezone'),
        write_field('args','db_timezone', true),
        read_field('args','app_timezone'),
        write_field('args','app_timezone', true),
        read_field('args','block_given'),
        write_field('args','block_given', true),
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
        read_field('stmt_wrapper','client'),
        write_field('stmt_wrapper','client', false),
        read_field('args','sql'),
        write_field('args','sql', true),
      {
        match: /if \(stmt_wrapper->refcount == 0\) {/,
        replacement: MYSQL2_FREE_STATEMENT
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
