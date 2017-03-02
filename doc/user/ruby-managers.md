# Configuring Ruby managers

It's recommended to add TruffleRuby to a Ruby manager for ease of use.

## RVM

RVM has a command for adding precompiled Ruby.

```bash
rvm mount path/to/graalvm/language/ruby -n truffleruby
```

## rbenv

To add TruffleRuby to rbenv a symbolic link has to be added to the `versions` 
directory of rbenv.

```bash
ln -s path/to/graalvm/language/ruby "$RBENV_ROOT/versions/truffleruby"
```

## chruby

To add TruffleRuby to chruby a symbolic link has to be added to the `$HOME/.rubies` 
directory.

```bash
ln -s path/to/graalvm/language/ruby "$HOME/.rubies/truffleruby"
```

## Using TruffleRuby without Ruby manager

If you are using a Ruby manager like `rvm`, `rbnev`, or `chruby` and wish
not to add TruffleRuby to one of them 
make sure that the manager does not set environment variables 
`GEM_HOME`, `GEM_PATH`, and `GEM_ROOT`. 
The variables are picked up by truffleruby (as any other Ruby 
implementation would do) causing truffleruby to pickup the wrong gem-home 
directory instead of its own.

It can be easily fixed by clearing the environment with one of the following 
commands:

```bash
rvm use system
rbenv system
chruby system
```

Otherwise, unset the variables with:

```bash
unset GEM_HOME GEM_PATH GEM_ROOT
```
Next step: [Installing gems](installing-gems.md).

