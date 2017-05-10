# Executables in `bin` 

## Requirements

1.  [x] When TruffleRuby tar (GraalVM) is downloaded and unpacked, all files in `bin` and 
    `language/ruby/bin` has to always run the TruffleRuby from the tar. 
    -   Therefore `PATH` cannot be involved (`/usr/bin/env` cannot be used in shebang).
    -   There cannot be absolute paths in `bin` executables.
    -   All scripts in `bin` have to always resolve to `truffleruby` in the same dir.
2.  [x] Newly installed gem executables has to always run the `truffleruby` from the same `bin`. 
    -   By default RubyGems generate executables with absolute path.
3.  [x] Behave as proper Ruby implementation when the `bin` is put on `PATH`.
4.  [x] RubyMine requires `gem` in `bin` to work when executed with `truffleruby -x` option 
    (otherwise gems are not listed).
    -   `-x` option has to be implemented
5.  [x] `ruby -S irb` when executed in a `bin` directory has to work. (Applies to other 
    executables in `bin` as well.)
    -   In MRI: When a file starting with a shebang not containing `ruby` is loaded the lines at 
        the begging are skipped until a shebang containing `'ruby'` is found (it has `-x` 
        option behaviour). We can implement it and reuse.
6.  [x] Any executable has to work when pwd is `bin` directory.
    -   Translates to `ruby -S executable` 
7.  [x] Works on Linux, Solaris, macOS.
    -   Currently works.
8.  [ ] Works with Ruby managers. (Stacked shebangs cause problems, since the `execve` can 
    resolve only one level)
    -   `truffleruby` has to be binary executable.
    -   Issues are observable with rbenv on macOS with fish shell.

## Solution

-   [ ] Implement command line option `-x` (MRI compatible)
-   [ ] Implement the same behaviour for files starting with non-ruby shebang (MRI compatible)

Then we can have a hybrid executables, an example of `gem` file follows:

```ruby
#!/usr/bin/env bash
exec "$(dirname $0)/truffleruby" "$(dirname $0)/gem" "$@" # ignored by Ruby interpreter
#!ruby
# ^ marks start of Ruby interpretation, content of current lib/bin follows   

#--
# Copyright 2006 by Chad Fowler, Rich Kilmer, Jim Weirich and others.
# All rights reserved.
# See LICENSE.txt for permissions.
#++

require 'rubygems'
require 'rubygems/gem_runner'
require 'rubygems/exceptions'

required_version = Gem::Requirement.new ">= 1.8.7"

unless required_version.satisfied_by? Gem.ruby_version then
  abort "Expected Ruby Version #{required_version}, is #{Gem.ruby_version}"
end

args = ARGV.clone

begin
  Gem::GemRunner.new.run args
rescue Gem::SystemExitException => e
  exit e.exit_code
end
```

If executed directly, bash interprets the file. It looks up correct ruby executable 
(`truffleruby` in the same directory) and executes the same file with it. When Ruby 
interpreter executes the file it ignores the first 2 lines running only the Ruby 
portion of the file.

-   [ ] `lib/bin/*` files can be removed
