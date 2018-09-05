# Updating Ruby installers

We support 3 Ruby installers:

* [ruby-install](https://github.com/postmodern/ruby-install), used by `chruby`
* [ruby-build](https://github.com/rbenv/ruby-build), used by `rbenv`
* [rvm](https://github.com/rvm/rvm)

To add a release to Ruby installers, we need to make a Pull Request to each of those.
We assume `$VERSION` is replaced by the release version below (e.g. `1.0.0-rc6`).

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

There is a handy script in the repo that does most of the job:

```
./update.sh truffleruby $VERSION
```

You also need to change `truffleruby/stable.txt` to list only the latest release.

Example PR for `1.0.0-rc6`: https://github.com/postmodern/ruby-versions/pull/32

cc `@havenwood` in the PR description.

## ruby-build

First, copy the definition of the last release:

```
cd share/ruby-build
cp truffleruby-1.0.0-rc6 truffleruby-$VERSION
```

Then, replace the version number in `share/ruby-build/truffleruby-$VERSION`,
as well as the SHA-256 checksums (copy-paste the checksums from ruby-versions).

Example PR for `1.0.0-rc6`: https://github.com/rbenv/ruby-build/pull/1230

cc `@hsbt` in the PR description.

`rbenv` users which installed `ruby-build` using Homebrew need a new release of
`ruby-build` to try the new TruffleRuby release.

## RVM

RVM needs modifications to 5 files, which are also documented
[upstream](https://github.com/rvm/rvm/blob/master/CONTRIBUTING.md#adding-support-for-new-version-of-ruby).

* `config/db`
* `config/known`
* `config/known_strings`
* `config/md5`
* `config/sha512`

The simplest is to follow a PR for a previous release, and to copy-paste the
checksums from ruby-versions.

Example PR for `1.0.0-rc6`: https://github.com/rvm/rvm/pull/4452

You should also update the CHANGELOG, which can only be done after creating the
PR as the CHANGELOG points to PR numbers and URLs.

cc `@havenwood @mpapis` in the PR description.

## Conclusion

This is easy enough but still feels quite redundant and error-prone.

In the future, we should probably create some scripts to automate most of this.
For instance, it seems easy to create the `ruby-build` definition from a (e.g., ERB)
template and the (local) `ruby-versions` data, which could also be contributed upstream.
`./update.sh` in `ruby-versions` could probably update `truffleruby/stable.txt`
automatically. For RVM, the modifications are probably simple enough to write a
Ruby script with some regexps to modify the right files.

Further on, it would be even nicer if only one PR was needed to `ruby-versions`,
which already has all the relevant data (versions and checksums),
but it does not seem obvious how to use `ruby-versions` in other installers.
See the corresponding issues for [ruby-build](https://github.com/rbenv/ruby-build/issues/1194)
and [RVM](https://github.com/rvm/rvm/issues/4262).
