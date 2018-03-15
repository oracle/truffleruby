class MySQL2Patches < CommonPatches
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

  PATCHES = {
    gem: 'mysql2',
    patches: {
      'client.c' => [
        *read_write_field('wrapper','active_thread', false),
        *read_write_field('wrapper','encoding', false),
        *read_write_field('async_args','self', true),
        *read_write_field('args','sql', true),
        {
          match: /nogvl_close\(wrapper\);/,
          replacement: MYSQL2_FREE_WRAPPER
        }
      ],
      'result.c' => [
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
      ],
      'statement.c' => [
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
  }
end
