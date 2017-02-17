# Ensuring Ruby managers are configured correctly

If you are using a Ruby manager like `rvm`, `rbnev`, or `chruby` make 
sure that the manager does not set environment variables `GEM_HOME`, `GEM_PATH`,
and `GEM_ROOT`. The variables are picked up by truffleruby (as any other Ruby 
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

It will be easier when TruffleRuby can be added to ruby managers, we are working 
on it.  

Next step: [Installing gems](installing-gems.md).

