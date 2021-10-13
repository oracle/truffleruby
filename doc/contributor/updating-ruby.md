# Updating our version of Ruby

Before you do anything, check with Benoit Daloze for clearance to upgrade.

The workflow below will allow you to see and reapply the modifications that we
have to MRI source code while updating.

You can re-run these instructions at any time to compare against unmodified
MRI files.

## Updating a specific default gem

To update a specific default gem to a newer version than in the MRI release, run:
```
cd ruby
git checkout -b truffleruby-updates-$VERSION vn_n_n
ruby tool/sync_default_gems.rb $GEM

git push -u eregon HEAD
```
to update the default gem in MRI.
Then follow the instructions below to reimport MRI files and to update default gems.

## Setup

Set the environment variable `$VERSION` to the target version:
```
export VERSION=n.n.n
```

Re-install the target MRI version using the commands, to have a clean set of gems:
```
rm -rf ~/.rubies/ruby-$VERSION
ruby-install ruby $VERSION
# OR
rm -rf ~/.rubies/ruby-$VERSION
ruby-build $VERSION ~/.rubies/ruby-$VERSION
ruby-install --no-install-deps -r ~/tmp ruby $VERSION
```

`ruby-build` does not keep the build directory
(required as `RUBY_BUILD_DIR` for `tool/import-mri-files.sh`),
so one needs the extra `ruby-install` command when using `ruby-build`.

## Create reference branches

For both the current version of Ruby you're using, and the new version, create
reference branches that include unmodified MRI sources.

Check out the version of Ruby you want to create the branch for in `../ruby`.

Then create the reference branch in the TruffleRuby repository

```bash
git checkout -b vNN
tool/import-mri-files.sh
git commit -am 'vNN'
```

You can then compare between these two branches and yours. For example to see
what changes you made on top of the old version, what's changed between the
old version and the new version, and so on. Keep them around while you do the
update.

## Update MRI with modifications

In your working branch you can cherry-pick the new reference branch,
and then re-apply old patches using the old reference branch.

```bash
# Commit message: Import files from MRI n.n.n
git cherry-pick vNew
# Commit message: Re-apply changes on top of n.n.n files
git revert vOld
```

You'll usually get some conflicts to work out.

## Comment out `-test-` requires

Run

```bash
git grep -E -- "^\\s*require '-test-/"
git grep -E -- '^\s*require "-test-/'
```

And comment any `require` found in files under `test/mri/tests`
but not for files under `test/mri/tests/cext-ruby`.

## Update config_*.h files

Configuration files must be regenerated from ruby for Linux and macOS
and copied into `lib/cext/include/truffleruby`. In the MRI repository
do the following:

```
ruby-build truffleruby-dev ~/.rubies/truffleruby-dev
chruby truffleruby-dev

graalvm_clang=$(ruby -e 'puts RbConfig::CONFIG["CC"]')

autoconf
CC=$graalvm_clang ./configure
```

The output of configure should report that it has created or updated a
config.h file. For example

```
.ext/include/x86_64-linux/ruby/config.h updated
```

You will need to copy that file to
`lib/cext/include/truffleruby/config_linux.h` or
`lib/cext/include/truffleruby/config_darwin.h`.

After that you should clean your MRI source repository with:

```bash
git clean -Xdf
```

## Update libraries from third-party repos

Look in `../ruby/ext/json/lib/json/version.rb` to see the version of `flori/json` being used,
compare to `lib/json/lib/json/version.rb` and if different then
copy `flori/json`'s `lib` directory into `lib/json`:
```
rm -rf lib/json/lib
cp -R ../../json/lib lib/json
```

## Updating default and bundled gems

You need a clean install (e.g., no extra gems installed) of MRI for this
(see `ruby-install` above).

```
export TRUFFLERUBY=$(pwd)
rm -rf lib/gems/gems
rm -rf lib/gems/specifications

cd ~/.rubies/ruby-$VERSION
cp -R lib/ruby/gems/*.0/gems $TRUFFLERUBY/lib/gems
cp -R lib/ruby/gems/*.0/specifications $TRUFFLERUBY/lib/gems

cd $TRUFFLERUBY
ruby tool/patch-default-gemspecs.rb
```

## Updating exe/ executables

```
rm -rf exe
cp -R ~/.rubies/ruby-$VERSION/bin exe
rm -f exe/ruby
ruby tool/patch_launchers.rb
```

## Make other changes

In a separate commit, update all of these:

* Update `.ruby-version`, `TruffleRuby.LANGUAGE_VERSION`
* Reset `lib/cext/ABI_version.txt` and `lib/cext/ABI_check.txt` to `1` if `RUBY_VERSION` was updated.
* Update `versions.json` (from `cat ../ruby/gems/bundled_gems`, `ls -l lib/gems/specifications/default` and `jt gem --version`)
* Update `TargetRubyVersion` in `.rubocop.yml`
* Copy and paste `-h` and `--help` output to `RubyLauncher`
* Copy and paste the TruffleRuby `--help` output to `doc/user/options.md`
* Update `doc/user/compatibility.md` and `README.md`
* Update `doc/legal/legal.md`
* Update method lists - see `spec/truffle/methods_spec.rb`
* Run `jt test gems default-bundled-gems`
* Grep for the old version with `git grep -F x.y.z`
* If `tool/id.def` or `lib/cext/include/truffleruby/internal/id.h` has changed, `jt build core-symbols` and check for correctness.

For a new major version:
* Update the list of `:next` specs and change the "next version" in `spec/truffleruby.mspec`.
* Update the docs for next version specs in [workflow.md](workflow.md).
* Update the versions in the `ruby/spec on CRuby` job of `.github/workflows/ci.yml`.

## Last step

* Request the new MRI version on Jira, then update `ci.jsonnet` to use the corresponding MRI version for benchmarking.
