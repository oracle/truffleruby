# Updating our version of Ruby

First, check with Chris Seaton for clearance.

From MRI copy and paste over our versions of:

* `lib` to `lib/mri`
* `ext/{bigdecimal,psych,pty}/lib` to `lib/mri`
* `ext/openssl/*.{c,h}` to `src/main/c/openssl`
* `ext/openssl/lib` to `lib/mri`
* `test/mri`
* `ext/-test-` to `test/mri`
* `doc/legal/ruby-bsdl.txt` and `doc/legal/ruby-licence.txt`
* Move `test/mri/-ext-` to `test/mri/cext/ruby`, and `test/mri/-test-` to `test/mri/cext/c`
* Change instances of `-test-` in the MRI cext tests to `c`

The script `tool/update-mri.sh` will do the above for you, assuming you have the
version of MRI you want checked out in `../ruby`. You should be able to commit
changes from this script without modification. If you can't, you need to update
the script or these instructions.

Look at `mkmf.rb` and `src/main/c/openssl` to restore the
modifications we have made there, and in general check for changes that we need
to match in some way in other code, or legal questions.

Then copy and paste:

* `-h` and `--help` output in `CommandLineParser`
* `lib/json` using the version of `flori/json` specified but not totally included in `ext/json`

Then:

* Update version information in `Launcher`
* Update `doc/user/compatibility.md`
* Update `doc/user/legal.md`

Check again with everyone for clearance.

## Updating bundled gems

The current list of bundled gems their versions are found here:
https://github.com/ruby/ruby/blob/ruby_2_3/gems/bundled_gems

To update a bundled gem, follow these steps:

1.  Remove the current gem and gemspec from `lib/gems/2.3.0/gems` and `lib/gems/2.3.0/specifications`
2.  Run the gem install command with the desired version. E.g. `gem install rake -v 10.4.2 --no-doc`
3.  Update the project `.gitignore` to allow the newly install gem sources and gemspec
4.  If the gem installs any executables like `rake` in `bin`. Add these to the `.gitignore` using `!bin/rake` if not already and ensure that the shebang has a format as follows:
    
    ```bash
    #!/usr/bin/env bash
    exec "$(dirname $0)/truffleruby" "$(dirname $0)/the-executable" "$@" # ignored by Ruby interpreter
    #!ruby
    # ^ marks start of Ruby interpretation

    # ... the content of the executable
    ```
    
    See [Launchers doc](launchers.md) 

## Updating C headers

Use the following steps to update C headers to another MRI version. The example commands assume truffleruby project is the current directory and there is a ruby directory as a sibling.

1. Create a patch of all header changes made to original headers. For example, diff changes following the commit message "Copy MRI x.x.x changes".

`git diff <commit hash of original headers changes> HEAD > headers.patch -- lib/cext`

2. Removing existing MRI headers while preserving a few added headers.

`rm -rf lib/cext/ruby.h lib/cext/ruby`
`git checkout lib/cext/ruby/config.h`

2. Copy the updated MRI headers over, review changes, and then commit them.

`cp -r ../ruby/include/. lib/cext/`

`cp -r ../ruby/ccan/. lib/cext/ccan`

3. Apply the patch from step one and resolve any conflicts.

`git apply -3 headers.patch`