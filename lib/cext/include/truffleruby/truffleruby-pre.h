/*
* Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved. This
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

#define TRUFFLERUBY

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
#define USE_RGENGC 0
#define USE_RINCGC 0

// Value types

typedef unsigned long VALUE;
typedef unsigned long ID;

POLYGLOT_DECLARE_TYPE(VALUE)

// Support

extern void* rb_tr_cext;
#define RUBY_CEXT rb_tr_cext

// Wrapping and unwrapping of values.

extern void* (*rb_tr_unwrap)(VALUE obj);
extern VALUE (*rb_tr_wrap)(void *obj);
extern VALUE (*rb_tr_longwrap)(long obj);
extern void* (*rb_tr_id2sym)(ID obj);
extern ID (*rb_tr_sym2id)(VALUE sym);

#include <ruby/thread_native.h>

// Helpers

#ifndef offsetof
#define offsetof(p_type,field) ((size_t)&(((p_type *)0)->field))
#endif

// Defines

// To support racc releases before https://github.com/ruby/racc/pull/165
#define HAVE_RB_BLOCK_CALL

#if defined(__cplusplus)
}
#endif

#endif
