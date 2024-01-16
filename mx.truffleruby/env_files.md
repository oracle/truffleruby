Here is how the various env files relate to each other:
* `jvm`
  * `jvm-ce`: + GraalVM Community Compiler
    * `jvm-ce-libgraal`: + libgraal
    * `native`: + librubyvm + `Truffle Macro`
      * `native-host-inlining`: + `TruffleHostInliningPrintExplored`
      * `native-profiling`: + `-H:-DeleteLocalSymbols`
  * `jvm-ee`: + Oracle GraalVM Compiler + `Truffle enterprise` + license + `LLVM Runtime Native Enterprise`
    * `jvm-ee-libgraal`: + libgraal
    * `native-ee`: + librubyvm + `Truffle Macro Enterprise` + Native Image G1
      * `native-ee-host-inlining`: + `TruffleHostInliningPrintExplored`
      * `native-ee-aux`: + `AuxiliaryEngineCache`, - Native Image G1 (currently incompatible)
      * `native-ee-polybench`: + `PolyBench Launcher Enterprise` + `PolyBench Instruments`, only builds `polybench` launcher
  * `jvm-js`: + Graal.js
  * `jvm-py`: + GraalPython
