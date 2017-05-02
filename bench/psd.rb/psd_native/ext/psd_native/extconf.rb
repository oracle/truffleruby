require 'mkmf'
$CFLAGS << ' -Wall'
create_makefile('psd_native/psd_native')
