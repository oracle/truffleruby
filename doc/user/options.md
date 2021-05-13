---
layout: docs-experimental
toc_group: ruby
link_title: TruffleRuby Options and Command Line
permalink: /reference-manual/ruby/Options/
redirect_from: /docs/reference-manual/ruby/Options/
---
# TruffleRuby Options and Command Line

TruffleRuby has the same command-line interface as our compatible MRI version.

```shell
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
  -v              print the version number, then turn on verbose mode
  -w              turn warnings on for your script
  -W[level=2|:category]
                  set warning level; 0=silence, 1=medium, 2=verbose
  -x[directory]   strip off text before #!ruby line and perhaps cd to directory
  --copyright     print the copyright
  --enable={gems|rubyopt|...}[,...], --disable={gems|rubyopt|...}[,...]
                  enable or disable features. see below for available features
  --external-encoding=encoding, --internal-encoding=encoding
                  specify the default external or internal character encoding
  --verbose       turn on verbose mode and disable script from stdin
  --version       print the version number, then exit
  --help          show this message, -h for short message

Features:
  gems            rubygems (default: enabled)
  did_you_mean    did_you_mean (default: enabled)
  rubyopt         RUBYOPT environment variable (default: enabled)
  frozen-string-literal
                  freeze all string literals (default: disabled)

Warning categories:
  deprecated      deprecated features
  experimental    experimental features
```

TruffleRuby also reads the `RUBYOPT` environment variable, as in standard
Ruby, if run from the Ruby launcher.

## Unlisted Ruby Switches

MRI has some extra Ruby switches which are not normally listed in help output
but are documented in the Ruby manual page.

```
  -Xdirectory     cd to directory before executing your script (same as -C)
  -U              set the internal encoding to UTF-8
  -K[EeSsUuNnAa]  sets the source and external encoding
  --encoding=external[:internal]
                  the same as --external-encoding=external and optionally --internal-encoding=internal
```

## TruffleRuby Options

TruffleRuby options are set via `--option=value`, or you can use `--ruby.option=value` from any launcher.
You can omit `=value` to set to `true`.

Available options and documentation can be seen with `--help:languages`.
Additionally, set `--help:expert` and `--help:internal` to see those categories of options.
All options all experimental and subject to change at any time.

Options can also be set as JVM system properties, where they have a prefix `polyglot.ruby.`.
For example, `--vm.Dpolyglot.ruby.cexts.remap=true`, or via any other way of setting JVM system properties.
Finally, options can be set as GraalVM polyglot API configuration options.

The priority for options is the command line first, then the Graal-SDK polyglot API configuration, then system properties last.

TruffleRuby options, as well as conventional Ruby options and VM options, can also be set in the `TRUFFLERUBYOPT` and `RUBYOPT` environment variables, if run from the Ruby launcher.

`--` or the first non-option argument stops processing of TrufflRuby and VM options in the same way it stops processing of Ruby arguments.

## VM Options

To set options in the underlying VM, use `--vm.`, valid for both the native configuration and the JVM configuration.
For example, `--vm.Dsystem_property=value` or `--vm.ea`.

To set the classpath, use the `=` notation, rather than two separate arguments.
For example, `--vm.cp=lib.jar` or `--vm.classpath=lib.jar`.

## Other Binary Switches

Other binaries, such as `irb`, `gem`, and so on, support exactly the same switches as in standard Ruby.

## Determining the TruffleRuby Home

TruffleRuby needs to know where to locate files such as the standard library.
These are stored in the TruffleRuby home directory.
The Ruby home is always the one that the Truffle framework reports.

If the Ruby home appears not to be correct, or is unset, a warning will be given but the program will continue and you will not be able to require standard libraries.
You can tell TruffleRuby not to try to find a home at all using the `no-home-provided` option.
