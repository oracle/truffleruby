# Installing gems

As mentioned in the [README](../../README.md) TruffleRuby does not yet
support openssl and C extensions therefore we apply few patched internally to 
make `rubygems` and `bundler` work out of the box. Gems with C extensions will
install but nothing will be compiled. If the gem does not contain a pure 
Ruby implementation of the C extension the gem will not function properly.
E.g. `nokogiri`, Active Record drivers, etc.

The patches will be eventually removed.

Ensure you have ruby managers configured properly, see 
[Ensuring Ruby managers are configured correctly](ruby-managers.md).

Examples:

**Note:** Bundler `1.14.x` is not yet supported, please use 1.13 in the meanwhile.

    gem install bundler --version 1.13.7

`install`, `update`, and `exec` work as expected.

    bundle install
    bundle update
    bundle exec an_application_executable.rb

Next step: [Playing Optcarrot](optcarrot.md)
