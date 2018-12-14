# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Patches racc 1.4.14.

require_relative 'common_patches'

class RaccPatches < CommonPatches

  CPARSE_PARAM_VALUE_FIELDS = %w[value_v parser lexer action_table action_check action_default action_pointer
                                 goto_table goto_check goto_default goto_pointer reduce_table token_table state
                                 vstack tstack retval]

  def self.generate_cparse_param_handles
    CPARSE_PARAM_VALUE_FIELDS.collect do |field|
      read_write_field('v', field, true)
    end.flatten
  end

  PATCHES = {
    gem: 'racc',
    patches: {
      'cparse.c' => [
        { # Convert fixnum constants to ints because we convert cparse_params.t to an int.
            match: /INT2FIX\((\w+?_TOKEN)\)/,
            replacement: '\\1'
        },
        { # struct cparse_params. Creating a handle for a primitive value doesn't seem to work, so convert the `Fixnum` field to an `int` to avoid the need for a handle.
          match: 'VALUE t;',
          replacement: 'int t;'
        },
        { # initialize_params
          match: 'v->t = INT2FIX(FINAL_TOKEN + 1);',
          replacement: 'v->t = FINAL_TOKEN + 1;'
        },
        { # parse_main
          match: 'NUM2LONG(v->t)',
          replacement: 'v->t'
        },
        { # Change signature because when we call the callback we're not providing a self value -- difference from MRI.
            match: 'static VALUE reduce0 _((VALUE block_args, VALUE data, VALUE self));',
            replacement: 'static VALUE reduce0 _((VALUE block_args, VALUE data));'
        },
        { # Change signature because when we call the callback we're not providing a self value -- difference from MRI.
            match: 'reduce0(VALUE val, VALUE data, VALUE self)',
            replacement: 'reduce0(VALUE val, VALUE data)'
        },
        { # racc_cparse and racc_yyparse
          match: 'volatile VALUE',
          replacement: 'VALUE'
        },
        { # parse_main
          match: 'v->t = rb_hash_aref(v->token_table, tok);',
          replacement: 'VALUE tr_tmp = rb_hash_aref(v->token_table, tok == NULL ? Qfalse : rb_tr_managed_from_handle(tok));'
        },
        { # parse_main
          match: 'if (NIL_P(v->t)) {',
          replacement: 'if (!NIL_P(tr_tmp)) { v->t = tr_tmp; } else {'
        },
        { # parse_main
          match: '3, v->t, tok, val);',
          replacement: '3, v->t, tok, rb_tr_managed_from_handle(val));'
        },
        { # parse_main
          match: 'SHIFT(v, act, v->t, val);',
          replacement: 'SHIFT(v, act, v->t, rb_tr_managed_from_handle(val));'
        },
        { # parse_main
          match: '3, v->t, val, v->vstack);',
          replacement: '3, v->t, rb_tr_managed_from_handle(val), v->vstack);'
        },
        { # parse_main
          match: 'SHIFT(v, act, ERROR_TOKEN, val);',
          replacement: 'SHIFT(v, act, ERROR_TOKEN, rb_tr_managed_from_handle(val));'
        },
        { # extract_user_token
          match: '*tok = AREF(block_args, 0);',
          replacement: '*tok = rb_tr_handle_for_managed_leaking(AREF(block_args, 0));'
        },
        { # extract_user_token
          match: '*val = AREF(block_args, 1);',
          replacement: '*val = rb_tr_handle_for_managed_leaking(AREF(block_args, 1));'
        },
        { # extract_user_token
          match: '*tok = Qfalse;',
          replacement: '*tok = NULL;'
        },
        { # extract_user_token
          match: '*val = rb_str_new("$", 1);',
          replacement: '*val = rb_tr_handle_for_managed_leaking(rb_str_new("$", 1));'
        },
        { # lexer_i
          match: 'parse_main(v, tok, val, 1);',
          replacement: 'parse_main(v, rb_tr_managed_from_handle(tok), rb_tr_managed_from_handle(val), 1);'
        }
      ].concat(generate_cparse_param_handles)
    }
  }

end
