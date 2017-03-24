# Temporary patching system

-   The patching system is loaded in `post-boot.rb` only when rubygems are enabled.
-   The code handling the patching itself can be found in `lib/truffle/truffle/patching.rb`.
-   There is a `Hash` which declares, when files in this directory should be required in relation to they originals,
    at the begging of the `patching.rb` file. 
-   Whenever a file is required e.g. `bundler/cli/exec` and there is a file under same path in this `patches` directory, 
    the file in the `patches` directory is required before, after or instead the original 
 
