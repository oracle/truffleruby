# Updating our version of Ruby

First, check with Chris Seaton for clearance.

From MRI copy and paste over our versions of:

* `lib/mri`
* `test/mri`
* `doc/legal/ruby-bsdl.txt` and `doc/legal/ruby-licence.txt`
* `-h` and `--help` output in `CommandLineParser`

Check for changes that we need to match in some way in other code, or legal
questions.

* Update version information in `RubyLanguage`
* Update `doc/user/compatibility.md`

## Updating bundled gems
The current list of bundled gems their versions are found here:
https://github.com/ruby/ruby/blob/ruby_2_3/gems/bundled_gems

To update a bundled gem, follow these steps:

1. Remove the current gem and gemspec from `lib/gems/2.3.0/gems` and `lib/gems/2.3.0/specifications`
2. Run the gem install command with the desired version. E.g. `./bin/ruby -rbundler-workarounds -S gem install rake -v 10.4.2 --no-doc`
3. Update the project `.gitignore` to allow the newly install gem sources and gemspec
4. If the gem installs any executables like `rake` in `lib/bin`. Add these to the `.gitignore` if not already and verify there is no shebang line and remove executable permissions on the file.