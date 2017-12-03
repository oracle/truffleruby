# Temporary patching system

- The patching system is loaded in `post-boot.rb` and applies for stdlib and gems.
- The code handling the patching itself can be found in `lib/truffle/truffle/patching.rb`.
- When a gem *abc* is activated and there is a directory *abc* in `lib/patches`, the directory is injected
  into `LOAD_PATH` before the original load-paths of the gem *abc*. As a result the patching files are loaded
  first before the originals and the patching file is responsible for loading the original file. The patching file
  can load the original file when needed or not at all with the helper `Truffle::Patching.require_original __FILE__`.
