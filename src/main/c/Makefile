Q$(MX_VERBOSE) = @
MX_SUBPROCESS_FLAGS = $(MX_VERBOSE:y=-v)
MKMF_MAKEFILE_SUBPROCESS_FLAGS = $(MX_VERBOSE:y=V=1)

OS := $(shell uname)
ifeq ($(OS),Darwin)
DLEXT := dylib
else
DLEXT := so
endif

ifndef MX_HOME
$(error This Makefile should be called from mx)
endif

ROOT := $(realpath ../../..)
RUBY := $(MX_HOME)/mx $(MX_SUBPROCESS_FLAGS) -p $(ROOT) miniruby_for_building_cexts

CC := clang
CFLAGS := -Wall -Werror -Wno-unused-function -fPIC -std=c99
LDFLAGS := -m64

TRUFFLE_POSIX := truffleposix/libtruffleposix.$(DLEXT)
SPAWN_HELPER := spawn-helper/spawn-helper

RUBY_HEADERS := $(wildcard $(ROOT)/lib/cext/include/*.h) $(wildcard $(ROOT)/lib/cext/include/*/*.h) $(wildcard $(ROOT)/lib/cext/include/*/*/*.h)
RBCONFIG := $(ROOT)/lib/truffle/rbconfig.rb
RBCONFIG_MKMF := $(ROOT)/lib/truffle/rbconfig-for-mkmf.rb
MKMF := $(ROOT)/lib/mri/mkmf.rb
LIBTRUFFLERUBY = cext/libtruffleruby.$(DLEXT)
BASIC_EXTCONF_DEPS := $(SPAWN_HELPER) $(TRUFFLE_POSIX) $(RUBY_HEADERS) $(RBCONFIG) $(RBCONFIG_MKMF) $(MKMF)
# C extensions link against libtruffleruby (and might do have_func() checks against it), so it needs to be there before.
# However, if libtruffleruby is recompiled, there is no need to rebuild C extensions, so it's a order-only-prerequisite.
EXTCONF_DEPS := $(BASIC_EXTCONF_DEPS) | $(LIBTRUFFLERUBY)

IF_EXTCONF_FAIL := ( echo "`pwd`/extconf.rb failed:" 1>&2 && cat mkmf.log && false )

all: $(LIBTRUFFLERUBY) openssl/openssl.$(DLEXT) zlib/zlib.$(DLEXT) \
			psych/psych.$(DLEXT) syslog/syslog.$(DLEXT) nkf/nkf.$(DLEXT) \
			etc/etc.$(DLEXT) rbconfig-sizeof/sizeof.$(DLEXT)

clean: clean_cexts clean_truffleposix

clean_truffleposix:
	$(Q) rm -f $(TRUFFLE_POSIX) truffleposix/*.o

clean_cexts:
	$(Q) rm -f cext/Makefile cext/*.o $(LIBTRUFFLERUBY)
	$(Q) rm -f openssl/Makefile openssl/*.o openssl/openssl.$(DLEXT)
	$(Q) rm -f zlib/Makefile zlib/*.o zlib/zlib.$(DLEXT)
	$(Q) rm -f psych/Makefile psych/*.o psych/yaml/*.o psych/psych.$(DLEXT)
	$(Q) rm -f syslog/Makefile syslog/*.o syslog/syslog.$(DLEXT)
	$(Q) rm -f nkf/Makefile nkf/*.o nkf/nkf.$(DLEXT)
	$(Q) rm -f etc/Makefile etc/*.o etc/etc.$(DLEXT) etc/constdefs.h
	$(Q) rm -f rbconfig-sizeof/Makefile rbconfig-sizeof/*.o rbconfig-sizeof/sizeof.$(DLEXT)

# spawn-helper
$(SPAWN_HELPER): spawn-helper/Makefile spawn-helper/spawn-helper.c
	$(Q) cd spawn-helper && $(MAKE)

# truffleposix
$(TRUFFLE_POSIX): truffleposix/Makefile truffleposix/truffleposix.c
	$(Q) cd truffleposix && $(MAKE)

# libtruffleruby
cext/Makefile: cext/extconf.rb $(BASIC_EXTCONF_DEPS)
	$(Q) cd cext && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

$(LIBTRUFFLERUBY): cext/Makefile cext/*.c
	$(Q) cd cext && $(MAKE)

# openssl
openssl/Makefile: openssl/extconf.rb $(EXTCONF_DEPS)
	$(Q) cd openssl && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

openssl/openssl.$(DLEXT): openssl/Makefile openssl/*.c openssl/*.h
	$(Q) cd openssl && $(MAKE) $(MKMF_MAKEFILE_SUBPROCESS_FLAGS)

# zlib
zlib/Makefile: zlib/extconf.rb $(EXTCONF_DEPS)
	$(Q) cd zlib && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

zlib/zlib.$(DLEXT): zlib/Makefile zlib/zlib.c
	$(Q) cd zlib && $(MAKE) $(MKMF_MAKEFILE_SUBPROCESS_FLAGS)

# syslog
syslog/Makefile: syslog/extconf.rb $(EXTCONF_DEPS)
	$(Q) cd syslog && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

syslog/syslog.$(DLEXT): syslog/Makefile syslog/syslog.c
	$(Q) cd syslog && $(MAKE) $(MKMF_MAKEFILE_SUBPROCESS_FLAGS)

# nkf
nkf/Makefile: nkf/extconf.rb $(EXTCONF_DEPS)
	$(Q) cd nkf && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

nkf/nkf.$(DLEXT): nkf/Makefile nkf/nkf.c nkf/nkf-utf8/*.c nkf/nkf-utf8/*.h
	$(Q) cd nkf && $(MAKE) $(MKMF_MAKEFILE_SUBPROCESS_FLAGS)

# etc
etc/Makefile: etc/extconf.rb $(EXTCONF_DEPS)
	$(Q) cd etc && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

etc/etc.$(DLEXT): etc/Makefile etc/etc.c
	$(Q) cd etc && $(MAKE) $(MKMF_MAKEFILE_SUBPROCESS_FLAGS)

etc/etc.c: etc/constdefs.h

etc/constdefs.h: etc/mkconstants.rb
	$(Q) $(RUBY) $< -o $@

# rbconfig/sizeof
rbconfig-sizeof/Makefile: rbconfig-sizeof/extconf.rb $(EXTCONF_DEPS)
	$(Q) cd rbconfig-sizeof && $(RUBY) -rmkmf extconf.rb || $(IF_EXTCONF_FAIL)

rbconfig-sizeof/sizeof.$(DLEXT): rbconfig-sizeof/Makefile rbconfig-sizeof/sizes.c
	$(Q) cd rbconfig-sizeof && $(MAKE) $(MKMF_MAKEFILE_SUBPROCESS_FLAGS)

# psych
# Always use the bundled libyaml, as we want it as bitcode and avoid extra handles
psych/Makefile: psych/extconf.rb $(EXTCONF_DEPS)
	$(Q) cd psych && $(RUBY) extconf.rb --enable-bundled-libyaml || $(IF_EXTCONF_FAIL)

psych/psych.$(DLEXT): psych/Makefile psych/*.c psych/*.h psych/yaml/*.c psych/yaml/*.h
	$(Q) cd psych && $(MAKE) $(MKMF_MAKEFILE_SUBPROCESS_FLAGS)
