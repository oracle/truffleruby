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

  PATCHES = {
    gem: 'pg',
    patches: {
      'pg_binary_encoder.c' => [
        {
          match: /[[:blank:]]*?switch\s*?\(.*?Qfalse\s*?:.*?break;/m,
          replacement: PG_BINARY_ENCODER_PATCH
        }
      ],
      'pg_type_map_by_class.c' => [
        {
          match: /#define CACHE_LOOKUP\(this, klass\) \( &this->cache_row\[\(klass >> 8\) & 0xff\] \)/,
          replacement: '#define CACHE_LOOKUP(this, klass) ( &this->cache_row[FIX2INT(rb_obj_id(klass)) & 0xff] )'
        }
      ],
      'pg_coder.c' => [
        *read_write_field('conv','coder_obj', true),
        *read_write_field('this','coder_obj', true)
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
        {
          match: /pgconn_gc_free\( t_pg_connection \*this \)\n{/m,
          replacement: PG_CONNECTION_FREE
        }
      ]
    }
  }
end
