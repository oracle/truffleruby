# Updating our version of Ruby

Before you do anything, check with Chris Seaton for clearance to upgrade.

The workflow below will allow you to see and reapply the modifications that we
have to MRI source code while updating.

You can re-run these instructions at any time to compare against unmodified
MRI files.

## Create reference branches

For both the current version of Ruby you're using, and the new version, create
reference branches that include unmodified MRI sources.

Check out the version of Ruby you want to create the branch for in `../ruby`.

Then create the reference branch in the TruffleRuby repository

```bash
$ git checkout -b vNN
$ tool/import-mri-files.sh
$ git commit -am 'vNN'
```

You can then compare between these two branches and yours. For example to see
what changes you made on top of the old version, what's changed between the
old version and the new version, and so on. Keep them around while you do the
update.

## Update MRI with modifications

In your working branch you can import MRI files again, and you can re-apply
old patches using the old reference branch.

```bash
$ tool/import-mri-files.sh
$ git diff vNN master | git apply -3
```

You'll usually get some conflicts to work out.

## Make other changes

* Update `versions.json` and `.ruby-version`
* Copy and paste `-h` and `--help` output to `RubyLauncher`
* Copy and paste the TruffleRuby `--help` output to `doc/user/options.md`
* Update `doc/user/compatibility.md`
* Update `doc/legal/legal.md`
* Update `doc/contributor/stdlib.md`
* Update method lists - see `spec/truffle/methods_spec.rb`
* Update `ci.jsonnet` to use the corresponding MRI version for benchmarking

## Update libraries from third-party repos

Look in `../ruby/ext/json` to see the version of `flori/json` being used, and
then copy the original source of `flori/json` into `lib/json`.

## Updating .gemspec of default gems

Default gems are imported from MRI files, except the .gemspec files in
`lib/gems/specifications/default`.
To update those, copy the files over from an installed MRI.
```
rm -rf lib/gems/specifications/default
cp -r ~/.rubies/ruby-n.n.n/lib/ruby/gems/n.n.n/specifications/default lib/gems/specifications
```

## Updating bundled gems

To update a bundled gem, follow these steps:

* Remove the current gem and gemspec from `lib/gems/gems` and
  `lib/gems/specifications`
* Run the gem install command with the desired version
  `gem install rake -v 10.4.2 --no-doc`
* Update the project `.gitignore` to allow the newly install gem sources
  and gemspec
* Copy from the build directory `lib` to the source `lib`
* If the gem installs any executables like `rake` in `bin` ensure that the
  shebang has a format as follows:

```bash
#!/usr/bin/env bash
# ignored by Ruby interpreter

# get the absolute path of the executable and resolve symlinks
SELF_PATH=$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")
while [ -h "$SELF_PATH" ]; do
  # 1) cd to directory of the symlink
  # 2) cd to the directory of where the symlink points
  # 3) get the pwd
  # 4) append the basename
  DIR=$(dirname "$SELF_PATH")
  SYM=$(readlink "$SELF_PATH")
  SELF_PATH=$(cd "$DIR" && cd "$(dirname "$SYM")" && pwd)/$(basename "$SYM")
done
exec "$(dirname $SELF_PATH)/ruby" "$SELF_PATH" "$@"

#!ruby
# ^ marks start of Ruby interpretation

# ... the content of the executable
```
