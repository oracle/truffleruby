#ifndef INTERNAL_STRING_H                                /*-*-C-*-vi:se ft=c:*/
#define INTERNAL_STRING_H

#define STR_EMBED_P(str)      (str, false)
#define STR_SHARED_P(str)     (str, false)

VALUE rb_fstring(VALUE);
VALUE rb_fstring_cstr(const char *str);

#endif /* INTERNAL_STRING_H */
