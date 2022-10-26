/*
* Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
* code is released under a tri EPL/GPL/LGPL license. You can use it,
* redistribute it and/or modify it under the terms of the:
*
* Eclipse Public License version 2.0
* GNU General Public License version 2
* GNU Lesser General Public License version 2.1
*
* This file contains code that is based on the Ruby API headers,
* copyright (C) Yukihiro Matsumoto, licensed under the 2-clause BSD licence.
*/

#ifndef TRUFFLERUBY_PRE_H
#define TRUFFLERUBY_PRE_H

#if defined(__cplusplus)
extern "C" {
#endif

RUBY_SYMBOL_EXPORT_BEGIN

#include <graalvm/llvm/polyglot.h>

#include <ctype.h> // isdigit

// Configuration

// We disable USE_FLONUM, as we do not use pointer tagging for Float.
// Enabling USE_FLONUM also changes the value of Qtrue/Qnil/Qundef.
#define USE_FLONUM 0

// We don't have a transient heap
#define USE_TRANSIENT_HEAP 0

// Sulong enforces calling functions with the correct arity, so we set this
// to catch rb_block_call_func_t* functions with incorrect arity faster.
#define RB_BLOCK_CALL_FUNC_STRICT 1

// To avoid extra write barrier code
#define USE_RINCGC 0

// Skip DTrace-generated code
#define DTRACE_PROBES_DISABLED 1

// Support

extern void* rb_tr_cext;
#define RUBY_CEXT rb_tr_cext

#ifndef TRUFFLERUBY_ABI_VERSION
#error "TRUFFLERUBY_ABI_VERSION must be defined when compiling native extensions. Does the extension override CPPFLAGS or DEFS?"
#endif
void* rb_tr_abi_version(void) __attribute__((weak));
void* rb_tr_abi_version(void) {
  const char* abi_version = STRINGIZE(TRUFFLERUBY_ABI_VERSION);
  return polyglot_from_string(abi_version, "US-ASCII");
}

// Wrapping and unwrapping of values.

#include "ruby/internal/value.h"

extern void* (*rb_tr_unwrap)(VALUE obj);
extern VALUE (*rb_tr_wrap)(void *obj);
extern VALUE (*rb_tr_longwrap)(long obj);
extern void* (*rb_tr_id2sym)(ID obj);
extern ID (*rb_tr_sym2id)(VALUE sym);
extern void* (*rb_tr_force_native)(VALUE obj);

// Helpers

#ifndef offsetof
#define offsetof(p_type,field) ((size_t)&(((p_type *)0)->field))
#endif

// Defines

// To support racc releases before https://github.com/ruby/racc/pull/165
#define HAVE_RB_BLOCK_CALL

RUBY_SYMBOL_EXPORT_END

#if defined(__cplusplus)
}
#endif

#endif
