# Installing gems

As mentioned in the 
[`README`](https://github.com/graalvm/truffleruby/tree/truffle-head/README.md) 
TruffleRuby currently cannot run `gem install` out of the box because of incomplete
support for openssl and Nokogiri. However there is a workaround which can be used
to get both `gem install` and `bundler install` working.

> Bundler `1.14.x` is not yet supported by bundler-workarounds, please use 1.13 
> in the meanwhile.

    ruby -r bundler-workarounds -S gem install bundler --version 1.13.7
    ruby -r bundler-workarounds -S bundle install
    ruby -r bundler-workarounds -S bundle update
    
`bundle exec` does not need the `bundler-workarounds` loaded. 
    
    ruby -S bundle exec bin/rails server
