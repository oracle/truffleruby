// To generate those, go to MRI,
// $ autoconf
// $ CC=clang ./configure
// $ find . -name config.h
#ifdef __APPLE__
#include <truffleruby/config_darwin.h>
#else
#include <truffleruby/config_linux.h>
#endif
