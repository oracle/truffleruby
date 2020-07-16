
#include <sys/types.h>
#include "string.h"

const char ruby_hexdigits[] = "0123456789abcdef0123456789ABCDEF";
#define hexdigit ruby_hexdigits

unsigned long ruby_scan_hex(const char *start, size_t len, size_t *retlen) {
  register const char *s = start;
  register unsigned long retval = 0;
  const char *tmp;
  size_t i = 0;

  for (i = 0; i < len; i++) {
    if (!s[0]) {
      break;
    }
    tmp = strchr(hexdigit, *s);
    if (!tmp) {
      break;
    }
    retval <<= 4;
    retval |= (tmp - hexdigit) & 15;
    s++;
  }
  *retlen = (int)(s - start);    /* less than len */
  return retval;
}

const signed char ruby_digit36_to_number_table[] = {
  /*     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
  /*0*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*1*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*2*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*3*/  0, 1, 2, 3, 4, 5, 6, 7, 8, 9,-1,-1,-1,-1,-1,-1,
  /*4*/ -1,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,
  /*5*/ 25,26,27,28,29,30,31,32,33,34,35,-1,-1,-1,-1,-1,
  /*6*/ -1,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,
  /*7*/ 25,26,27,28,29,30,31,32,33,34,35,-1,-1,-1,-1,-1,
  /*8*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*9*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*a*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*b*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*c*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*d*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*e*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
  /*f*/ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
};

unsigned long ruby_scan_digits(const char *str, ssize_t len, int base, size_t *retlen, int *overflow) {
  const char *start = str;
  unsigned long ret = 0, x;
  unsigned long mul_overflow = (~(unsigned long)0) / base;

  *overflow = 0;

  if (!len) {
    *retlen = 0;
    return 0;
  }

  do {
    int d = ruby_digit36_to_number_table[(unsigned char)*str++];
    if (d == -1 || base <= d) {
      --str;
      break;
    }
    if (mul_overflow < ret) {
      *overflow = 1;
    }
    ret *= base;
    x = ret;
    ret += d;
    if (ret < x) {
      *overflow = 1;
    }
  } while (len < 0 || --len);
  *retlen = str - start;
  return ret;
}

unsigned long ruby_scan_oct(const char *start, size_t len, size_t *retlen) {
  register const char *s = start;
  register unsigned long retval = 0;
  size_t i;

  for (i = 0; i < len; i++) {
    if ((s[0] < '0') || ('7' < s[0])) {
      break;
    }
    retval <<= 3;
    retval |= *s++ - '0';
  }
  *retlen = (int)(s - start); /* less than len */
  return retval;
}
