These benchmarks are originally from
[Bench 9000](https://github.com/jruby/bench9000).

We are using ChunkyPNG at revision `efd61c8d0ddcabdcf09fb44f8e8c1ba709995940`.
We use this old revision because we know it has interesting patterns of
meta-programming that we are interested in benchmarking. Some of these
patterns were replaced in later versions.

We've also applied this patch, to fix obvious bugs in keyword arguments which
limits optimisations.

```
diff --git a/lib/chunky_png/canvas/resampling.rb b/lib/chunky_png/canvas/resampling.rb
index e3d97ca..58c234a 100644
--- a/lib/chunky_png/canvas/resampling.rb
+++ b/lib/chunky_png/canvas/resampling.rb
@@ -39,8 +39,8 @@ module ChunkyPNG
       # @param [Integer] new_width The width of the destination
       # @return [Array<Integer>, Array<Integer>] Two arrays of indicies and residues
       def steps_residues(width, new_width)
-        indicies = Array.new(size=new_width, obj=nil)
-        residues = Array.new(size=new_width, obj=nil)
+        indicies = Array.new(new_width, 0)
+        residues = Array.new(new_width, 0)
         
         # This works by accumulating the fractional error and
         # overflowing when necessary.
@@ -80,7 +80,7 @@ module ChunkyPNG
         steps_y = steps(height, new_height)
 
 
-        pixels = Array(size=new_width*new_height)
+        pixels = Array.new(new_width*new_height, 0)
         i = 0
         for y in steps_y
           for x in steps_x
@@ -104,7 +104,7 @@ module ChunkyPNG
         index_x, interp_x = steps_residues(width, new_width)
         index_y, interp_y = steps_residues(height, new_height)
 
-        pixels = Array(size=new_width*new_height)
+        pixels = Array.new(new_width*new_height, 0)
         i = 0
         for y in 1..new_height
           # Clamp the indicies to the edges of the image

```

OilyPNG is at revision `705202d54c891c709a2c9075e6d0cd4bba04f209`.

Modified to use `rb_jt_scan_args_12` in `operations.c`.
