# Updating Ruby installers

We support 3 Ruby installers:

* [ruby-install](https://github.com/postmodern/ruby-install), used by `chruby`
* [ruby-build](https://github.com/rbenv/ruby-build), used by `rbenv`
* [rvm](https://github.com/rvm/rvm)

To add a release to Ruby installers, we need to update each of those.
We assume `$VERSION` is replaced by the release version below (e.g. `1.0.0-rc12`).

In general, you can just follow the PRs of a previous release.
But there are some extra details below to make it easier.

Benoit has push rights to all 3 repositories, so that can speed up the process by not needing PRs.

## Git Branches and Naming

For each repository below, I create a branch named `truffleruby-$VERSION`.
This is of course not required, but makes it easier to track.
For commit messages, I use `Add TruffleRuby $VERSION`.

## ruby-install

`ruby-install` should not need modifications and just relies on a list of version
maintained in the [ruby-versions](https://github.com/postmodern/ruby-versions) repository.

There is a handy script in the repo that does everything needed:

```bash
./update.sh truffleruby $VERSION
./update.sh truffleruby-graalvm $VERSION
```

## ruby-build

There is a script in the repo that does everything needed:

```bash
script/update-truffleruby $VERSION ../ruby-versions/pkg
script/update-truffleruby-graalvm $VERSION ../ruby-versions/pkg
```

`rbenv` users which installed `ruby-build` using Homebrew need a new release of
`ruby-build` to try the new TruffleRuby release.

### Create a Release

Once pushed/merged to the default branch, it's a good idea to create a `ruby-build` release,
so `ruby-build` Homebrew users can get the new TruffleRuby release too:

```bash
script/release
```

See [this comment](https://github.com/rbenv/ruby-build/pull/1318#issuecomment-548399571) for details.

## RVM

There is a script in the repo that does everything needed:

```bash
ruby update-truffleruby.rb $VERSION ../ruby-versions/pkg
```

The script must be run after running the `ruby-versions` script above.

cc `@havenwood, @pkuczynski` in the PR description.

## ruby/setup-ruby

Trigger the builds on [ruby/ruby-builder](https://github.com/ruby/ruby-builder) and once done,
add the version in [ruby-builder-versions.js](https://github.com/ruby/setup-ruby/blob/master/ruby-builder-versions.js),
update the [README](https://github.com/ruby/setup-ruby/blob/master/README.md#supported-versions),
and make a release.

## flavorjones/docker-truffleruby

Make a PR to [flavorjones/docker-truffleruby](https://github.com/flavorjones/docker-truffleruby).
See [CONTRIBUTING.md](https://github.com/flavorjones/docker-truffleruby/blob/master/CONTRIBUTING.md)
for how to update to the latest release.

## Conclusion

This is easy enough but feels a bit redundant and requires manual steps for
creating the PRs.

It would be even nicer if only one PR was needed to `ruby-versions`,
which already has all the relevant data (versions and checksums),
but it does not seem obvious how to use `ruby-versions` in other installers.
See the corresponding issues for [ruby-build](https://github.com/rbenv/ruby-build/issues/1194)
and [RVM](https://github.com/rvm/rvm/issues/4262).
