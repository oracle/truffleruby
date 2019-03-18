# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Tested with pg version 0.21.0
class PgPatches

  PG_BINARY_ENCODER_PATCH = <<-EOF
    switch(rb_tr_to_int_const(value)){
        case Qtrue_int_const : mybool = 1; break;
        case Qfalse_int_const : mybool = 0; break;
EOF

  PG_TEXT_DECODER_FREE_OLD = <<-EOF
static VALUE
pg_text_dec_bytea(t_pg_coder *conv, const char *val, int len, int tuple, int field, int enc_idx)
EOF

  PG_TEXT_DECODER_FREE_NEW = <<-EOF
static VALUE pg_tr_pq_freemem(VALUE mem) {
  PQfreemem((void *)mem);
  return Qfalse;
}

static VALUE
pg_text_dec_bytea(t_pg_coder *conv, const char *val, int len, int tuple, int field, int enc_idx)
EOF

  PATCHES = {
    gem: 'pg',
    patches: {
      'pg_binary_encoder.c' => [
        {
          match: /[[:blank:]]*?switch\s*?\(.*?Qfalse\s*?:.*?break;/m,
          replacement: PG_BINARY_ENCODER_PATCH
        }
      ],
      'pg_result.c' => [
        {
          match: 'xmalloc(',
          replacement: 'calloc(1,'
        },
      ],
      'pg_tuple.c' => [
        {
          match: 'xmalloc(',
          replacement: 'calloc(1,'
        },
      ],
      'pg_text_decoder.c' => [
        {
          match: PG_TEXT_DECODER_FREE_OLD,
          replacement: PG_TEXT_DECODER_FREE_NEW
        },
        {
          match: '(VALUE(*)())PQfreemem',
          replacement: 'pg_tr_pq_freemem'
        }
      ],
      'pg_text_encoder.c' => [
        {
          match: 'if(TYPE(*intermediate) == T_FIXNUM)',
          replacement: 'if(TYPE(*intermediate) == T_FIXNUM && NUM2LL(*intermediate) != -(NUM2LL(*intermediate)))'
        },
      ],
      'pg_type_map_by_class.c' => [
        {
          match: '#define CACHE_LOOKUP(this, klass) ( &this->cache_row[(klass >> 8) & 0xff] )',
          replacement: '#define CACHE_LOOKUP(this, klass) ( &this->cache_row[(unsigned long)(rb_tr_obj_id(klass)) & 0xff] )'
        },
      ],
      'pg_type_map.c' => [
        # The result of rb_object_classname is used in an exception
        # string, We turn it into a ruby string to work round a bug in
        # our string formatting.
        {
          match: 'rb_obj_classname(self)',
          replacement: 'rb_str_new_cstr(rb_obj_classname(self))'
        }
      ],
    }
  }
end
