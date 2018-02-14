# Patching system

The patching system is loaded in `post-boot.rb` and is able to patch stdlib and installed gems. 
The code implementing the patching itself can be found in `lib/truffle/truffle/patching.rb`.
It can be disabled by passing the `-Xpatching=false` option.

The patching system works for gems as follows. 
When a gem *g* is activated and there is a directory `g` in `lib/patches`, 
the directory is injected into `LOAD_PATH` before the original load-paths of the *g* gem. 
As a result the patching files are loaded first before the original files in the gem.
The patching file is responsible for loading the original file (if desirable),
which can be easily done with helper method `Truffle::Patching.require_original __FILE__`.

# C file preprocessing

Some C extension of gems need to be patched to make it work with TruffleRuby. 
The patches are defined in `lib/cext/preprocess.rb` 
and applied immediately before compiling the extension's C source files.

# Legacy workarounds 

They should all be eventually removed, no new usages should be added.

## TruffleRuby tool

Is a legacy tool to setup patches to run gem tests. 
It's used only in `jt test ecosystem` mainly for Rails testing.
The tool should be removed and no new tests should use it.

## Constant replacement

`org.truffleruby.parser.ConstantReplacer` replaces constant 
if a partial file path matches and the name of the constant equals.
Usually used to fix Ruby implementation detection.
Should be removed and not used further.    
