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

  PG_TEXT_ENC_INTEGER_OLD = <<-EOF
static int
pg_text_enc_integer(t_pg_coder *this, VALUE value, char *out, VALUE *intermediate, int enc_idx)
{
	if(out){
		if(TYPE(*intermediate) == T_STRING){
			return pg_coder_enc_to_s(this, value, out, intermediate, enc_idx);
		}else{
			char *start = out;
			int len;
			int neg = 0;
			long long ll = NUM2LL(*intermediate);

			if (ll < 0) {
				/* We don't expect problems with the most negative integer not being representable
				 * as a positive integer, because Fixnum is only up to 63 bits.
				 */
				ll = -ll;
				neg = 1;
			}

			/* Compute the result string backwards. */
			do {
				long long remainder;
				long long oldval = ll;

				ll /= 10;
				remainder = oldval - ll * 10;
				*out++ = '0' + remainder;
			} while (ll != 0);

			if (neg)
				*out++ = '-';

			len = out - start;

			/* Reverse string. */
			out--;
			while (start < out)
			{
				char swap = *start;

				*start++ = *out;
				*out-- = swap;
			}

			return len;
		}
	}else{
		*intermediate = pg_obj_to_i(value);
		if(TYPE(*intermediate) == T_FIXNUM){
			int len;
			long long sll = NUM2LL(*intermediate);
			long long ll = sll < 0 ? -sll : sll;
			if( ll < 100000000 ){
				if( ll < 10000 ){
					if( ll < 100 ){
						len = ll < 10 ? 1 : 2;
					}else{
						len = ll < 1000 ? 3 : 4;
					}
				}else{
					if( ll < 1000000 ){
						len = ll < 100000 ? 5 : 6;
					}else{
						len = ll < 10000000 ? 7 : 8;
					}
				}
			}else{
				if( ll < 1000000000000LL ){
					if( ll < 10000000000LL ){
						len = ll < 1000000000LL ? 9 : 10;
					}else{
						len = ll < 100000000000LL ? 11 : 12;
					}
				}else{
					if( ll < 100000000000000LL ){
						len = ll < 10000000000000LL ? 13 : 14;
					}else{
						return pg_coder_enc_to_s(this, *intermediate, NULL, intermediate, enc_idx);
					}
				}
			}
			return sll < 0 ? len+1 : len;
		}else{
			return pg_coder_enc_to_s(this, *intermediate, NULL, intermediate, enc_idx);
		}
	}
}
EOF

  PG_TEXT_ENC_INTEGER_NEW = <<-EOF
static int
pg_text_enc_integer(t_pg_coder *this, VALUE value, char *out, VALUE *intermediate, int enc_idx)
{
	if(out){
		if(TYPE(*intermediate) == T_STRING){
			return pg_coder_enc_to_s(this, value, out, intermediate, enc_idx);
		}else{
			char *start = out;
			int len;
			int neg = 0;
			long long ll = NUM2LL(*intermediate);
            unsigned long long ull = ll;

			if (ll < 0) {
                ull = ~ll;
                ull++;
				neg = 1;
			}

			/* Compute the result string backwards. */
			do {
				unsigned long long remainder;
				unsigned long long oldval = ull;

				ull /= 10;
				remainder = oldval - ull * 10;
				*out++ = '0' + remainder;
			} while (ull != 0);

			if (neg)
				*out++ = '-';

			len = out - start;

			/* Reverse string. */
			out--;
			while (start < out)
			{
				char swap = *start;

				*start++ = *out;
				*out-- = swap;
			}

			return len;
		}
	}else{
		*intermediate = pg_obj_to_i(value);
		if(TYPE(*intermediate) == T_FIXNUM){
			int len;
			long long sll = NUM2LL(*intermediate);
			unsigned long long ull = sll < 0 ? ((unsigned long long) ~sll) + 1 : (unsigned long long) sll;
			if( ull < 100000000 ){
				if( ull < 10000 ){
					if( ull < 100 ){
						len = ull < 10 ? 1 : 2;
					}else{
						len = ull < 1000 ? 3 : 4;
					}
				}else{
					if( ull < 1000000 ){
						len = ull < 100000 ? 5 : 6;
					}else{
						len = ull < 10000000 ? 7 : 8;
					}
				}
			}else{
				if( ull < 1000000000000LL ){
					if( ull < 10000000000LL ){
						len = ull < 1000000000LL ? 9 : 10;
					}else{
						len = ull < 100000000000LL ? 11 : 12;
					}
				}else{
					if( ull < 100000000000000LL ){
						len = ull < 10000000000000LL ? 13 : 14;
					}else{
						return pg_coder_enc_to_s(this, *intermediate, NULL, intermediate, enc_idx);
					}
				}
			}
			return sll < 0 ? len+1 : len;
		}else{
			return pg_coder_enc_to_s(this, *intermediate, NULL, intermediate, enc_idx);
		}
	}
}
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
          match: PG_TEXT_ENC_INTEGER_OLD,
          replacement: PG_TEXT_ENC_INTEGER_NEW
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
