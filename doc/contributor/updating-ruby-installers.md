# Updating Ruby installers

We support 3 Ruby installers:

* [ruby-install](https://github.com/postmodern/ruby-install), used by `chruby`
* [ruby-build](https://github.com/rbenv/ruby-build), used by `rbenv`
* [rvm](https://github.com/rvm/rvm)

To add a release to Ruby installers, we need to make a Pull Request to each of those.
We assume `$VERSION` is replaced by the release version below (e.g. `1.0.0-rc12`).

In general, you can just follow the PRs of a previous release.
But there are some extra details below to make it easier.

## Git Branches and Naming

For each repository below, I create a branch named `truffleruby-$VERSION`.
This is of course not required, but makes it easier to track.

I then usually look with `git log -p` at the branch of the previous release.
I keep all such branches on my forks, so you can easily look at it as well.

For commit messages, I use `Add TruffleRuby $VERSION`.

## ruby-install

`ruby-install` should not need modifications and just relies on a list of version
maintained in the [ruby-versions](https://github.com/postmodern/ruby-versions) repository.

There is a handy script in the repo that does everything needed:

```bash
$ ./update.sh truffleruby $VERSION
```

Example PR for `1.0.0-rc12`: https://github.com/postmodern/ruby-versions/pull/39

cc `@havenwood` in the PR description.

## ruby-build

There is a script in the repo that does everything needed:

```bash
$ script/update-truffleruby $VERSION
```

Example PR for `1.0.0-rc12`: https://github.com/rbenv/ruby-build/pull/1282

cc `@hsbt` in the PR description.

`rbenv` users which installed `ruby-build` using Homebrew need a new release of
`ruby-build` to try the new TruffleRuby release.

## RVM

There is a script in the repo that does everything needed:

```bash
$ ruby update-truffleruby.rb $VERSION ../ruby-versions/pkg
```

The script must be run after running the `ruby-versions` script above.

Example PR for `1.0.0-rc12`: https://github.com/rvm/rvm/pull/4605

cc `@havenwood` in the PR description.

## Conclusion

This is easy enough but feels a bit redundant and requires manual steps for
creating the PRs.

It would be even nicer if only one PR was needed to `ruby-versions`,
which already has all the relevant data (versions and checksums),
but it does not seem obvious how to use `ruby-versions` in other installers.
See the corresponding issues for [ruby-build](https://github.com/rbenv/ruby-build/issues/1194)
and [RVM](https://github.com/rvm/rvm/issues/4262).
