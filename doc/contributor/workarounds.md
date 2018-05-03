# Patching system

The patching system is loaded in `post-boot.rb` and is able to patch stdlib and
installed gems.  The code implementing the patching itself can be found in
`lib/truffle/truffle/patching.rb`. It can be disabled by passing the
`-Xpatching=false` option.

The patching system works for gems as follows.  When a gem *g* is activated,
there is a directory `g` in `lib/patches`, and the directory is listed in
`lib/truffle/truffle/patching.rb`, the directory is inserted into `$LOAD_PATH`
before the original load-paths of the *g* gem. As a result the patching files
are loaded first before the original files in the gem. The patching file is
responsible for loading the original file (if desirable), which can be done with
`Truffle::Patching.require_original __FILE__`.

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
