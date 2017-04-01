# Temporary patching system

-   The patching system is loaded in `post-boot.rb` only when rubygems are enabled.
-   The code handling the patching itself can be found in `lib/truffle/truffle/patching.rb`.
-   When a gem *abc* is activated and there is a directory *abc* in `lib/patches`, the directory is injected
    into `LOAD_PATH` before original load-paths of the gem *abc*. As a result the patching files are loaded 
    first before the originals and the file is responsible for loading the original file. The patching file
    can load original when needed or not at all with helper `Truffle::Patching.require_original __FILE__`. 
