# Configuring Ruby managers

It's recommended to add TruffleRuby to a Ruby manager for ease of use. You will
need to [install GraalVM and Ruby](installing.md) manually. There is no support
yet for automatically installing TruffleRuby in a version manager.

## rbenv

To add TruffleRuby to `rbenv` a symbolic link has to be added to the `versions` 
directory of rbenv:

```bash
$ ln -s path/to/graalvm/jre/languages/ruby "$RBENV_ROOT/versions/truffleruby"
$ rbenv shell truffleruby
$ ruby --version
```

## chruby

To add TruffleRuby to `chruby` a symbolic link has to be added to the
`$HOME/.rubies`  directory:

```bash
$ ln -s path/to/graalvm/jre/languages/ruby "$HOME/.rubies/truffleruby"
$ chruby truffleruby
$ ruby --version
```

## RVM

RVM has a command for adding a precompiled Ruby to the list of available rubies.

```bash
$ rvm mount path/to/graalvm/jre/languages/ruby -n truffleruby
$ rvm use ext-truffleruby
$ ruby --version
```

## macOS

Note that on macOS the path is slightly different, and will be
`path/to/graalvm/Contents/Home/jre/languages/ruby`.

## Using TruffleRuby without a Ruby manager

If you are using a Ruby manager like `rvm`, `rbenv`, or `chruby` and wish not to
add TruffleRuby to one of them make sure that the manager does not set
environment variables `GEM_HOME`, `GEM_PATH`, and `GEM_ROOT`. The variables
are picked up by TruffleRuby (as any other Ruby implementation would do)
causing TruffleRuby to pickup the wrong Gem home instead of its own.

One way to fix this for all sessions is to tell TruffleRuby to ignore `GEM_*`
variables and always use its own Gem home under `truffleruby/lib/ruby/gems`:

```bash
# In ~/.bashrc or ~/.zshenv
$ export TRUFFLERUBY_RESILIENT_GEM_HOME=true
```

It can also be fixed just for the current terminal by clearing
the environment with one of the following commands:

```bash
$ rvm use system
$ rbenv system
$ chruby system
# Or manually:
$ unset GEM_HOME GEM_PATH GEM_ROOT
```
