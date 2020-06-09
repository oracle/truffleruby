#include <truffleruby-impl.h>

/*
 * Note that some functions are #undef'd just before declaration.
 * This is needed because these functions are declared by MRI as macros in e.g., ruby.h,
 * and so would produce invalid syntax when using the function name for definition.
 */

// Globals

void* rb_tr_cext;
void* (*rb_tr_unwrap)(VALUE obj);
VALUE (*rb_tr_wrap)(void *obj);
VALUE (*rb_tr_longwrap)(long obj);
void* (*rb_tr_id2sym)(ID id);
ID (*rb_tr_sym2id)(VALUE val);
bool (*rb_tr_is_native_object)(VALUE value);

void rb_tr_init_printf(void);
void rb_tr_init_exception(void);

// Run when loading C-extension support
void rb_tr_init(void *ruby_cext) {
  rb_tr_cext = ruby_cext;
  rb_tr_is_native_object = polyglot_invoke(rb_tr_cext, "rb_tr_is_native_object_function");
  rb_tr_unwrap = polyglot_invoke(rb_tr_cext, "rb_tr_unwrap_function");
  rb_tr_id2sym = polyglot_invoke(rb_tr_cext, "rb_tr_id2sym_function");
  rb_tr_sym2id = polyglot_invoke(rb_tr_cext, "rb_tr_sym2id_function");
  rb_tr_wrap = polyglot_invoke(rb_tr_cext, "rb_tr_wrap_function");
  rb_tr_longwrap = polyglot_invoke(rb_tr_cext, "rb_tr_wrap_function");

  rb_tr_init_exception();
  rb_tr_init_global_constants();
  rb_tr_init_printf();
}
