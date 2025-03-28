exclude :test_autosplit, "| truffleruby: the -F option is not implemented"
exclude :test_backtrace_limit, "| ERROR: Invalid argument --backtrace-limit specified. For input string: \"true\""
exclude :test_chdir, "| truffleruby: invalid argument"
exclude :test_copyright, "needs investigation"
exclude :test_crash_report, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_crash_report_executable_path, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_crash_report_pipe, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_crash_report_script, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_crash_report_script_path, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_cwd_encoding, "| <internal:core> core/kernel.rb:269:in `require': cannot load such file -- ./b (LoadError)"
exclude :test_disable, "slow: 30.37s on truffleruby 24.2.0-dev-b555f590, like ruby 3.2.4, GraalVM CE JVM [x86_64-linux] with AMD Ryzen 7 3700X 8-Core Processor (16 vCPUs)"
exclude :test_dump_insns_with_rflag, "| [ruby] WARNING the --dump=insns switch is silently ignored as it is an internal development tool"
exclude :test_dump_parsetree_with_rflag, "| [ruby] WARNING the --dump=parsetree switch is silently ignored as it is an internal development tool"
exclude :test_dump_syntax_with_rflag, "| :run"
exclude :test_dump_yydebug_with_rflag, "| [ruby] WARNING the -y switch is silently ignored as it is an internal development tool"
exclude :test_encoding, "| Exception during RubyContext.patch(): | java.lang.RuntimeException: unknown encoding name - test_ruby_test_rubyoptions_foobarbazqux"
exclude :test_eval, "| truffleruby: invalid argument"
exclude :test_flag_in_shebang, "needs investigation"
exclude :test_free_at_exit_env_var, "needs investigation"
exclude :test_frozen_string_literal, "slow: 37.48s on truffleruby 24.2.0-dev-b555f590, like ruby 3.2.4, GraalVM CE JVM [x86_64-linux] with AMD Ryzen 7 3700X 8-Core Processor (16 vCPUs)"
exclude :test_frozen_string_literal_debug, "[\"--disable=gems\", \"--enable-frozen-string-literal\", \"--debug-frozen-string-literal\"] \"foo\" << \"bar\""
exclude :test_indentation_check, "very slow: 456.06s on truffleruby 24.2.0-dev-b555f590, like ruby 3.2.4, GraalVM CE JVM [x86_64-linux] with AMD Ryzen 7 3700X 8-Core Processor (16 vCPUs)"
exclude :test_invalid_option, "| ERROR: truffleruby: invalid option -\r  (Use --help for usage instructions.)"
exclude :test_kanji, "needs investigation"
exclude :test_notfound, "| truffleruby: No such file or directory -- ./notexist.rb (LoadError)"
exclude :test_option_variables, "<[\"[true, true, true]\","
exclude :test_parser_flag, "| ERROR: truffleruby: invalid option --parser=prism  (Use --help for usage instructions.)"
exclude :test_pflag_gsub, "| -e:1:in `<main>': undefined method `gsub' for main:Object (NoMethodError)"
exclude :test_pflag_sub, "| -e:1:in `<main>': undefined method `sub' for main:Object (NoMethodError)"
exclude :test_program_name, "<\"-\"> expected but was <\"\">."
exclude :test_require, "| truffleruby: invalid argument"
exclude :test_rubyopt, "<\"\\\"あ\\\"\"> expected but was <\"\\\"\\\\u3042\\\"\">."
exclude :test_script_is_directory, "| truffleruby: Is a directory (java.io.IOException) (IOError)"
exclude :test_segv_loaded_features, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_segv_setproctitle, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_segv_test, "ArgumentError: unknown exec option: :rlimit_core"
exclude :test_separator, "Errno::EPIPE: Broken pipe"
exclude :test_setproctitle, "ArgumentError expected but nothing was raised."
exclude :test_sflag, "needs investigation"
exclude :test_shebang, "| -:1: warning: shebang line ending with \\r may cause problems"
exclude :test_syntax_check, "| SyntaxError in -e:1: Invalid break"
exclude :test_unused_variable, "| -e:1: warning: assigned but unused variable - a"
exclude :test_usage_long, "<[]> expected but was <[\"  --native                                     Run using the native launcher with limited access to Java libraries\","
exclude :test_verbose, "Expected /^ruby 3\\.3\\.5(?:[p ]|dev|rc).*? \\[x86_64\\-linux\\]$/ to match \"truffleruby 24.2.0-dev-d4d49440, like ruby 3.3.5, GraalVM CE Native [x86_64-linux]\"."
exclude :test_version, "Expected /^ruby 3\\.3\\.5(?:[p ]|dev|rc).*? \\[x86_64\\-linux\\]$/ to match \"truffleruby 24.2.0-dev-d4d49440, like ruby 3.3.5, GraalVM CE Native [x86_64-linux]\"."
exclude :test_warning, "needs investigation"
exclude :test_yydebug, "<[]> expected to be != to"
