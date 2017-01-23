# Installing gems

As mentioned in the 
[`README`](https://github.com/graalvm/truffleruby/tree/truffle-head/README.md) 
TruffleRuby currently cannot run `gem install` out of the box because of incomplete
support for openssl and Nokogiri. However there is a workaround which can be used
to get both `gem install` and `bundler install` working.
 
    ruby -r bundler-workarounds.rb -S gem install
    ruby -r bundler-workarounds.rb -S bundle install
    ruby -r bundler-workarounds.rb -S bundle update
    
`bundle exec` does not need the `bundler-workarounds.rb` loaded. 
    
    ruby -S bundle exec bin/rails server
