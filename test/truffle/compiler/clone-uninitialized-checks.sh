#!/usr/bin/env bash

source test/truffle/common.sh.inc

# --always-clone-all covers more than just --core-always-clone, but is much slower (6 min vs 1 min)
jt test fast -- --vm.XX:-UseJVMCICompiler --engine.Compilation=false --core-always-clone --check-clone-uninitialized-correctness
