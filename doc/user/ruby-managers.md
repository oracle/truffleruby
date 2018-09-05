# Ruby managers and installers

If you [installed GraalVM](installing-graalvm.md), it is recommended to add
TruffleRuby to a Ruby manager. See
[Configuring Ruby managers for the full GraalVM distribution](#configuring-ruby-managers-for-the-full-graalvm-distribution)
below.

## Installing TruffleRuby with RVM, ruby-build or ruby-install

TruffleRuby is supported by the 3 major Ruby installers.

### RVM

Upgrade RVM to let RVM know about the latest TruffleRuby release:

```bash
rvm get head
```

Install TruffleRuby with:

```
rvm install truffleruby
```

### ruby-build and rbenv

We assume you already have [`ruby-build`](https://github.com/rbenv/ruby-build)
installed as a plugin for [`rbenv`](https://github.com/rbenv/rbenv).

First, you need to upgrade `ruby-build` to get the latest TruffleRuby
definition. See [`ruby-build`'s instructions for upgrading](https://github.com/rbenv/ruby-build#upgrading).

Check the latest available version of TruffleRuby with:

```bash
rbenv install --list | grep truffleruby
```

Then install the latest TruffleRuby with:

```bash
rbenv install truffleruby-[LATEST_VERSION]
```

### ruby-install and chruby

First, you need at least `ruby-install` 0.7.0 to get TruffleRuby support.
Check your version with:

```bash
ruby-install --version
```

If it is older than `0.7.0`, you need to update to latest `ruby-install`.
Follow the [installation instructions](https://github.com/postmodern/ruby-install#install),
since the steps for upgrading `ruby-install` are the same as the steps for
installing it.

Then install TruffleRuby with:

```bash
ruby-install --latest
ruby-install truffleruby
```

There are also instructions on the
[chruby wiki](https://github.com/postmodern/chruby/wiki/TruffleRuby)
if you prefer to install TruffleRuby manually.

## Configuring Ruby managers for the full GraalVM distribution

When [installing GraalVM](installing-graalvm.md), it is recommended to add
TruffleRuby to a Ruby manager for ease of use.

First, [install GraalVM and Ruby](installing-graalvm.md).
Make sure you ran the post-install script *before* adding GraalVM to Ruby managers.

Then follow these steps to integrate GraalVM with your Ruby manager.

### rbenv

To add TruffleRuby to `rbenv`, create a symbolic link in the `versions` directory
of rbenv:

```bash
$ ln -s path/to/graalvm/jre/languages/ruby "$RBENV_ROOT/versions/truffleruby"
$ rbenv shell truffleruby
$ ruby --version
```

### chruby

To add TruffleRuby to `chruby`, create a symbolic link to the `$HOME/.rubies` directory:

```bash
$ ln -s path/to/graalvm/jre/languages/ruby "$HOME/.rubies/truffleruby"
$ chruby truffleruby
$ ruby --version
```

### RVM

RVM has a command for adding a precompiled Ruby to the list of available rubies.

```bash
$ rvm mount path/to/graalvm/jre/languages/ruby -n truffleruby
$ rvm use ext-truffleruby
$ ruby --version
```

### macOS

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
$ rbenv system
$ chruby system
$ rvm use system
# Or manually:
$ unset GEM_HOME GEM_PATH GEM_ROOT
```
