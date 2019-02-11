# Context Pre-Initialization

TruffleRuby supports context pre-initialization with SubstrateVM.

Context pre-initialization is enabled in Truffle by setting a Java property:

```
-Dpolyglot.engine.PreinitializeContexts=ruby
```

This is enabled by default in `mx.truffleruby/native-image.properties`.

Context pre-initialization does the full initialization during image build time;
it creates a RubyContext (with `createContext()`) and calls
`initializeContext()`.

During pre-initialization at image build-time, some special considerations need
to be taken in account. Currently, native calls (via NFI) are not supported
during pre-initialization. Places which need special treatment can be found
by looking at the usages of `RubyContext#isPreInitializing()`.

At runtime, quite a few things need to be changed to reflect the runtime
environment instead of the image build-time environment.
This process is called *patching* and starts in `RubyContext#patchContext()`.
For patches in the Ruby core, look for `Truffle::Boot.preinitializing?`,
`Truffle::Boot.delay` and `Truffle::Boot.redo`.

In some cases, the pre-initialized context cannot be reused due to incompatible
options. See `RubyContext#compatibleOptions()`.

## Debugging

`--log.level=FINE` outputs useful information regarding pre-initialization, notably
whether the pre-initialized context is reused.

It is possible to run the pre-initialization process on the JVM to help
debugging more quickly. However, this does not fully emulate a pre-initialized
image, as for instance the same process is used for pre-initialization and
runtime execution.

Debugging on the JVM can be achieved with:

```
bin/truffleruby -J-Dpolyglot.engine.PreinitializeContexts=ruby --log.level=FINE -e 'p :hi'
```
