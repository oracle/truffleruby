# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

class JsonPatches < CommonPatches
  PATCHES = {
    gem: 'json',
    patches: {
      ['parser', 'parser.c'] => [
        *read_write_field('json','Vsource', false),
        *read_write_field('json','create_id', false),
        *read_write_field('json','object_class', false),
        *read_write_field('json','array_class', false),
        *read_write_field('json','decimal_class', false),
        *read_write_field('json','match_string', false),
        { # cParser_initialize
          match: /if \(rb_tr_managed_from_handle\(json->Vsource\)\)/,
          replacement: 'if (json->Vsource)'
        },
        { # cParser_parse
          match: /VALUE result = Qnil;/,
          replacement: "VALUE result_array_for_address[1];\nresult_array_for_address[0] = Qnil;"
        },
        { # cParser_parse
          match: /char \*np = JSON_parse_value\(json, p, pe, &result, 0\);/,
          replacement: 'char *np = JSON_parse_value(json, p, pe, result_array_for_address, 0);'
        },
        {
          match: /if \(cs >= JSON_first_final && p == pe\) {\s+return result;/m,
          replacement: "if (cs >= JSON_first_final && p == pe) {\n return result_array_for_address[0];"
        },
        { # JSON_parse_object
          match: /VALUE last_name = Qnil;/,
          replacement: "VALUE last_name_array_for_address[1];\nlast_name_array_for_address[0] = Qnil;"
        },
        { # JSON_parse_object
          match: /VALUE v = Qnil;/,
          replacement: "VALUE v_array_for_address[1];\nv_array_for_address[0] = Qnil;"
        },
        { # JSON_parse_object
          match: /np = JSON_parse_string\(json, p, pe, &last_name\);/,
          replacement: 'np = JSON_parse_string(json, p, pe, last_name_array_for_address);'
        },
        { # JSON_parse_object
          match: /rb_hash_aset\(\*result, last_name, v\);/,
          replacement: 'rb_hash_aset(*result, last_name_array_for_address[0], v_array_for_address[0]);'
        },
        { # JSON_parse_object
          match: /rb_funcall\(\*result, i_aset, 2, last_name, v\);/,
          replacement: 'rb_funcall(*result, i_aset, 2, last_name_array_for_address[0], v_array_for_address[0]);'
        },
        { # JSON_parse_object
          match: /char \*np = JSON_parse_value\(json, p, pe, &v, current_nesting\);/,
          replacement: 'char *np = JSON_parse_value(json, p, pe, v_array_for_address, current_nesting);'
        },
        { # JSON_parse_object
          match: /rb_ary_push\(\*result, v\);/,
          replacement: 'rb_ary_push(*result, v_array_for_address[0]);'
        },
        { # JSON_parse_object
          match: /rb_funcall\(\*result, i_leftshift, 1, v\);/,
          replacement: 'rb_funcall(*result, i_leftshift, 1, v_array_for_address[0]);'
        },
        { # JSON_parse_object
          match: /char \*np = JSON_parse_value\(json, p, pe, &v\);/,
          replacement: 'char *np = JSON_parse_value(json, p, pe, v_array_for_address);'
        },
        { # JSON_parse_string
          match: /if \(json->create_additions && RTEST\(match_string = rb_tr_managed_from_handle\(json->match_string\)\)\) {/,
          replacement: 'if (json->create_additions && RTEST(match_string = rb_tr_managed_from_handle_or_null(json->match_string))) {'
        },
        { # JSON_parse_string
          # Work around a bug in the json extension where it tries to call `rb_str_resize` on non-String objects.
          # We remove it entirely because the string resize is an MRI-optimization to reduce large preallocated strings
          # to embedded strings. We don't have that distinction in our implementation and the resize would be a wasteful operation.
          match: /rb_str_resize\(\*result, RSTRING_LEN\(\*result\)\);/,
          replacement: ''
        },
        { # cParser_parse_strict
          match: /np = JSON_parse_array\(json, p, pe, &result\);/,
          replacement: 'np = JSON_parse_array(json, p, pe, result_array_for_address);'
        },
        { # cParser_parse_strict
          match: /np = JSON_parse_object\(json, p, pe, &result\);/,
          replacement: 'np = JSON_parse_object(json, p, pe, result_array_for_address);'
        },
        { # cParser_parse_quirks_mode
          match: /if \(cs >= JSON_quirks_mode_first_final && p == pe\) {\s+return result;/m,
          replacement: "if (cs >= JSON_quirks_mode_first_final && p == pe) {\n return result_array_for_address[0];"
        },
        { # cParser_parse_quirks_mode
          match: /char \*np = JSON_parse_value\(json, p, pe, &result\);/,
          replacement: 'char *np = JSON_parse_value(json, p, pe, result_array_for_address);'
        }
      ],
      ['generator', 'generator.c'] => [
        { # generate_json
          match: /if \(obj == Qnil\)/,
          replacement: 'if (NIL_P(obj))'
        },
        { # generate_json
          match: /if \(obj == Qfalse\)/,
          replacement: 'if (rb_tr_obj_equal(Qfalse, obj))'
        },
        { # generate_json
          match: /if \(obj == Qtrue\)/,
          replacement: 'if (rb_tr_obj_equal(Qtrue, obj))'
        }
      ]
    }
  }
end
