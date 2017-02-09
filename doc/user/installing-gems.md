# Installing gems

As mentioned in the 
[`README`](https://github.com/graalvm/truffleruby/tree/truffle-head/README.md) 
TruffleRuby currently cannot run `gem install` out of the box because of incomplete
support for openssl and Nokogiri. However there is a workaround which can be used
to get both `gem install` and `bundler install` working.

First, make sure `GEM_HOME`, `GEM_PATH` and `GEM_ROOT` are not set
as they would interfere with RubyGems/Bundler.
You can do this by switching to the system Ruby
with your Ruby version switcher if you have one:
```bash
rvm use system
rbenv system
chruby system
```
Otherwise, unset the variables with:
```bash
unset GEM_HOME GEM_PATH GEM_ROOT
```

Now install Bundler:

> Bundler `1.14.x` is not yet supported by bundler-workarounds, please use 1.13 
> in the meanwhile.

    ruby -rbundler-workarounds -S gem install bundler -v 1.13.7

You can run bundle `install` and `update` like this:

    ruby -rbundler-workarounds -S bundle install
    ruby -rbundler-workarounds -S bundle update
    
`bundle exec` does not need the `bundler-workarounds` loaded. 
    
    ruby -S bundle exec bin/rails server
