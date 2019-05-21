Benchmark of a NES emulator written in Ruby, imported from https://github.com/mame/optcarrot.

The code is imported from revision `898783836dfcec04d25a8e7d4b1ce3ce3bbcebdd`.

* bin/, lib/ and tools/ use the MIT license.
  See optcarrot.gemspec
* Lan_Master.nes is in the Public Domain.
  See examples/source.yml

The emulator can be run with the SDL2 GUI with:
```
$ jt build --graal

$ jt ruby bench/optcarrot/bin/optcarrot --sdl2 --audio=none bench/optcarrot/examples/Lan_Master.nes
```
