These benchmarks are originally from
[Bench 9000](https://github.com/jruby/bench9000).

We are using PSD.rb at revision `e14d652ddc705e865d8b2b897d618b25d78bcc7c`.
We use this old revision because we know it has interesting patterns of
meta-programming that we are interested in benchmarking. Some of these
patterns were replaced in later versions.

See also the ChunkyPNG benchmarks - we use the same version of ChunkyPNG as
in those.

We are using PSDNative at revision `bbea04db2f4f483bde73b6793e68eff73f3b9c3f`,
with this patch, which worked around limitations in the original TruffleC
(you could also reasonably call them bugs in PSDNative).

```
diff --git a/ext/psd_native/mask.c b/ext/psd_native/mask.c
index b3ed11f..9d4a926 100644
--- a/ext/psd_native/mask.c
+++ b/ext/psd_native/mask.c
@@ -38,7 +38,7 @@ VALUE psd_native_mask_apply_bang(VALUE self) {
       if (doc_x < 0 || doc_x >= doc_width || doc_y < 0 || doc_y > doc_height) {
         alpha = 0;
       } else {
-        alpha = mask_data[i];
+        alpha = FIX2INT(mask_data[i]);
       }
 
       color = (color & 0xffffff00) | (A(color) * alpha / 255);
diff --git a/ext/psd_native/image_mode_cmyk.c b/ext/psd_native/image_mode_cmyk.c
index b2b8bb6..eb1a8e9 100644
--- a/ext/psd_native/image_mode_cmyk.c
+++ b/ext/psd_native/image_mode_cmyk.c
@@ -17,7 +17,9 @@ VALUE psd_native_combine_cmyk_channel(VALUE self) {
   uint32_t a = 255;
   VALUE rgb;
   
-  int channel_ids[channel_count];
+  // TODO(mg): truffle c has problems with dynamic stack allocations
+  int * channel_ids = malloc(channel_count * sizeof(int));
+  // int channel_ids[channel_count];
   for (i = 0; i < channel_count; i++) {
     channel_ids[i] = FIX2INT(rb_hash_aref(channels_info[i], ID2SYM(rb_intern("id"))));
   }
@@ -57,5 +59,6 @@ VALUE psd_native_combine_cmyk_channel(VALUE self) {
     rb_ary_push(rb_iv_get(self, "@pixel_data"), INT2FIX(BUILD_PIXEL(r, g, b, a)));
   }
 
+  free(channel_ids); //TODO(mg)
   return Qnil;
 }
\ No newline at end of file
diff --git a/ext/psd_native/image_mode_rgb.c b/ext/psd_native/image_mode_rgb.c
index 9406bee..3265341 100644
--- a/ext/psd_native/image_mode_rgb.c
+++ b/ext/psd_native/image_mode_rgb.c
@@ -14,7 +14,8 @@ VALUE psd_native_combine_rgb_channel(VALUE self) {
   int i, j;
   uint32_t val, color;
 
-  int channel_ids[channel_count];
+  // int channel_ids[channel_count];
+  int * channel_ids = malloc(sizeof(int) * channel_count); // TODO(mg): trufflec cannot dynamically allocate stack space
   for (i = 0; i < channel_count; i++) {
     channel_ids[i] = FIX2INT(rb_hash_aref(channels_info[i], ID2SYM(rb_intern("id"))));
   }
@@ -43,5 +44,6 @@ VALUE psd_native_combine_rgb_channel(VALUE self) {
     rb_ary_push(pixel_data, INT2FIX(color));
   }
   
+  free(channel_ids); // TODO(mg)
   return Qnil;
 }
\ No newline at end of file
diff --git a/ext/psd_native/psd_native_ext.c b/ext/psd_native/psd_native_ext.c
index cd8a11d..9adc8b8 100644
--- a/ext/psd_native/psd_native_ext.c
+++ b/ext/psd_native/psd_native_ext.c
@@ -4,8 +4,9 @@ static VALUE psd_class;
 static VALUE logger;
 
 void Init_psd_native() {
-  psd_class = rb_const_get(rb_cObject, rb_intern("PSD"));
-  logger = rb_funcall(psd_class, rb_intern("logger"), 0);
+  // TODO(CS)
+  //psd_class = rb_const_get(rb_cObject, rb_intern("PSD"));
+  //logger = rb_funcall(psd_class, rb_intern("logger"), 0);
 
   VALUE PSDNative = rb_define_module("PSDNative");
   VALUE ImageMode = rb_define_module_under(PSDNative, "ImageMode");
@@ -81,5 +83,6 @@ void Init_psd_native() {
 }
 
 void psd_logger(char* level, char* message) {
-  rb_funcall(logger, rb_intern(level), 1, rb_str_new2(message));
+  // TODO(CS)
+  //rb_funcall(logger, rb_intern(level), 1, rb_str_new2(message));
 }
\ No newline at end of file
diff --git a/ext/psd_native/rle_decoding.c b/ext/psd_native/rle_decoding.c
index fece79d..dfc08a7 100644
--- a/ext/psd_native/rle_decoding.c
+++ b/ext/psd_native/rle_decoding.c
@@ -1,7 +1,10 @@
 #include "psd_native_ext.h"
 
 VALUE psd_native_decode_rle_channel(VALUE self) {
-  int bytes, len, val;
+  //int bytes, len, val;
+  // TODO(MG): val must not be an integer
+  VALUE val; 
+  int bytes, len;
   int i, j, k, l;
 
   int height = FIX2INT(rb_funcall(self, rb_intern("height"), 0));

```
