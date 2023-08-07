Here is how the various env files relate to each other:
* `jvm`
  * `jvm-ce`: + GraalVM Community Compiler
    * `jvm-ce-ntl`: + native toolchain launchers
      * `jvm-ce-libgraal`: + libgraal
      * `native`: + librubyvm + `Truffle Macro`
        * `native-host-inlining`: + `TruffleHostInliningPrintExplored`, - native toolchain launchers
  * `jvm-ee`: + Oracle GraalVM Compiler + `Truffle enterprise`
    * `jvm-ee-ntl`: + native toolchain launchers
      * `jvm-ee-libgraal`: + libgraal
      * `native-ee`: + librubyvm + `Truffle Macro Enterprise`
        * `native-ee-g1`: + Native Image G1
  * `jvm-gu`: + Graal Updater
  * `jvm-js`: + Graal.js
  * `jvm-py`: + GraalPython
