/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>

bool rb_warning_category_enabled_p(rb_warning_category_t category) {
    VALUE category_name;

    switch (category) {
        case RB_WARN_CATEGORY_DEPRECATED:
            category_name = ID2SYM(rb_intern("deprecated"));
            break;
        case RB_WARN_CATEGORY_EXPERIMENTAL:
            category_name = ID2SYM(rb_intern("experimental"));
            break;
        default:
            category_name = Qnil;
    }

    if (category_name != Qnil) {
        return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_warning_category_enabled_p", category_name));
    } else {
        return Qtrue;
    }
}
