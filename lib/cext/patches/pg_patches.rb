# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'common_patches'

# Tested with pg version 0.21.0
class PgPatches < CommonPatches

  PG_BINARY_ENCODER_PATCH = <<-EOF
    switch(rb_tr_to_int_const(value)){
        case Qtrue_int_const : mybool = 1; break;
        case Qfalse_int_const : mybool = 0; break;
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

  # Using cached encodings requires more handle operations to store
  # objects in the st_table, so we'll simply avoid use of the cache
  # entirely.
  PG_GET_CACHED_ENCODING_ORIG = <<-EOF
rb_encoding *
pg_get_pg_encoding_as_rb_encoding( int enc_id )
{
	rb_encoding *enc;

	/* Use the cached value if it exists */
	if ( st_lookup(enc_pg2ruby, (st_data_t)enc_id, (st_data_t*)&enc) ) {
		return enc;
	}
	else {
		const char *name = pg_encoding_to_char( enc_id );

		enc = pg_get_pg_encname_as_rb_encoding( name );
		st_insert( enc_pg2ruby, (st_data_t)enc_id, (st_data_t)enc );

		return enc;
	}

}
EOF

  PG_GET_CACHED_ENCODING_NEW = <<-EOF
rb_encoding *
pg_get_pg_encoding_as_rb_encoding( int enc_id )
{
	const char *name = pg_encoding_to_char( enc_id );

	return pg_get_pg_encname_as_rb_encoding( name );
}
EOF

  # Async execution requires better support for the MRI file
  # descriptor API than we currently provide, so we'll substitute with
  # synchronous execution for now.
  PG_ASYNC_EXEC_ORIG = <<-EOF
static VALUE
pgconn_async_exec(int argc, VALUE *argv, VALUE self)
{
	VALUE rb_pgresult = Qnil;

	/* remove any remaining results from the queue */
	pgconn_block( 0, NULL, self ); /* wait for input (without blocking) before reading the last result */
	pgconn_get_last_result( self );

	pgconn_send_query( argc, argv, self );
	pgconn_block( 0, NULL, self );
	rb_pgresult = pgconn_get_last_result( self );

	if ( rb_block_given_p() ) {
		return rb_ensure( rb_yield, rb_pgresult, pg_result_clear, rb_pgresult );
	}
	return rb_pgresult;
}
EOF

  PG_ASYNC_EXEC_NEW = <<-EOF
static VALUE
pgconn_async_exec(int argc, VALUE *argv, VALUE self)
{
	return pgconn_exec(argc, argv, self);
}
EOF

  PG_CONN_QUOTE_IDENT_ORIG = <<-EOF
static VALUE
pgconn_s_quote_ident(VALUE self, VALUE str_or_array)
{
	VALUE ret;
	int enc_idx;

	if( rb_obj_is_kind_of(self, rb_cPGconn) ){
		enc_idx = ENCODING_GET( self );
	}else{
		enc_idx = RB_TYPE_P(str_or_array, T_STRING) ? ENCODING_GET( str_or_array ) : rb_ascii8bit_encindex();
	}
	pg_text_enc_identifier(NULL, str_or_array, NULL, &ret, enc_idx);

	OBJ_INFECT(ret, str_or_array);

	return ret;
}
EOF

  PG_CONN_QUOTE_IDENT_NEW = <<-EOF
static VALUE
pgconn_s_quote_ident(VALUE self, VALUE str_or_array)
{
	VALUE ret[1];
	int enc_idx;

	if( rb_obj_is_kind_of(self, rb_cPGconn) ){
		enc_idx = ENCODING_GET( self );
	}else{
		enc_idx = RB_TYPE_P(str_or_array, T_STRING) ? ENCODING_GET( str_or_array ) : rb_ascii8bit_encindex();
	}
	pg_text_enc_identifier(NULL, str_or_array, NULL, ret, enc_idx);

	OBJ_INFECT(ret[0], str_or_array);

	return ret[0];
}
EOF

  PG_RESULT_FIELDS_ORIG = <<-EOF
static VALUE
pgresult_fields(VALUE self)
{
	t_pg_result *this = pgresult_get_this_safe(self);

	if( this->nfields == -1 )
		pgresult_init_fnames( self );

	return rb_ary_new4( this->nfields, this->fnames );
}
EOF

  PG_RESULT_FIELDS_NEW = <<-EOF
static VALUE
pgresult_fields(VALUE self)
{
	t_pg_result *this = pgresult_get_this_safe(self);
	int i;
	VALUE res;

	if( this->nfields == -1 )
		pgresult_init_fnames( self );

	res = rb_ary_new2(this->nfields);
	for ( i = 0; i < this->nfields; i++) {
	  rb_ary_store(res, i, rb_tr_managed_from_handle_or_null(this->fnames[i]));
	}
	return res;
}
EOF

  PG_TUPLE_ALLOC_ORIG = <<-EOF
	this = (t_pg_tuple *)xmalloc(
		sizeof(*this) +
		sizeof(*this->values) * num_fields +
		sizeof(*this->values) * (dup_names ? 1 : 0));
EOF

  PG_TUPLE_ALLOC_NEW = <<-EOF
    this = (t_pg_tuple *)rb_tr_new_managed_struct(t_pg_tuple);
EOF

  PATCHES = {
    gem: 'pg',
    patches: {
      'pg.c' => [
        {
          match: PG_GET_CACHED_ENCODING_ORIG,
          replacement: PG_GET_CACHED_ENCODING_NEW
        }
      ],
      'pg_binary_encoder.c' => [
        {
          match: /[[:blank:]]*?switch\s*?\(.*?Qfalse\s*?:.*?break;/m,
          replacement: PG_BINARY_ENCODER_PATCH
        }
      ],
      'pg_type_map_by_class.c' => [
        *read_write_field('this->typemap', 'default_typemap', false),
        *read_write_field('this', 'self', false),
        *read_write_field('this', 'klass_to_coder', false),
        *read_write_field('p_ce', 'klass', false),
        {
          match: '#define CACHE_LOOKUP(this, klass) ( &this->cache_row[(klass >> 8) & 0xff] )',
          replacement: '#define CACHE_LOOKUP(this, klass) ( &this->cache_row[(unsigned long)(rb_tr_obj_id(klass)) & 0xff] )'
        },
      ],
      'pg_text_encoder.c' => [
        *replace_reference_passing_with_array('subint'),
      ],
      'pg_copy_coder.c' => [
        *read_write_field('this', 'typemap', false),
        *read_write_field('this', 'null_string', false),
        *replace_reference_passing_with_array('subint'),
      ],
      'pg_type_map_by_column.c' => [
        *read_write_field('this->typemap', 'default_typemap', false),
        *read_write_field('conv', 'coder_obj', false),
      ],
      'pg_type_map.c' => [
        *read_write_field('this', 'default_typemap', false),
        # The result of rb_object_classname is used in an exception
        # string, We turn it into a ruby string to work round a bug in
        # our string formatting.
        {
          match: 'rb_obj_classname(self)',
          replacement: 'rb_str_new_cstr(rb_obj_classname(self))'
        }
      ],
      'pg_type_map_by_mri_type.c' => [
        *read_write_field('this->typemap', 'default_typemap', false),
        *read_write_field('this->coders', 'ask_##type', false),
        *read_write_field('this->coders', 'coder_obj_##type', false),
      ],
      'pg_type_map_by_oid.c' => [
        *read_write_field('this->typemap', 'default_typemap', false),
        *read_write_field('p_colmap->typemap','default_typemap', false),
        {
          match: 'this->format[i].oid_to_coder = rb_hash_new();',
          replacement: 'this->format[i].oid_to_coder = rb_tr_handle_for_managed(rb_hash_new());'
        },
        {
          match: 'hash = this->format[p_coder->format].oid_to_coder;',
          replacement: 'hash = rb_tr_managed_from_handle_or_null(this->format[p_coder->format].oid_to_coder);'
        },
        {
          match: 'VALUE obj = rb_hash_lookup( this->format[format].oid_to_coder, UINT2NUM( oid ));',
          replacement: 'VALUE obj = rb_hash_lookup( rb_tr_managed_from_handle_or_null(this->format[format].oid_to_coder), UINT2NUM( oid ));'
        },
        {
          match: 'p_new_typemap->typemap.default_typemap = sub_typemap;',
          replacement: 'p_new_typemap->typemap.default_typemap = rb_tr_handle_for_managed(sub_typemap);'
        },
        {
          match: 'rb_hash_delete( hash, oid );',
          replacement: 'rb_hash_delete( rb_tr_managed_from_handle_or_null(hash), oid );'
        },
      ],
      'pg_type_map_in_ruby.c' => [
        *read_write_field('this->typemap', 'default_typemap', false),
		*read_write_field('p_new_typemap', 'default_typemap', false),
        *read_write_field('this', 'self', false),
      ],
      'pg_coder.c' => [
        *read_write_field('conv','coder_obj', true),
        *read_write_field('this','coder_obj', true),
        *replace_reference_passing_with_array('intermediate')
      ],
      'pg_result.c' => [
        *read_write_field('this','connection', false),
        *read_write_field('this','tuple_hash', false),
        *read_write_field('this','typemap', false),
        *read_write_field('p_conn','type_map_for_results', false),
        {
          match: 'this->fnames[i] = rb_obj_freeze(fname);',
          replacement: 'this->fnames[i] = rb_tr_handle_for_managed(rb_obj_freeze(fname));'
        },
        {
          match: 'rb_hash_aset( tuple, this->fnames[field_num], val )',
          replacement: 'rb_hash_aset( tuple, rb_tr_managed_from_handle_or_null(this->fnames[field_num]), val )'
        },
        {
		  match: PG_RESULT_FIELDS_ORIG,
		  replacement: PG_RESULT_FIELDS_NEW
        },
      ],
      'pg_connection.c' => [
        *read_write_field('this','socket_io', false),
        *read_write_field('this','notice_receiver', false),
        *read_write_field('this','notice_processor', false),
        *read_write_field('this','type_map_for_queries', false),
        *read_write_field('this','type_map_for_results', false),
        *read_write_field('this','trace_stream', false),
        *read_write_field('this','external_encoding', false),
        *read_write_field('this','encoder_for_put_copy_data', false),
        *read_write_field('this','decoder_for_get_copy_data', false),
        *replace_reference_passing_with_array('intermediate'),
        {
          match: 'PQsetNoticeProcessor(this->pgconn, gvl_notice_processor_proxy, (void *)self);',
          replacement: 'PQsetNoticeProcessor(this->pgconn, gvl_notice_processor_proxy, rb_tr_handle_for_managed(self));'
        },
        {
          match: 'VALUE self = (VALUE)arg;',
          replacement: 'VALUE self = rb_tr_managed_from_handle_or_null(arg);'
        },
        {
          match: 'pg_get_connection(self)->type_map_for_queries;',
          replacement: 'rb_tr_managed_from_handle_or_null(pg_get_connection(self)->type_map_for_queries);'
        },
        {
          match: /rb_scan_args\(argc, argv, "([0-9]+)", &(command|name), &paramsData.params, &in_res_fmt, &paramsData.typemap\);/,
          replacement: 'VALUE tmp_params, tmp_typemap; rb_scan_args(argc, argv, "\\1", &\\2, &tmp_params, &in_res_fmt, &tmp_typemap);
paramsData.params = tmp_params;
paramsData.typemap = tmp_typemap;'
        },
        *read_write_field('paramsData','params', false),
        *read_write_field('paramsData','typemap', false),
        *read_write_field('paramsData','heap_pool', false),
        *read_write_field('paramsData','gc_array', false),
        *read_write_field('paramsData','typecast_result_value', false),
        {
          match: 'paramsData->typecast_heap_chain = Qnil;',
          replacement: 'paramsData->typecast_heap_chain = rb_tr_handle_for_managed(Qnil);'
        },
        {
          match: /pgconn_gc_free\( t_pg_connection \*this \)\n{/m,
          replacement: PG_CONNECTION_FREE
        },
        {
          match: PG_ASYNC_EXEC_ORIG,
          replacement: PG_ASYNC_EXEC_NEW
        },
        {
          match: /\(\(VALUE\*\)args\)\[([0-9]+)\]/,
          replacement: 'rb_ary_entry(args, \\1)'
        },
        {
          match: 'VALUE args[] = { self, rb_str_new_cstr(encname) };',
          replacement: 'VALUE args = rb_ary_new_capa(2); rb_ary_store(args, 0, self); rb_ary_store(args, 1, rb_str_new_cstr(encname));'
        },
        {
          match: 'rb_rescue(pgconn_set_client_encoding_async1, (VALUE)&args, pgconn_set_client_encoding_async2, Qnil);',
          replacement: 'rb_rescue(pgconn_set_client_encoding_async1, args, pgconn_set_client_encoding_async2, Qnil);'
        },
        {
          match: PG_CONN_QUOTE_IDENT_ORIG,
          replacement: PG_CONN_QUOTE_IDENT_NEW
        },
        {
          match: '( *typecast_heap_chain )',
          replacement: '( rb_tr_managed_from_handle_or_null(*typecast_heap_chain) )'
        },
        {
          match: '*typecast_heap_chain = Data_Wrap_Struct( rb_cObject, NULL, free_typecast_heap_chain, allocated );',
          replacement: '*typecast_heap_chain = rb_tr_handle_for_managed(Data_Wrap_Struct( rb_cObject, NULL, free_typecast_heap_chain, allocated ));'
        }
      ],
      'pg_tuple.c' => [
        {
          match: 'VALUE values[0];',
          replacement: 'void *values[0];'
        },
        {
          match: '} t_pg_tuple;',
          replacement: '} t_pg_tuple; POLYGLOT_DECLARE_TYPE(t_pg_tuple);'
        },
        {
          match: PG_TUPLE_ALLOC_ORIG,
          replacement: PG_TUPLE_ALLOC_NEW
        }
      ]
    }
  }
end
