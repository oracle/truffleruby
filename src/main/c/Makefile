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
RUBY := $(MX_HOME)/mx -p $(ROOT) miniruby_for_building_cexts

CC := clang
CFLAGS := -Wall -Werror -Wno-unused-function -fPIC -std=c99
LDFLAGS := -m64

TRUFFLE_POSIX := truffleposix/libtruffleposix.$(DLEXT)
SULONG_HEADERS := $(ROOT)/lib/cext/include/sulong/truffle.h $(SULONG_POLYGLOT_H)
GENERATE_SULONG_MOCK := $(ROOT)/tool/generate-sulongmock.rb

RUBY_HEADERS := $(wildcard $(ROOT)/lib/cext/include/*.h) $(wildcard $(ROOT)/lib/cext/include/*/*.h) $(wildcard $(ROOT)/lib/cext/include/*/*/*.h)
RBCONFIG := $(ROOT)/lib/truffle/rbconfig.rb
RBCONFIG_MKMF := $(ROOT)/lib/truffle/rbconfig-for-mkmf.rb
PREPROCESS := $(ROOT)/lib/cext/preprocess.rb
MKMF := $(ROOT)/lib/mri/mkmf.rb
BASIC_EXTCONF_DEPS := $(TRUFFLE_POSIX) $(RUBY_HEADERS) $(RBCONFIG) $(RBCONFIG_MKMF) $(PREPROCESS) $(MKMF)
# These libraries are needed for try_link() in extconf.rb
EXTCONF_DEPS := $(BASIC_EXTCONF_DEPS) sulongmock/sulongmock.o cext/ruby.o cext/ruby.su

IF_EXTCONF_FAIL := ( echo "`pwd`/extconf.rb failed:" 1>&2 && cat mkmf.log && false )

all:  cext/ruby.su \
      etc/etc.su \
      nkf/nkf.su \
      openssl/openssl.su \
      psych/psych.su \
      rbconfig-sizeof/sizeof.su \
      syslog/syslog.su \
      zlib/zlib.su

clean: clean_cexts clean_truffleposix

clean_truffleposix:
	rm -f $(TRUFFLE_POSIX) truffleposix/*.o

clean_cexts:
	rm -f cext/Makefile cext/*.bc cext/ruby.o cext/ruby.su
	rm -f etc/Makefile etc/*.bc etc/etc.su etc/constdefs.h
	rm -f nkf/Makefile nkf/*.bc nkf/nkf.su
	rm -f openssl/Makefile openssl/*.bc openssl/openssl.su
	rm -f psych/Makefile psych/*.bc psych/yaml/*.bc psych/psych.su
	rm -f rbconfig-sizeof/Makefile rbconfig-sizeof/*.bc rbconfig-sizeof/sizeof.su
	rm -f sulongmock/sulongmock.o
	rm -f syslog/Makefile syslog/*.bc syslog/syslog.su
	rm -f zlib/Makefile zlib/*.bc zlib/zlib.su

# truffleposix
$(TRUFFLE_POSIX): truffleposix/Makefile truffleposix/truffleposix.c
	cd truffleposix && $(MAKE)

# sulongmock.o
sulongmock/sulongmock.c: $(GENERATE_SULONG_MOCK) $(SULONG_HEADERS) $(TRUFFLE_POSIX)
	$(RUBY) $(GENERATE_SULONG_MOCK) $(SULONG_HEADERS)

sulongmock/sulongmock.o: sulongmock/sulongmock.c
	$(CC) -o $@ -c $(CFLAGS) $(LDFLAGS) -I$(ROOT)/lib/cext/include -I$(SULONG_HEADERS_DIR) $<

# ruby.o
cext/ruby.o: cext/*.c $(RUBY_HEADERS)
	$(CC) -o $@ -c $(CFLAGS) $(LDFLAGS) -DSULONG_POLYGLOT_H=\"$(SULONG_POLYGLOT_H)\" -I$(ROOT)/lib/cext/include cext/ruby.c

# ruby.su
cext/Makefile: cext/extconf.rb $(BASIC_EXTCONF_DEPS)
	cd cext && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

cext/ruby.su: cext/Makefile cext/*.c
	cd cext && $(MAKE)

# etc.su
etc/Makefile: etc/extconf.rb $(EXTCONF_DEPS)
	cd etc && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

etc/etc.su: etc/Makefile etc/etc.c
	cd etc && $(MAKE)

etc/etc.c: etc/constdefs.h

etc/constdefs.h: etc/mkconstants.rb
	$(RUBY) $< -o $@

# nkf.su
nkf/Makefile: nkf/extconf.rb $(EXTCONF_DEPS)
	cd nkf && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

nkf/nkf.su: nkf/Makefile nkf/nkf.c nkf/nkf-utf8/*.c nkf/nkf-utf8/*.h
	cd nkf && $(MAKE)

# openssl.su
openssl/Makefile: openssl/extconf.rb $(EXTCONF_DEPS)
	cd openssl && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

openssl/openssl.su: openssl/Makefile openssl/*.c openssl/*.h
	cd openssl && $(MAKE)

# psych.su
# Always use the bundled libyaml, as we want it as bitcode and avoid extra handles
psych/Makefile: psych/extconf.rb $(EXTCONF_DEPS)
	cd psych && $(RUBY) extconf.rb --enable-bundled-libyaml || $(IF_EXTCONF_FAIL)

psych/psych.su: psych/Makefile psych/*.c psych/*.h psych/yaml/*.c psych/yaml/*.h
	cd psych && $(MAKE)

# rbconfig/sizeof.su
rbconfig-sizeof/Makefile: rbconfig-sizeof/extconf.rb $(EXTCONF_DEPS)
	cd rbconfig-sizeof && $(RUBY) -rmkmf extconf.rb || $(IF_EXTCONF_FAIL)

rbconfig-sizeof/sizeof.su: rbconfig-sizeof/Makefile rbconfig-sizeof/sizes.c
	cd rbconfig-sizeof && $(MAKE)

# syslog.su
syslog/Makefile: syslog/extconf.rb $(EXTCONF_DEPS)
	cd syslog && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

syslog/syslog.su: syslog/Makefile syslog/syslog.c
	cd syslog && $(MAKE)

# zlib.su
zlib/Makefile: zlib/extconf.rb $(EXTCONF_DEPS)
	cd zlib && $(RUBY) extconf.rb || $(IF_EXTCONF_FAIL)

zlib/zlib.su: zlib/Makefile zlib/zlib.c
	cd zlib && $(MAKE)
