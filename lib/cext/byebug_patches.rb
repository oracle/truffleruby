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
    'byebug.c' => {
      gem: 'byebug',
      patches: [
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
      ]
    },
    'threads.c' => {
      gem: 'byebug',
      patches: [
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
    },
  }
end
