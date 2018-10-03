# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'common_patches'

class SQLite3Patches < CommonPatches
  PATCHES = {
    gem: 'sqlite3',
    patches: {
      'database.c' => [
        { # tracefunc
          match: 'VALUE self = (VALUE)data;',
          replacement: 'VALUE self = rb_tr_managed_from_handle(data);'
        },
        { # trace
          match: 'sqlite3_trace(ctx->db, NIL_P(block) ? NULL : tracefunc, (void *)self);',
          replacement: 'sqlite3_trace(ctx->db, NIL_P(block) ? NULL : tracefunc, rb_tr_handle_for_managed_leaking(self));'
        },
        { # rb_sqlite3_busy_handler
          match: 'VALUE self = (VALUE)(ctx);',
          replacement: 'VALUE self = rb_tr_managed_from_handle(ctx);'
        },
        { # busy_handler
          match: 'ctx->db, NIL_P(block) ? NULL : rb_sqlite3_busy_handler, (void *)self);',
          replacement: 'ctx->db, NIL_P(block) ? NULL : rb_sqlite3_busy_handler, rb_tr_handle_for_managed_leaking(self));'
        },
        { # rb_sqlite3_func, rb_sqlite3_step, rb_sqlite3_final
          match: 'VALUE callable = (VALUE)sqlite3_user_data(ctx);',
          replacement: 'VALUE callable = rb_tr_managed_from_handle(sqlite3_user_data(ctx));'
        },
        { # define_function_with_flags
          match: '(void *)block,',
          replacement: 'rb_tr_handle_for_managed_leaking(block),'
        },
        { # rb_sqlite3_step
          match: 'params = xcalloc((size_t)argc, sizeof(VALUE *));',
          replacement: 'params = truffle_managed_malloc((size_t)argc * sizeof(VALUE *));'
        },
        { # rb_sqlite3_step
          match: 'xfree(params);',
          replacement: ''
        },
        { # define_aggregator
          match: '(void *)aggregator,',
          replacement: 'rb_tr_handle_for_managed_leaking(aggregator),'
        },
        { # rb_sqlite3_auth
          match: 'VALUE self   = (VALUE)ctx;',
          replacement: 'VALUE self   = rb_tr_managed_from_handle(ctx);'
        },
        { # rb_sqlite3_auth
          match: 'if(Qfalse == result) return SQLITE_DENY;',
          replacement: 'if(rb_tr_obj_equal(Qfalse, result)) return SQLITE_DENY;'
        },
        { # set_authorizer
          match: 'ctx->db, NIL_P(authorizer) ? NULL : rb_sqlite3_auth, (void *)self',
          replacement: 'ctx->db, NIL_P(authorizer) ? NULL : rb_sqlite3_auth, rb_tr_handle_for_managed_leaking(self)'
        },
        { # rb_comparator_func
          match: 'comparator = (VALUE)ctx;',
          replacement: 'comparator = rb_tr_managed_from_handle(ctx);'
        },
        { # collation
          match: '(void *)comparator,',
          replacement: 'rb_tr_handle_for_managed_leaking(comparator),'
        },
        { # enc_cb
          match: 'VALUE self = (VALUE)_self;',
          replacement: 'VALUE self = rb_tr_managed_from_handle(_self);'
        },
        { # db_encoding
          match: 'sqlite3_exec(ctx->db, "PRAGMA encoding", enc_cb, (void *)self, NULL);',
          replacement: '{void *handle = rb_tr_handle_for_managed(self); sqlite3_exec(ctx->db, "PRAGMA encoding", enc_cb, handle, NULL); rb_tr_release_handle(handle);}'
        },
        { # hash_callback_function
          match: 'rb_ary_push(callback_ary, new_hash);',
          replacement: 'rb_ary_push(rb_tr_managed_from_handle(callback_ary), new_hash);'
        },
        { # regular_callback_function
          match: 'rb_ary_push(callback_ary, new_ary);',
          replacement: 'rb_ary_push(rb_tr_managed_from_handle(callback_ary), new_ary);'
        },
        { # exec_batch
          match: 'status = sqlite3_exec(ctx->db, StringValuePtr(sql), hash_callback_function, callback_ary, &errMsg);',
          replacement: 'status = sqlite3_exec(ctx->db, StringValuePtr(sql), hash_callback_function, rb_tr_handle_for_managed_leaking(callback_ary), &errMsg);'
        },
        { # exec_batch
          match: 'status = sqlite3_exec(ctx->db, StringValuePtr(sql), regular_callback_function, callback_ary, &errMsg);',
          replacement: 'status = sqlite3_exec(ctx->db, StringValuePtr(sql), regular_callback_function, rb_tr_handle_for_managed_leaking(callback_ary), &errMsg);'
        }
      ]
    }
  }
end
