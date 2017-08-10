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

PATCHED_FILES = {'xml_node_set.c'      => {:match =>  /[[:blank:]]*?switch\s*?\(.*?Qnil:/m, 
                                           :replacement => XML_NODE_SET_PATCH},
                 'pg_binary_encoder.c' => {:match => /[[:blank:]]*?switch\s*?\(.*?Qfalse\s*?:.*?break;/m,
                                           :replacement => PG_BINARY_ENCODER_PATCH}}

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

def patch(file, contents)
  if patch = PATCHED_FILES[file]
    contents = contents.gsub(patch[:match], patch[:replacement].rstrip)
  end
  contents
end

if __FILE__ == $0
  puts "#line 1 \"#{ARGF.filename}\""
  
  contents = patch(ARGF.filename, File.read(ARGF.filename))
  contents.each_line do |line|
    puts preprocess(line)
  end
end
