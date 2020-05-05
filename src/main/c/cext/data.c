#include <truffleruby-impl.h>

// RData and RTypedData, rb_data_*, rb_typeddata_*

static RUBY_DATA_FUNC rb_tr_free_function(RUBY_DATA_FUNC dfree) {
  return (dfree == (RUBY_DATA_FUNC)RUBY_DEFAULT_FREE) ? free : dfree;
}

#undef rb_data_object_wrap
VALUE rb_data_object_wrap(VALUE klass, void *data, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_data_object_wrap",
                                            rb_tr_unwrap(klass), data, dmark, rb_tr_free_function(dfree) ));
}

VALUE rb_data_object_zalloc(VALUE klass, size_t size, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree) {
  void *data = calloc(1, size);
  return rb_data_object_wrap(klass, data, dmark, dfree);
}

VALUE rb_data_object_alloc_managed(VALUE klass, size_t size, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree, void *interoptypeid) {
  void *data = rb_tr_new_managed_struct_internal(interoptypeid);
  return rb_data_object_wrap(klass, data, dmark, (dfree == (RUBY_DATA_FUNC)RUBY_DEFAULT_FREE) ? NULL : dfree);
}

// Typed data

VALUE rb_data_typed_object_wrap(VALUE ruby_class, void *data, const rb_data_type_t *data_type) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_data_typed_object_wrap",
                                    rb_tr_unwrap(ruby_class), data, data_type, data_type->function.dmark, rb_tr_free_function(data_type->function.dfree), data_type->function.dsize));
}

VALUE rb_data_typed_object_zalloc(VALUE ruby_class, size_t size, const rb_data_type_t *data_type) {
  void *data = calloc(1, size);
  return rb_data_typed_object_wrap(ruby_class, data, data_type);
}

VALUE rb_data_typed_object_alloc_managed(VALUE ruby_class, size_t size, const rb_data_type_t *data_type, void *interoptypeid) {
  void *data = rb_tr_new_managed_struct_internal(interoptypeid);
  return rb_data_typed_object_wrap(ruby_class, data, data_type);
}

VALUE rb_data_typed_object_make(VALUE ruby_class, const rb_data_type_t *type, void **data_pointer, size_t size) {
  TypedData_Make_Struct0(result, ruby_class, void, size, type, *data_pointer);
  return result;
}

void *rb_check_typeddata(VALUE value, const rb_data_type_t *data_type) {
  if (RTYPEDDATA_TYPE(value) != data_type) {
    rb_raise(rb_eTypeError, "wrong argument type");
  }
  return RTYPEDDATA_DATA(value);
}

int rb_typeddata_inherited_p(const rb_data_type_t *child, const rb_data_type_t *parent) {
  while (child) {
    if (child == parent) {
      return 1;
    }
    child = child->parent;
  }
  return 0;
}

int rb_typeddata_is_kind_of(VALUE obj, const rb_data_type_t *data_type) {
  return RB_TYPE_P(obj, T_DATA) &&
    RTYPEDDATA_P(obj) &&
    rb_typeddata_inherited_p(RTYPEDDATA_TYPE(obj), data_type);
}
