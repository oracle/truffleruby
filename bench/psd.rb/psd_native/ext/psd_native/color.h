#ifndef PSD_NATIVE_COLOR
#define PSD_NATIVE_COLOR

#define BUILD_PIXEL(r, g, b, a) (((PIXEL) (r) << 24) + ((PIXEL) (g) << 16) + ((PIXEL) (b) << 8) + (PIXEL) (a))
#define CLAMP_PIXEL(p) ((p < 0) ? 0 : ((p > 255) ? 255 : p))
#define R(color) (PIXEL) (((PIXEL) (color) & (PIXEL) 0xff000000) >> 24)
#define G(color) (PIXEL) (((PIXEL) (color) & (PIXEL) 0x00ff0000) >> 16)
#define B(color) (PIXEL) (((PIXEL) (color) & (PIXEL) 0x0000ff00) >> 8)
#define A(color) (PIXEL) ((PIXEL) (color) & (PIXEL) 0x000000ff)
#define OPAQUE(color) ((PIXEL) A(color) == (PIXEL) 0x000000ff)
#define TRANSPARENT(color) ((PIXEL) A(color) == (PIXEL) 0x00000000)
#define PSD_MAX(a, b) (((a) > (b)) ? (a) : (b))
#define PSD_MIN(a, b) (((a) < (b)) ? (a) : (b))

VALUE psd_native_cmyk_to_rgb(VALUE self, VALUE c, VALUE m, VALUE y, VALUE k);
int psd_clamp_int(int n, int low, int high);

#endif