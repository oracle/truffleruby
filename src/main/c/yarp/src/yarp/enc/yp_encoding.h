#ifndef YARP_ENCODING_H
#define YARP_ENCODING_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#define YP_ENCODING_ALPHABETIC_BIT 0b001
#define YP_ENCODING_ALPHANUMERIC_BIT 0b010
#define YP_ENCODING_UPPERCASE_BIT 0b100

/******************************************************************************/
/* ASCII                                                                      */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_ascii_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_ascii_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_ascii_isupper_char(const char *c);

/******************************************************************************/
/* Big5                                                                       */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_big5_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_big5_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_big5_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-1                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_1_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_1_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_1_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-2                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_2_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_2_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_2_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-3                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_3_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_3_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_3_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-4                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_4_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_4_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_4_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-5                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_5_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_5_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_5_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-6                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_6_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_6_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_6_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-7                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_7_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_7_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_7_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-8                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_8_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_8_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_8_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-9                                                                 */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_9_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_9_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_9_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-10                                                                */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_10_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_10_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_10_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-11                                                                */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_11_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_11_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_11_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-13                                                                */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_13_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_13_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_13_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-14                                                                */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_14_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_14_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_14_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-15                                                                */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_15_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_15_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_15_isupper_char(const char *c);

/******************************************************************************/
/* ISO-8859-16                                                                */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_16_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_iso_8859_16_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_iso_8859_16_isupper_char(const char *c);

/******************************************************************************/
/* UTF-8                                                                      */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_utf_8_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_utf_8_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_utf_8_isupper_char(const char *c);

/******************************************************************************/
/* Windows-1251                                                               */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_windows_1251_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_windows_1251_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_windows_1251_isupper_char(const char *c);


/******************************************************************************/
/* Windows-1252                                                               */
/******************************************************************************/

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_windows_1252_alpha_char(const char *c);

__attribute__((__visibility__("default"))) extern size_t
yp_encoding_windows_1252_alnum_char(const char *c);

__attribute__((__visibility__("default"))) extern bool
yp_encoding_windows_1252_isupper_char(const char *c);


#endif
