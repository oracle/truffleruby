# Running Optcarrot

## Running the Demo

Start by [installing GraalVM](installing-graalvm.md).

Then add GraalVM/bin in PATH (or use a Ruby manager):

```bash
export PATH="/path/to/graalvm/bin:$PATH"
```

You also need to install SDL2:
* `brew install sdl2` on macOS
* `sudo yum install SDL2-devel` for RedHat-based Linux
* `sudo apt-get install libsdl2-dev` for Debian-based Linux

Then clone the optcarrot repository:

```bash
git clone https://github.com/eregon/optcarrot.git
cd optcarrot
```

Then you can play the Lan Master game.

On Linux:

```bash
ruby --jvm bin/optcarrot --print-fps --sdl2 --audio=none examples/Lan_Master.nes
```

On macOS, you need an extra flag for the GUI to appear: `--vm.XstartOnFirstThread`

```bash
ruby --jvm --vm.XstartOnFirstThread bin/optcarrot --print-fps --sdl2 --audio=none examples/Lan_Master.nes
```

(`--audio=none` is used since the audio it not nice at non-60FPS speeds)

To play, use keys `1`...`7` (not on numpad) to scale the screen,
`X` for OK/turn right, `S` for turn left, arrows to move around, `Q` to quit.

More information can be found in this [blog post](https://eregon.me/blog/2016/11/28/optcarrot.html).

[Here](https://youtu.be/mRKjWrNJ8DI?t=180) is a recording of a talk running the demo on TruffleRuby and MRI.

### Running on Other Ruby implementations

You can also run it on MRI for comparison.
You will need to install the FFI gem with:
```bash
gem install --user ffi
ruby bin/optcarrot --print-fps --sdl2 --audio=none examples/Lan_Master.nes
```

You can also run it on JRuby if desired:
```bash
jruby bin/optcarrot --print-fps --sdl2 --audio=none examples/Lan_Master.nes
```

## Running as a Benchmark from the TruffleRuby Repository

If you have a local checkout of TruffleRuby, you can also use the version of OptCarrot under `bench/optcarrot`.
See the [Benchmarking](../contributor/benchmarking.md#optcarrot) documentation for details.
