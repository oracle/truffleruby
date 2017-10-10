# frozen_string_literal: false
#
# extconf.rb
#
# $Id$
#

require 'mkmf'

have_type('z_crc_t', 'zlib.h')

create_makefile('zlib')
