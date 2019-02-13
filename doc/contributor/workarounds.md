# Ruby files patching system

When `require 'some/patch'` (has to be a relative path) is called 
and an original file exists on `$LOAD_PATH`
and there is a patch file `some/patch.rb` in a special directory `lib/patches` 
then the TruffleRuby will first evaluate (not require) the file 
`lib/patches/some/patch.rb` instead. 

To require the original file call `require 'some/patch'` in the 
`lib/patches/some/patch.rb` file. It will require the original ruby 
file found on `$LOAD_PATH` ( stdlib file or a gem file).

When requiring the original file is not desired just omit the 
`require 'some/patch'` in the patch file. The patch file will be evaluated
only once.  

The evaluated patch files are not visible in `$LOAD_PATH` nor `$LOADED_FEATURES`.

Patching can be disabled by passing the `--patching=false` option. 
`--log.level=CONFIG` can be used to see paths of loaded patch files.

# C file preprocessing

Some C extension of gems need to be patched to make them work with TruffleRuby.
The patches are defined in `lib/cext/preprocess.rb`  and applied immediately on
the C source before passing it to the compiler.

# Legacy workarounds 

They should all be eventually removed and no new usages should be added.

## TruffleRuby tool

Is a legacy tool to setup patches to run gem tests.  It's used only in `jt test
ecosystem` mainly for Rails testing. The tool should be removed and no new tests
should use it.
