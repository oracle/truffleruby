# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

class ByeBugPatches < CommonPatches

  BYEBUG_BYEBUG_INIT = <<-EOF
Init_byebug()
{
  tracing = Qfalse;
  post_mortem = Qfalse;
  verbose = Qfalse;
  catchpoints = Qnil;
  breakpoints = Qnil;
  tracepoints = Qnil;
  raised_exception = Qnil;
  threads = Qnil;
EOF

  BYEBUG_THREADS_INIT = <<-EOF
Init_threads_table(VALUE mByebug)
{
  next_thread = Qnil;
  locker = Qnil;
EOF

  PATCHES = {
    gem: 'byebug',
    patches: {
      'byebug.c' => [
        {
          match: /\b(static VALUE \w+) = (?:Qfalse|Qnil);/,
          replacement: '\1;'
        },
        {
          match: /^VALUE threads = Qnil;/,
          replacement: 'VALUE threads;'
        },
        {
          match: /Init_byebug\(\)\n{/,
          replacement: BYEBUG_BYEBUG_INIT
        }
      ],
      'threads.c' => [
        {
          match: /^((?:static )?VALUE \w+) = (?:Qfalse|Qnil);/,
          replacement: '\1;'
        },
        {
          match: /^VALUE next_thread = Qnil;/,
          replacement: 'VALUE next_thread;'
        },
        {
          match: /Init_threads_table\(VALUE mByebug\)\n{/,
          replacement: BYEBUG_THREADS_INIT
        }
      ]
    }
  }
end
