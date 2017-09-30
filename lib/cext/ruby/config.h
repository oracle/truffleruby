// To generate those, go to MRI,
// $ autoconf
// $ CC=clang ./configure
// $ find . -name config.h
#ifdef __APPLE__
#include <truffle/config_darwin.h>
#else
#include <truffle/config_linux.h>
#endif
