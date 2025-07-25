#!/usr/bin/env ruby

# Copyright (c) 2023, 2025 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

copyright = File.read(__FILE__)[/Copyright \(c\) (\d+, )?\d+ Oracle/]

NO_RETURN_FUNCTIONS = %w[
  ruby_malloc_size_overflow
  rb_error_arity
  rb_error_frozen_object
  rb_iter_break
  rb_iter_break_value
  rb_f_notimplement
  rb_exc_raise
  rb_jump_tag
  rb_syserr_fail
  rb_syserr_fail_str
  rb_sys_fail
  rb_sys_fail_str
  rb_throw
  rb_throw_obj
  rb_memerror
  rb_eof_error
  rb_tr_fatal_va_list
  rb_tr_bug_va_list
  rb_gvar_readonly_setter
  rb_num_zerodiv
  rb_tr_not_implemented
]

RETURN_STRUCT_BY_VALUE = {
  'rb_time_timeval' => 'rb_tr_time_timeval',
  'rb_time_timespec' => 'rb_tr_time_timespec',
}

type_regexp = /\b(?:(?:const|unsigned|volatile|struct|enum)\s+)*\w+\b(?:\s+const)?(?:\s*\*+\s*)?/
function_pointer_arg_regexp = /\b\w+\s*\(\*(\w+)\)\s*\([^)]*?\)/
argument_regexp = /\bvoid\b|\.\.\.|#{type_regexp}\s*(\w+)(?:\[\d*\])?|#{function_pointer_arg_regexp}/
arguments_regexp = /\(#{argument_regexp}(?:,\s*#{argument_regexp})*\)/
function_regexp = /(^(#{type_regexp})\s*(\w+)(#{arguments_regexp})\s*\{)$/

functions = []

Dir["src/main/c/cext/*.c"].sort.each do |file|
  next if %w[cext_constants.c ruby.c st.c strlcpy.c].include?(File.basename(file))

  contents = File.read(file)
  found_functions = contents.scan(function_regexp)

  found_functions_lines = found_functions.map(&:first)
  all_function_lines = contents.scan(/^\w.+\) \{$/).grep_v(/^static /)

  unless found_functions_lines == all_function_lines
    raise "Some functions could not be parsed:\nMissing:\n#{(all_function_lines - found_functions_lines)}\nExtra:\n#{(found_functions_lines - all_function_lines)}"
  end
  functions.concat found_functions
end

def struct_by_value?(type)
  type =~ /\bstruct\b/ and !type.include?('*')
end

functions.each do |declaration, return_type, function_name, argument_types|
  raise declaration if /\bstatic\b/ =~ declaration
  raise declaration if function_name.start_with?('rb_tr_init') and function_name != 'rb_tr_init_exception'
  if declaration.include? "\n"
    abort "This declaration includes newlines but should not:\n#{declaration}\n\n"
  end
  if struct_by_value?(return_type) and !RETURN_STRUCT_BY_VALUE.include?(function_name)
    abort "Returning a struct by value from Sulong to NFI is not supported for:\n#{declaration}"
  end
end

File.open("src/main/c/cext-trampoline/trampoline.c", "w") do |f|
  f.puts <<COPYRIGHT
/*
 * #{copyright} and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
COPYRIGHT
  f.puts
  f.puts "// GENERATED BY #{__FILE__}"
  f.puts
  f.puts <<C
#include <ruby.h>
#include <ruby/debug.h>
#include <ruby/encoding.h>
#include <ruby/io.h>
#include <ruby/ractor.h>
#include <ruby/thread_native.h>

#include <internal_all.h>

#include <trufflenfi.h>

#include <setjmp.h>

static TruffleContext* nfiContext = NULL;

static __thread jmp_buf *rb_tr_jmp_buf = NULL;

static inline bool rb_tr_exception_from_java(void) {
  TruffleEnv* env = (*nfiContext)->getTruffleEnv(nfiContext);
  return (*env)->exceptionCheck(env);
}

RBIMPL_ATTR_NORETURN()
static inline void rb_tr_exception_from_java_jump(const char *function) {
  if (LIKELY(rb_tr_jmp_buf != NULL)) {
    // fprintf(stderr, "There was an exception, longjmp()'ing");
    RUBY_LONGJMP(*rb_tr_jmp_buf, 1);
  } else {
    fprintf(stderr, "ERROR: There was a Java exception when returning from %s() but rb_tr_jmp_buf is NULL.", function);
    abort();
  }
}

C

  signatures = [
    ['rb_tr_setjmp_wrapper_void_to_void', '(void):void'],
    ['rb_tr_setjmp_wrapper_pointer1_to_void', '(VALUE arg):void'],
    ['rb_tr_setjmp_wrapper_pointer2_to_void', '(VALUE tracepoint, void *data):void'],
    ['rb_tr_setjmp_wrapper_pointer3_to_void', '(VALUE val, ID id, VALUE *data):void'],
    ['rb_tr_setjmp_wrapper_pointer3_to_int', '(VALUE key, VALUE val, VALUE arg):int'],
    ['rb_tr_setjmp_wrapper_pointer1_to_size_t', '(const void *arg):size_t'],
    ['rb_tr_setjmp_wrapper_int_pointer2_to_pointer', '(int argc, VALUE *argv, VALUE obj):VALUE'],
    ['rb_tr_setjmp_wrapper_pointer2_int_to_pointer', '(VALUE g, VALUE h, int r):VALUE'],
    ['rb_tr_setjmp_wrapper_pointer2_int_pointer2_to_pointer', '(VALUE yielded_arg, VALUE callback_arg, int argc, const VALUE *argv, VALUE blockarg):VALUE'],
  ]
  (1..16).each do |arity|
    signatures << [
      "rb_tr_setjmp_wrapper_pointer#{arity}_to_pointer",
      "(#{(1..arity).map { |i| "VALUE arg#{i}" }.join(', ')}):VALUE"
    ]
  end

  signatures.each do |function_name, signature|
    argument_types, return_type = signature.split(':')
    argument_types = argument_types.delete_prefix('(').delete_suffix(')')
    original_argument_types = argument_types
    argument_types = '' if argument_types == 'void'
    void = return_type == 'void'
    f.puts <<C
#{return_type} #{function_name}(#{return_type} (*func)(#{original_argument_types})#{', ' unless argument_types.empty?}#{argument_types}) {
  #{"#{return_type} result;" unless void}

  jmp_buf *prev_jmp_buf = rb_tr_jmp_buf;
  jmp_buf here;
  rb_tr_jmp_buf = &here;

  if (RUBY_SETJMP(here) == 0) {
    #{'result = ' unless void}func(#{argument_types.split(', ').map { |param| param[/\w+$/] }.join(', ')});
  } else {
    // fprintf(stderr, "Return from longjmp()");
    #{'result = Qundef; // The exception should be rethrown as soon as we are back in Java, so the return value should not matter' unless void}
  }

  rb_tr_jmp_buf = prev_jmp_buf;

  #{'return result;' unless void}
}

C
  end

  f.puts "\n// Functions\n\n"
  functions.each do |declaration, return_type, function_name, argument_types|
    next if RETURN_STRUCT_BY_VALUE.include?(function_name)
    f.puts "#undef #{function_name}"
    f.puts "static #{declaration.sub(/\{$/, '').rstrip.sub(function_name, "(*impl_#{function_name})")};"
  end
  f.puts

  functions.each do |declaration, return_type, function_name, argument_types|
    next if RETURN_STRUCT_BY_VALUE.include?(function_name)

    argument_names = argument_types.delete_prefix('(').delete_suffix(')')
    argument_names = argument_names.scan(/(?:^|,)\s*(#{argument_regexp})\s*(?=,|$)/o)
    argument_names = argument_names.map { |full_arg, name1, name2|
      if struct_by_value? full_arg
        abort "struct by value argument not well supported from NFI to Sulong: #{full_arg}\n#{declaration}"
      end

      if full_arg == "void"
        ""
      elsif full_arg == "..."
        raise "C does not allow to forward varargs:\n#{declaration}\nInstead copy the approach used for rb_yield_values() or rb_sprintf()\n\n"
      else
        name1 or name2 or raise "Could not find argument name for #{full_arg}"
      end
    }
    argument_names = argument_names.join(', ')
    argument_names = '' if argument_names == 'void'

    no_return = NO_RETURN_FUNCTIONS.include?(function_name)

    f.puts declaration
    if return_type.strip == 'void' or no_return
      f.puts "  impl_#{function_name}(#{argument_names});"
      if no_return
        f.puts "  if (LIKELY(rb_tr_exception_from_java())) {"
      else
        f.puts "  if (UNLIKELY(rb_tr_exception_from_java())) {"
      end
      f.puts "    rb_tr_exception_from_java_jump(#{function_name.dump});"
      f.puts "  }"
      f.puts "  UNREACHABLE;" if no_return
    else
      if function_name == 'ruby_native_thread_p'
        # We need to check if the current thread is attached first before calling to Sulong/Ruby
        f.puts "  if ((*nfiContext)->getTruffleEnv(nfiContext) == NULL) {"
        f.puts "    return 0;"
        f.puts "  }"
      end
      f.puts "  #{return_type} _result = impl_#{function_name}(#{argument_names});"
      f.puts "  if (UNLIKELY(rb_tr_exception_from_java())) {"
      f.puts "    rb_tr_exception_from_java_jump(#{function_name.dump});"
      f.puts "  }"
      f.puts "  return _result;"
    end
    f.puts "}"
    f.puts
  end

  f.puts <<C
// Return struct-by-value functions

struct timeval rb_time_timeval(VALUE time) {
  struct timeval result;
  rb_tr_time_timeval(time, &result);
  return result;
}

struct timespec rb_time_timespec(VALUE time) {
  struct timespec result;
  rb_tr_time_timespec(time, &result);
  return result;
}

C

  f.puts <<C
// Init functions

void rb_tr_trampoline_init_functions(TruffleEnv* env, void* (*get_libtruffleruby_function)(const char*)) {
  nfiContext = (*env)->getTruffleContext(env);

C
  functions.each do |declaration, return_type, function_name, argument_types|
    next if RETURN_STRUCT_BY_VALUE.include?(function_name)
    f.puts "  impl_#{function_name} = get_libtruffleruby_function(\"#{function_name}\");"
  end
  f.puts "}"
end
