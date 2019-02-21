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

TruffleRuby also reads the `RUBYOPT` environment variable, as in standard
Ruby, if run from the Ruby launcher.

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

## TruffleRuby options

TruffleRuby options are set via `--option=value`, or you can use
`--ruby.option=value` from any launcher. You can omit `=value` to set to `true`.

Available options and documentation can be seen with `--help:languages`.
Additionally set `--help:expert` and `--help:internal` to see those categories of
options. All options all experimental and subject to change at any time.

Options can also be set as JVM system properties, where they have a prefix
`polyglot.ruby.`. For example `--jvm.Dpolyglot.ruby.cexts.remap=true`, or via
any other way of setting JVM system properties. Finally, options can be set as
Graal-SDK polyglot API configuration options.

The priority for options is the command line first, then the Graal-SDK polyglot
API configuration, then system properties last.

TruffleRuby options, as well as conventional Ruby options and VM options, can
also bet set in the `TRUFFLERUBYOPT` and `RUBYOPT` environment variables, if
run from the Ruby launcher.

`--` or the first non-option argument stops processing of TrufflRuby and VM
options in the same way it stops processing of Ruby arguments.

## VM options

To set options in the underlying VM, use `--native.` in the native
configuration, and `--jvm.` in the JVM configuration.

For example `--native.Dsystem_property=value` or `--jvm.ea`.

To set the classpath, use the `=` notation, rather than two separate arguments.
For example `--jvm.cp=lib.jar` or `--jvm.classpath=lib.jar`.

## Other binary switches

Other binaries, such as `irb`, `gem`, and so on, support exactly the same
switches as in standard Ruby.

## Determining the TruffleRuby home

TruffleRuby needs to know where to locate files such as the standard library.
These are stored in the TruffleRuby home directory.

The search priority for finding Ruby home is:

* The value of the TruffleRuby `home` option (i.e., `--home=path/to/truffleruby_home`).
* The home that the Truffle framework reports.
* The parent of the directory containing the Ruby launcher executable.

If the `home` option is set, it's used even if it doesn't appear to be a correct
home location. Other options are tried until one is found that appears to be a
correct home location. If none appears to be correct a warning will be given but
the program will continue and you will not be able to require standard
libraries. You can tell TruffleRuby not to try to find a home at all using the
`no_home_provided` option.
