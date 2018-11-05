OS := $(shell uname)
ifeq ($(OS),Darwin)
DLEXT := dylib
else
DLEXT := so
endif

ROOT := $(realpath ../../..)
CEXTS_DIR := $(ROOT)/lib/mri
RUBY := $(ROOT)/bin/truffleruby --disable-gems

CC := clang
CFLAGS := -Wall -Werror -Wno-unused-function -fPIC -std=c99
LDFLAGS := -m64

TRUFFLE_POSIX := $(ROOT)/lib/cext/truffleposix.$(DLEXT)
SULONG_HEADERS := $(wildcard $(ROOT)/lib/cext/include/sulong/*.h)
GENERATE_SULONG_MOCK := $(ROOT)/tool/generate-sulongmock.rb
SULONG_MOCK := $(ROOT)/lib/cext/sulongmock.o
RUBY_O := $(ROOT)/lib/cext/ruby.o
RUBY_LIBRARY := $(ROOT)/lib/cext/ruby.su

RUBY_HEADERS := $(wildcard $(ROOT)/lib/cext/include/*.h) $(wildcard $(ROOT)/lib/cext/include/*/*.h) $(wildcard $(ROOT)/lib/cext/include/*/*/*.h)
RBCONFIG := $(ROOT)/lib/truffle/rbconfig.rb
RBCONFIG_MKMF := $(ROOT)/lib/truffle/rbconfig-for-mkmf.rb
PREPROCESS := $(ROOT)/lib/cext/preprocess.rb
MKMF := $(ROOT)/lib/mri/mkmf.rb
BASIC_EXTCONF_DEPS := $(TRUFFLE_POSIX) $(RUBY_HEADERS) $(RBCONFIG) $(RBCONFIG_MKMF) $(PREPROCESS) $(MKMF)
# These libraries are needed for try_link() in extconf.rb
EXTCONF_DEPS := $(BASIC_EXTCONF_DEPS) $(SULONG_MOCK) $(RUBY_O) $(RUBY_LIBRARY)

IF_EXTCONF_FAIL := ( echo "`pwd`/extconf.rb failed:" 1>&2 && cat mkmf.log && false )

ifeq ($(TRUFFLERUBY_CEXT_ENABLED),false)
goal: no_cexts $(TRUFFLE_POSIX)
else
goal: all
endif

no_cexts: clean_cexts
	@echo "WARNING: TRUFFLERUBY_CEXT_ENABLED is set to false - not building cexts" 1>&2

all: $(RUBY_LIBRARY) $(CEXTS_DIR)/openssl.su $(CEXTS_DIR)/zlib.su \
			$(CEXTS_DIR)/psych.su $(CEXTS_DIR)/syslog.su $(CEXTS_DIR)/nkf.su \
			$(CEXTS_DIR)/etc.su $(CEXTS_DIR)/rbconfig/sizeof.su

clean: clean_cexts clean_truffleposix

