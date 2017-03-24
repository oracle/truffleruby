# Installing gems

As mentioned in the [README](../../README.md) TruffleRuby does not yet
support openssl and C extensions, therefore we apply a few patches internally to 
make `rubygems` and `bundler` work out of the box. Gems with C extensions will
install but nothing will be compiled. If the gem does not contain a pure 
Ruby implementation of the C extension the gem will not function properly (
e.g. `nokogiri`, Active Record drivers, etc).

The patches require `wget` and `curl` to be installed, The patches will be eventually removed.

Examples:

Use a Ruby manager to switch to TruffleRuby e.g. `rbenv shell truffleruby`, 
see [Configuring Ruby managers](ruby-managers.md).

**Note:** Only bundler `1.14.x` is supported.

    gem install bundler --version 1.14.6

`install`, `update`, and `exec` work as expected.

    bundle install
    bundle update
    bundle exec an_application_executable.rb

Next step: [Playing Optcarrot](optcarrot.md)
