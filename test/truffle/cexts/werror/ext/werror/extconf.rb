require 'mkmf'
# No -pedantic because polyglot-impl.h fails that, and well, it's too pedantic
$warnflags += ' -W -Wall -Wextra -Werror'
create_makefile('werror')
