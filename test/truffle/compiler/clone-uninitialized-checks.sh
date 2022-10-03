#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt test fast -- --vm.XX:-UseJVMCICompiler --engine.Compilation=false --core-always-clone --check-clone-uninitialized-correctness
