Here is how the various env files relate to each other:
* `jvm`
  * `jvm-ce`: + GraalVM Community Compiler
    * `jvm-ce-ntl`: + native toolchain launchers + `truffleruby-polyglot-get`
      * `jvm-ce-libgraal`: + libgraal
      * `native`: + librubyvm + `Truffle Macro`
        * `native-host-inlining`: + `TruffleHostInliningPrintExplored`, - native toolchain launchers
  * `jvm-ee`: + Oracle GraalVM Compiler + `Truffle enterprise`
    * `jvm-ee-ntl`: + native toolchain launchers + `truffleruby-polyglot-get`
      * `jvm-ee-libgraal`: + libgraal
      * `native-ee`: + librubyvm + `Truffle Macro Enterprise` + Native Image G1
        * `native-ee-aux`: + `AuxiliaryEngineCache`, - Native Image G1 (currently incompatible)
  * `jvm-gu`: + Graal Updater
  * `jvm-js`: + Graal.js
  * `jvm-py`: + GraalPython