clean_truffleposix:
	rm -f $(TRUFFLE_POSIX)
	rm -f truffleposix/*.o truffleposix/truffleposix.$(DLEXT)

clean_cexts:
	rm -f $(SULONG_MOCK)
	rm -f $(RUBY_O)
	rm -f $(RUBY_LIBRARY)
	rm -f $(ROOT)/lib/cext/ruby.o
	rm -f cext/Makefile cext/*.bc cext/ruby.su
	rm -f $(CEXTS_DIR)/openssl.su
	rm -f openssl/Makefile openssl/*.bc openssl/openssl.su
	rm -f $(CEXTS_DIR)/zlib.su
	rm -f zlib/Makefile zlib/*.bc zlib/zlib.su
	rm -f $(CEXTS_DIR)/psych.su
	rm -f psych/Makefile psych/*.bc psych/yaml/*.bc psych/psych.su
	rm -f $(CEXTS_DIR)/syslog.su
	rm -f syslog/Makefile syslog/*.bc syslog/syslog.su
	rm -f $(CEXTS_DIR)/nkf.su
	rm -f nkf/Makefile nkf/*.bc nkf/nkf.su
	rm -f $(CEXTS_DIR)/etc.su
	rm -f etc/Makefile etc/*.bc etc/etc.su etc/constdefs.h
	rm -f $(CEXTS_DIR)/rbconfig/sizeof.su
	rm -f rbconfig-sizeof/Makefile rbconfig-sizeof/*.bc rbconfig-sizeof/sizeof.su

# truffleposix
truffleposix/truffleposix.$(DLEXT): truffleposix/Makefile truffleposix/truffleposix.c
	cd truffleposix && $(MAKE)

$(TRUFFLE_POSIX): truffleposix/truffleposix.$(DLEXT)
	cp $< $@

# sulongmock.o
sulongmock/sulongmock.c: $(GENERATE_SULONG_MOCK) $(SULONG_HEADERS) $(TRUFFLE_POSIX)
	$(RUBY) $(GENERATE_SULONG_MOCK)

$(SULONG_MOCK): sulongmock/sulongmock.c
	$(CC) -o $@ -c $(CFLAGS) $(LDFLAGS) -I$(ROOT)/lib/cext/include $<

# ruby.o
$(RUBY_O): cext/*.c $(RUBY_HEADERS)
	$(CC) -o $@ -c $(CFLAGS) $(LDFLAGS) -I$(ROOT)/lib/cext/include cext/ruby.c

# ruby.su
cext/Makefile: cext/extconf.rb $(BASIC_EXTCONF_DEPS)
	cd cext && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

cext/ruby.su: cext/Makefile cext/*.c
	cd cext && $(MAKE)

$(RUBY_LIBRARY): cext/ruby.su
	cp $< $@

# openssl.su
openssl/Makefile: openssl/extconf.rb $(EXTCONF_DEPS)
	cd openssl && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

openssl/openssl.su: openssl/Makefile openssl/*.c openssl/*.h
	cd openssl && $(MAKE)

$(CEXTS_DIR)/openssl.su: openssl/openssl.su
	cp $< $@

# zlib.su
zlib/Makefile: zlib/extconf.rb $(EXTCONF_DEPS) $(ROOT)/lib/cext/patches/zlib_patches.rb
	cd zlib && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

zlib/zlib.su: zlib/Makefile zlib/zlib.c
	cd zlib && $(MAKE)

$(CEXTS_DIR)/zlib.su: zlib/zlib.su
	cp $< $@

# syslog.su
syslog/Makefile: syslog/extconf.rb $(EXTCONF_DEPS)
	cd syslog && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

syslog/syslog.su: syslog/Makefile syslog/syslog.c
	cd syslog && $(MAKE)

$(CEXTS_DIR)/syslog.su: syslog/syslog.su
	cp $< $@

# nkf.su
nkf/Makefile: nkf/extconf.rb $(EXTCONF_DEPS)
	cd nkf && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

nkf/nkf.su: nkf/Makefile nkf/nkf.c nkf/nkf-utf8/*.c nkf/nkf-utf8/*.h
	cd nkf && $(MAKE)

$(CEXTS_DIR)/nkf.su: nkf/nkf.su
	cp $< $@

# etc.su
etc/Makefile: etc/extconf.rb $(EXTCONF_DEPS)
	cd etc && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

etc/etc.su: etc/Makefile etc/etc.c
	cd etc && $(MAKE)

etc/etc.c: etc/constdefs.h

etc/constdefs.h: etc/mkconstants.rb
	$(RUBY) $< > $@

$(CEXTS_DIR)/etc.su: etc/etc.su
	cp $< $@

# rbconfig/sizeof.su
rbconfig-sizeof/Makefile: rbconfig-sizeof/extconf.rb $(EXTCONF_DEPS)
	cd rbconfig-sizeof && $(RUBY) -rmkmf extconf.rb || $(IF_EXTCONF_FAIL)

rbconfig-sizeof/sizeof.su: rbconfig-sizeof/Makefile rbconfig-sizeof/sizes.c
	cd rbconfig-sizeof && $(MAKE)

$(CEXTS_DIR)/rbconfig/sizeof.su: rbconfig-sizeof/sizeof.su
	cp $< $@

# psych.su
# Always use the bundled libyaml, as we want it as bitcode and avoid extra handles
psych/Makefile: psych/extconf.rb $(EXTCONF_DEPS)
	cd psych && $(RUBY) extconf.rb --enable-bundled-libyaml || $(IF_EXTCONF_FAIL)

psych/psych.su: psych/Makefile psych/*.c psych/*.h psych/yaml/*.c psych/yaml/*.h
	cd psych && $(MAKE)

$(CEXTS_DIR)/psych.su: psych/psych.su
	cp $< $@
