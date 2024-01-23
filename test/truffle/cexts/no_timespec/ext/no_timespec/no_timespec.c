// Include time.h before ruby.h, this has the effect together with --std=c99 to not define struct timespec
#include <time.h>
#include <ruby.h>

void Init_no_timespec() {
  // This would fail but what we want to test is that including ruby.h works in this situation
  // struct timespec t;
  printf("Hello!\n");
}
