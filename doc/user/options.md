# TruffleRuby Options and Command Line

TruffleRuby has the same command line interface as MRI 2.4.4.

```
Usage: truffleruby [switches] [--] [programfile] [arguments]
  -0[octal]       specify record separator (\0, if no argument)
  -a              autosplit mode with -n or -p (splits $_ into $F)
  -c              check syntax only
  -Cdirectory     cd to directory before executing your script
  -d, --debug     set debugging flags (set $DEBUG to true)
  -e 'command'    one line of script. Several -e's allowed. Omit [programfile]
  -Eex[:in], --encoding=ex[:in]
                  specify the default external and internal character encodings
  -Fpattern       split() pattern for autosplit (-a)
  -i[extension]   edit ARGV files in place (make backup if extension supplied)
  -Idirectory     specify $LOAD_PATH directory (may be used more than once)
  -l              enable line ending processing
  -n              assume 'while gets(); ... end' loop around your script
  -p              assume loop like -n but print line also like sed
  -rlibrary       require the library before executing your script
  -s              enable some switch parsing for switches after script name
  -S              look for the script using PATH environment variable
  -T[level=1]     turn on tainting checks
  -v, --verbose   print version number, then turn on verbose mode
  -w              turn warnings on for your script
  -W[level=2]     set warning level; 0=silence, 1=medium, 2=verbose
  -x[directory]   strip off text before #!ruby line and perhaps cd to directory
  --copyright     print the copyright
  --enable=feature[,...], --disable=feature[,...]
                  enable or disable features
  --external-encoding=encoding, --internal-encoding=encoding
                  specify the default external or internal character encoding
  --version       print the version
  --help          show this message, -h for short message

Features:
  gems            rubygems (default: enabled)
  did_you_mean    did_you_mean (default: enabled)
  rubyopt         RUBYOPT environment variable (default: enabled)
  frozen-string-literal
                  freeze all string literals (default: disabled)
```

TruffleRuby also reads the `RUBYOPT` environment variable, if run from the Ruby
launcher.

## Unlisted Ruby switches

MRI has some extra Ruby switches which are aren't normally listed in help output
but are documented in the Ruby manual page.

```
  -U              set the internal encoding to UTF-8
  -KEeSsUuNnAa    sets the source and external encoding
  --encoding=external[:internal]
                  the same as --external-encoding=external and optionally --internal-encoding=internal
  -y, --ydebug    debug the parser
  -Xdirectory     the same as -Cdirectory
  --dump=insns    print disassembled instructions
```

## TruffleRuby-specific switches

Beyond the standard Ruby command line switches we support some additional
switches specific to TruffleRuby.

```
TruffleRuby:
  -Xlog=SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST
                  set the TruffleRuby logging level
  -Xoptions       print available TruffleRuby options
  -Xname=value    set a TruffleRuby option (omit value to set to true)
  -J-option=value Translates to --jvm.option=value
```

As well as being set at the command line, options, except for `log`, can be
set using `--ruby.option=`. They can also be set as JVM system properties,
where they have a prefix `polyglot.ruby.`. For example
`--jvm.Dpolyglot.ruby.cexts.remap=true`, or via any other way of setting JVM
system properties. Finally, options can be set as Graal-SDK polyglot API
configuration options.

The priority for options is the command line first, then the Graal-SDK polyglot
API configuration, then system properties last.

The logging level is not a TruffleRuby option like the others and so cannot be
set with a JVM system property. This is because the logger is once per VM,
rather than once per TruffleRuby instance, and is used to report problems
loading the TruffleRuby instance before options are loaded.

TruffleRuby-specific options, as well as conventional Ruby options, can also
bet set in the `TRUFFLERUBYOPT` environment variable, if run from the Ruby
launcher.

`--` or the first non-option argument both stop processing of Truffle-specific
arguments in the same way it stops processing of Ruby arguments.

## JVM- and SVM-specific switches

If you are running TruffleRuby on a JVM or the GraalVM, we additionally support
passing options to the JVM using either a `-J-` or `--jvm.` prefix.
For example `-J-ea`. `-J-classpath` and `-J-cp` 
also implicitly take the following argument to be passed to the JVM.
`-J-cmd` print the Java command that will be executed, for
debugging. 

```
  --jvm.[option]  Pass options to the JVM; for example, '--jvm.classpath=myapp.jar'. To see available options. use '--jvm.help'.
```

`--` or the first non-option argument both stop processing of JVM-specific
arguments in the same way it stops processing of Ruby arguments.

TruffleRuby also supports the `JAVA_HOME`, `JAVACMD` and `JAVA_OPTS` environment
variables when running on a JVM (except for `JAVACMD` on the GraalVM).

## SVM-specific switches

The SVM supports `--native.D` for setting system properties and 
`--native.XX:arg` for SVM options. 

```
  --native.[option]  Pass options to the native image. To see available options, use '--native.help'.
```

## Other binary switches

Other binaries, such as `irb`, `gem`, and so on, support exactly the same
switches as in standard Ruby.

## TruffleRuby options

TruffleRuby options (set via `-Xoption=value`, or `--ruby.option=value`) are
all experimental and subject to change at any time. Available options and
documentation can be seen with `-Xoptions`.

## Determining the TruffleRuby home

TruffleRuby needs to know where to locate files such as the standard library.
These are stored in the TruffleRuby home directory.

The search priority for finding Ruby home is:

* The value of the TruffleRuby `home` option (i.e., `-Xhome=path/to/truffleruby_home`).
* The home that the Truffle framework reports.
* The parent of the directory containing the Ruby launcher executable.

If the `home` option is set, it's used even if it doesn't appear to be a correct
home location. Other options are tried until one is found that appears to be a
correct home location. If none appears to be correct a warning will be given but
the program will continue and you will not be able to require standard
libraries. You can tell TruffleRuby not to try to find a home at all using the
`no_home_provided` option.
