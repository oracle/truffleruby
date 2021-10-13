#ifndef INTERNAL_GC_H                                    /*-*-C-*-vi:se ft=c:*/
#define INTERNAL_GC_H

RUBY_ATTR_MALLOC void *rb_xmalloc_mul_add(size_t, size_t, size_t);
#define ruby_sized_xfree(ptr, size) ruby_xfree(ptr)
#define SIZED_REALLOC_N(x, y, z, w) REALLOC_N(x, y, z)

#endif /* INTERNAL_GC_H */
