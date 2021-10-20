#ifndef INTERNAL_THREAD_H                                /*-*-C-*-vi:se ft=c:*/
#define INTERNAL_THREAD_H

#include "ruby.h"

VALUE rb_suppress_tracing(VALUE (*func)(VALUE), VALUE arg);

#endif /* INTERNAL_THREAD_H */
