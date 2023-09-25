Here is how the various env files relate to each other:
* `jvm`
  * `jvm-ce`: + GraalVM Community Compiler
    * `jvm-ce-ntl`: + native toolchain launchers
      * `jvm-ce-libgraal`: + libgraal
      * `native`: + librubyvm + `Truffle Macro`
        * `native-host-inlining`: + `TruffleHostInliningPrintExplored`, - native toolchain launchers
        * `native-profiling`: + `-H:-DeleteLocalSymbols`
  * `jvm-ee`: + Oracle GraalVM Compiler + `Truffle enterprise` + license + `LLVM Runtime Native Enterprise`
    * `jvm-ee-ntl`: + native toolchain launchers
      * `jvm-ee-libgraal`: + libgraal
      * `native-ee`: + librubyvm + `Truffle Macro Enterprise` + Native Image G1
        * `native-ee-host-inlining`: + `TruffleHostInliningPrintExplored`, - native toolchain launchers
        * `native-ee-aux`: + `AuxiliaryEngineCache`, - Native Image G1 (currently incompatible)
  * `jvm-js`: + Graal.js
  * `jvm-py`: + GraalPython
