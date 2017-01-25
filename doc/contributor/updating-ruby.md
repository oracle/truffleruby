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
