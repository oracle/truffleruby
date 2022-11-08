
#include "ruby/ruby.h"
#include "ruby/debug.h"
#include "ruby/encoding.h"
#include "debug_version.h"
//
static VALUE rb_mDebugger;

// iseq
typedef struct rb_iseq_struct rb_iseq_t;
VALUE rb_iseq_realpath(const rb_iseq_t *iseq);

static VALUE
iseq_realpath(VALUE iseqw)
{
    rb_iseq_t *iseq = DATA_PTR(iseqw);
    return rb_iseq_realpath(iseq);
}

static VALUE rb_cFrameInfo;

static VALUE
di_entry(VALUE loc, VALUE self, VALUE binding, VALUE iseq, VALUE klass, VALUE depth)
{
    return rb_struct_new(rb_cFrameInfo,
                         // :location, :self, :binding, :iseq, :class, :frame_depth,
                         loc, self, binding, iseq, klass, depth,
                         // :has_return_value, :return_value,
                         Qnil, Qnil,
                         // :has_raised_exception, :raised_exception,
                         Qnil, Qnil,
                         // :show_line, :local_variables
                         Qnil,
                         // :_local_variables, :_callee # for recorder
                         Qnil, Qnil,
                         // :dupped_binding
                         Qnil
                         );
}

static int
str_start_with(VALUE str, VALUE prefix)
{
    StringValue(prefix);
    rb_enc_check(str, prefix);
    if (RSTRING_LEN(str) >= RSTRING_LEN(prefix) &&
        memcmp(RSTRING_PTR(str), RSTRING_PTR(prefix), RSTRING_LEN(prefix)) == 0) {
        return 1;
    }
    else {
        return 0;
    }
}

static VALUE
di_body(const rb_debug_inspector_t *dc, void *ptr)
{
    VALUE skip_path_prefix = (VALUE)ptr;
    VALUE locs = rb_debug_inspector_backtrace_locations(dc);
    VALUE ary = rb_ary_new();
    long len = RARRAY_LEN(locs);
    long i;

    for (i=1; i<len; i++) {
        VALUE loc, e;
        VALUE iseq = rb_debug_inspector_frame_iseq_get(dc, i);

        if (!NIL_P(iseq)) {
            VALUE path = iseq_realpath(iseq);
            if (!NIL_P(path) && !NIL_P(skip_path_prefix) && str_start_with(path, skip_path_prefix)) continue;
        }

        loc = RARRAY_AREF(locs, i);
        e = di_entry(loc,
                     rb_debug_inspector_frame_self_get(dc, i),
                     rb_debug_inspector_frame_binding_get(dc, i),
                     iseq,
                     rb_debug_inspector_frame_class_get(dc, i),
                     INT2FIX(len - i));
        rb_ary_push(ary, e);
    }

    return ary;
}

static VALUE
capture_frames(VALUE self, VALUE skip_path_prefix)
{
    return rb_debug_inspector_open(di_body, (void *)skip_path_prefix);
}

static VALUE
frame_depth(VALUE self)
{
    // TODO: more efficient API
    VALUE bt = rb_make_backtrace();
    return INT2FIX(RARRAY_LEN(bt));
}

static void
method_added_tracker(VALUE tpval, void *ptr)
{
    rb_trace_arg_t *arg = rb_tracearg_from_tracepoint(tpval);
    VALUE mid = rb_tracearg_callee_id(arg);

    if (RB_UNLIKELY(mid == ID2SYM(rb_intern("method_added")) ||
                    mid == ID2SYM(rb_intern("singleton_method_added")))) {
        VALUE args[] = {
            tpval,
        };
        rb_funcallv(rb_mDebugger, rb_intern("method_added"), 1, args);
    }
}

static VALUE
create_method_added_tracker(VALUE self)
{
    return rb_tracepoint_new(0, RUBY_EVENT_CALL, method_added_tracker, NULL);
}

void Init_iseq_collector(void);

void
Init_debug(void)
{
    rb_mDebugger = rb_const_get(rb_cObject, rb_intern("DEBUGGER__"));
    rb_cFrameInfo = rb_const_get(rb_mDebugger, rb_intern("FrameInfo"));

    // Debugger and FrameInfo were defined in Ruby. We need to register them
    // as mark objects so they are automatically pinned.
    rb_gc_register_mark_object(rb_mDebugger);
    rb_gc_register_mark_object(rb_cFrameInfo);
    rb_define_singleton_method(rb_mDebugger, "capture_frames", capture_frames, 1);
    rb_define_singleton_method(rb_mDebugger, "frame_depth", frame_depth, 0);
    rb_define_singleton_method(rb_mDebugger, "create_method_added_tracker", create_method_added_tracker, 0);
    rb_define_const(rb_mDebugger, "SO_VERSION", rb_str_new2(RUBY_DEBUG_VERSION));
    Init_iseq_collector();
}
