# Updating our version of Ruby

First, check with Chris Seaton for clearance.

From MRI copy and paste over our versions of:

* `lib` to `lib/mri`
* `ext/{bigdecimal,psych,pty}/lib` to `lib/mri`
* `ext/openssl/*.{c,h}` to `truffleruby/src/main/c/openssl`
* `ext/openssl/lib` to `lib/mri`
* `test/mri`
* `doc/legal/ruby-bsdl.txt` and `doc/legal/ruby-licence.txt`

The script `tool/update-mri.sh` will do the above for you, assuming you have the
version of MRI you want checked out in `../ruby`. You should be able to commit
changes from this script without modification. If you can't, you need to update
the script or these instructions.

Look at `mkmf.rb` and `truffleruby/src/main/c/openssl` to restore the
modifications we have made there, and in general check for changes that we need
to match in some way in other code, or legal questions.

Then copy and paste:

* `-h` and `--help` output in `CommandLineParser`
* `lib/json` using the version of `flori/json` specified but not totally included in `ext/json`

Then:

* Update version information in `RubyLanguage`
* Update `doc/user/compatibility.md`
* Update `doc/user/legal.md`

Check again with everyone for clearance.

## Updating bundled gems

The current list of bundled gems their versions are found here:
https://github.com/ruby/ruby/blob/ruby_2_3/gems/bundled_gems

To update a bundled gem, follow these steps:

1. Remove the current gem and gemspec from `lib/gems/2.3.0/gems` and `lib/gems/2.3.0/specifications`
2. Run the gem install command with the desired version. E.g. `gem install rake -v 10.4.2 --no-doc`
3. Update the project `.gitignore` to allow the newly install gem sources and gemspec
4. If the gem installs any executables like `rake` in `lib/bin`. Add these to the `.gitignore` if not already and verify there is no shebang line and remove executable permissions on the file.
