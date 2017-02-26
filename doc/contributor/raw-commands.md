# How To Get Raw Java Commands

For some development tasks, the layers of runners and helper commands and
launchers can get very deep. We think that they usually make sense for how we
develop day-to-day, but we know it be can be frustrating to have so many
processes between you and actually running a JVM.

This document explains how to cut through all that and get the actual `java`
command being run. I've used `>>>` to mean a user prompt, because the run
commands are then printed with `$`.

## How to get a raw command for running Ruby from `jt`

For an example command such as:

```
>>> jt run -e 'puts "hello"'
```

`jt` will automatically print the command that it runs, but this is just a
launcher shell script which itself runs `java`. To get that command you need
to use the `-J-cmd` option.

```
>>> jt run -J-cmd -e 'puts "hello"'
$ /Users/chrisseaton/Documents/ruby/truffleruby/bin/truffleruby -Xcore.load_path=/Users/chrisseaton/Documents/ruby/truffleruby/truffleruby/src/main/ruby -Xgraal.warn_unless=false -J-cmd -e 'puts "hello"'
$ /Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/bin/java -Dfile.encoding=UTF-8 -Xbootclasspath/a:/Users/chrisseaton/Documents/ruby/truffleruby/lib/truffleruby.jar org.truffleruby.Main -Xhome=/Users/chrisseaton/Documents/ruby/truffleruby -Xlauncher=/Users/chrisseaton/Documents/ruby/truffleruby/bin/truffleruby -Xcore.load_path=/Users/chrisseaton/Documents/ruby/truffleruby/truffleruby/src/main/ruby -Xgraal.warn_unless=false -e puts "hello"
hello
```

You can take that last command and run it using another `java`, run it using `mx
vm`, add extra JVM flags, or anything else you want.

## How to get a raw command for running specs

When you run specs you get yet another program in the stack, `mspec`. But it too
prints the command it runs, and you can add `-J-cmd` there as well, but you need
to prefix it with `-T` to tell `mspec` to pass it to the command it launches.

```
>>> jt test -T-J-cmd spec/ruby/language/if_spec.rb
$ ruby spec/mspec/bin/mspec run --config spec/truffle.mspec --excl-tag fails -T-J-cmd spec/ruby/language/if_spec.rb
$ /Users/chrisseaton/Documents/ruby/truffleruby/bin/truffleruby -J-ea -J-esa -J-Xmx2G -Xgraal.warn_unless=false -Xcore.load_path=/Users/chrisseaton/Documents/ruby/truffleruby/truffleruby/src/main/ruby -Xbacktraces.hide_core_files=false -J-cmd -v /Users/chrisseaton/Documents/ruby/truffleruby/spec/mspec/bin/mspec-run -B spec/truffle.mspec --excl-tag fails spec/ruby/language/if_spec.rb
$ /Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/bin/java -Dfile.encoding=UTF-8 -Xbootclasspath/a:/Users/chrisseaton/Documents/ruby/truffleruby/lib/truffleruby.jar -ea -esa -Xmx2G org.truffleruby.Main -Xhome=/Users/chrisseaton/Documents/ruby/truffleruby -Xlauncher=/Users/chrisseaton/Documents/ruby/truffleruby/bin/truffleruby -Xgraal.warn_unless=false -Xcore.load_path=/Users/chrisseaton/Documents/ruby/truffleruby/truffleruby/src/main/ruby -Xbacktraces.hide_core_files=false -v /Users/chrisseaton/Documents/ruby/truffleruby/spec/mspec/bin/mspec-run -B spec/truffle.mspec --excl-tag fails spec/ruby/language/if_spec.rb
truffleruby 0.SNAPSHOT, like ruby 2.3.3 <Java HotSpot(TM) 64-Bit Server VM 1.8.0_121-b13 without Graal> [darwin-x86_64]
[/ | ==================100%================== | 00:00:00]      0F      0E

Finished in 0.167000 seconds

1 file, 47 examples, 54 expectations, 0 failures, 0 errors, 0 tagged
```

Again, the last command that this prints is just a normal `java` command that
you can take and run somewhere else or modify.
