require 'mkmf'
$INCFLAGS << " -I$(top_srcdir)" if $extmk
create_makefile 'rbs_extension'
