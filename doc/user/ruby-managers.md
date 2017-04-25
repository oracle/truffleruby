# Configuring Ruby managers

It's recommended to add TruffleRuby to a Ruby manager for ease of use.

## rbenv

To add TruffleRuby to `rbenv` a symbolic link has to be added to the `versions` 
directory of rbenv:

```bash
ln -s path/to/graalvm/language/ruby "$RBENV_ROOT/versions/truffleruby"
```

## chruby

To add TruffleRuby to `chruby` a symbolic link has to be added to the `$HOME/.rubies` 
directory:

```bash
ln -s path/to/graalvm/language/ruby "$HOME/.rubies/truffleruby"
```

## RVM

RVM has a command for adding a precompiled Ruby to the list of available rubies. RVM has to temporarily be patched, 
due to unsupported openssl.

```bash
cd path/to/rvm

echo "diff --git a/scripts/mount b/scripts/mount
index 031a23c..bf1dcb0 100755
--- a/scripts/mount
+++ b/scripts/mount
@@ -182,6 +182,7 @@ __rvm_osx_ssl_certs_ensure_for_ruby_except_jruby()
 {
   case \"\$1\" in
     (jruby*) true ;;
+    (*truffleruby*) true ;;
     (*)      __rvm_osx_ssl_certs_ensure_for_ruby \"\$2\" ;;
   esac
 }" | patch -p1
```

Then `rvm mount` can be used.

```bash
rvm mount path/to/graalvm/language/ruby -n truffleruby
```

The name passed with `-n` option has to contain `truffleruby`.

## Using TruffleRuby without a Ruby manager

If you are using a Ruby manager like `rvm`, `rbenv`, or `chruby` and wish
not to add TruffleRuby to one of them 
make sure that the manager does not set environment variables 
`GEM_HOME`, `GEM_PATH`, and `GEM_ROOT`. 
The variables are picked up by truffleruby (as any other Ruby 
implementation would do) causing truffleruby to pickup the wrong gem-home 
directory instead of its own.

It can be easily fixed by clearing the environment with one of the following 
commands:

```bash
rvm use system
rbenv system
chruby system
```

Otherwise, unset the variables with:

```bash
unset GEM_HOME GEM_PATH GEM_ROOT
```
Next step: [Installing gems](installing-gems.md).

