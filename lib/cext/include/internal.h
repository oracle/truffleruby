#ifndef RUBY_INTERNAL_H                                  /*-*-C-*-vi:se ft=c:*/
#define RUBY_INTERNAL_H 1
/**
 * @file
 * @author     $Author$
 * @date       Tue May 17 11:42:20 JST 2011
 * @copyright  Copyright (C) 2011 Yukihiro Matsumoto
 * @copyright  This  file  is   a  part  of  the   programming  language  Ruby.
 *             Permission  is hereby  granted,  to  either redistribute  and/or
 *             modify this file, provided that  the conditions mentioned in the
 *             file COPYING are met.  Consult the file for details.
 */
#include "ruby/internal/config.h"

#ifdef __cplusplus
# error not for C++
#endif

#define LIKELY(x) RB_LIKELY(x)
#define UNLIKELY(x) RB_UNLIKELY(x)

#include "ruby/ruby.h"

#endif /* RUBY_INTERNAL_H */
