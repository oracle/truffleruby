# Installing gems

As mentioned in the [README](../../README.md) 
TruffleRuby currently cannot run `gem install` out of the box because of incomplete
support for openssl and Nokogiri. However there is a workaround which can be used
to get both `gem install` and `bundler install` working.

Ensure you have ruby managers configured properly, see 
[Ensuring Ruby managers are configured correctly](ruby-managers.md).

Now install Bundler:

> **Note:** Bundler `1.14.x` is not yet supported, 
> please use 1.13 in the meanwhile.

    gem install bundler -v 1.13.7

You can run bundle `install` and `update`:

    bundle install
    bundle update
    
and `bundle exec`: 
    
    bundle exec bin/rails server

Next step: [Playing Optcarrot](optcarrot.md),
[Compatibility with Rails](README.md#do-you-run-rails)

