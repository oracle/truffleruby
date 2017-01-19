# TruffleRuby Options and Command Line

TruffleRuby has the same command line interface as MRI 2.3.3.

```
Usage: ruby [switches] [--] [programfile] [arguments]
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

TruffleRuby also reads the `RUBYOPT` environment variable.

## TruffleRuby-specific switches

Beyond the standard Ruby command line switches we support some additional
switches specific to TruffleRuby.

```
TruffleRuby:
  -Xlog=severe,warning,performance,info,config,fine,finer,finest
                  set the TruffleRuby logging level
  -Xoptions       print available TrufleRuby options
  -Xname=value    set a TruffleRuby option (omit value to set to true)
```

TruffleRuby options can be set like this at the command line or using JVM system
properties (prefix the name with `org.truffleruby`, such as
`org.truffleruby.inline_js=true`) either with a `-J` switch, in `JAVA_OPTS` or
set by any other JVM mechanism for setting system properties.

TruffleRuby options set on the command line (or set in `PolyglotEngine` when
TruffleRuby is embedded) take priority over those set in system properties.

The logging level is not a TruffleRuby option like the others and so cannot be
set with a JVM system property. This is because the logger is once per VM,
rather than once per TruffleRuby instance, and is used to report problems
loading the TruffleRuby instance before options are loaded.

TruffleRuby-specific options, as well as conventional Ruby options, can also
bet set in the `TRUFFLERUBYOPT` environment variable.

## JVM- and SVM-specific arguments

If you are running TruffleRuby on a JVM or the GraalVM, we additionally support
passing options to the JVM using either a `-J:` or `-J-` prefix. For example
`-J:ea` or `-J-ea`.

```
JVM:
  -J:arg, -J-arg  pass arg to the JVM
```

TruffleRuby also supports the `JAVA_HOME`, `JAVACMD` and `JAVA_OPTS` environment
variables when running on a JVM using the `truffleruby` launcher script.

The SVM supports `-D` for setting system properties and `-XX:arg` for SVM
options.

For backwards compatibility, TruffleRuby temporarily also supports `JRUBY_OPTS`.

```
SVM:
  -Dname=value     set a system property
  -XX:arg          pass arg to the SVM
```

## Determining the TruffleRuby home

TruffleRuby needs to know where to locate files such as the standard library.
These are stored in the TruffleRuby home directory. The TruffleRuby option
`home` has priority for setting the home directory. Otherwise it is set
automatically to the directory containing the TruffleRuby JAR file, if
TruffleRuby is running on a JVM.
