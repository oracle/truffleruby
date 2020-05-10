#include <truffleruby-impl.h>

double ruby_strtod(const char *nptr, char **endptr) {
  #undef strtod
  return strtod(nptr, endptr);
  #define strtod(s,e) ruby_strtod((s),(e))    
}
