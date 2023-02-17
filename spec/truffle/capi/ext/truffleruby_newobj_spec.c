/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include "ruby.h"
#include "rubyspec.h"

#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

static VALUE newobj_RB_NEWOBJ_defined(VALUE self) {
  #ifdef RB_NEWOBJ
    return Qtrue;
  #else
    return Qfalse;
  #endif
}

static VALUE newobj_NEWOBJ_defined(VALUE self) {
  #ifdef NEWOBJ
    return Qtrue;
  #else
    return Qfalse;
  #endif
}

static VALUE newobj_OBJSETUP_defined(VALUE self) {
  #ifdef OBJSETUP
    return Qtrue;
  #else
    return Qfalse;
  #endif
}

static VALUE newobj_NEWOBJ_OF_defined(VALUE self) {
  #ifdef NEWOBJ_OF
    return Qtrue;
  #else
    return Qfalse;
  #endif
}

static VALUE newobj_RB_NEWOBJ_OF_defined(VALUE self) {
  #ifdef RB_NEWOBJ_OF
    return Qtrue;
  #else
    return Qfalse;
  #endif
}

void Init_truffleruby_newobj_spec(void) {
  VALUE cls;
  cls = rb_define_class("CApiTruffleRubyNewobjSpecs", rb_cObject);
  rb_define_method(cls, "RB_NEWOBJ_defined?", newobj_RB_NEWOBJ_defined, 0);
  rb_define_method(cls, "NEWOBJ_defined?", newobj_NEWOBJ_defined, 0);
  rb_define_method(cls, "NEWOBJ_OF_defined?", newobj_NEWOBJ_OF_defined, 0);
  rb_define_method(cls, "RB_NEWOBJ_OF_defined?", newobj_RB_NEWOBJ_OF_defined, 0);
  rb_define_method(cls, "OBJSETUP_defined?", newobj_OBJSETUP_defined, 0);
}

#ifdef __cplusplus
}
#endif
