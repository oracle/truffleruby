ROOT := $(realpath ../../..)
RUBY := $(ROOT)/bin/truffleruby -Xgraal.warn_unless=false
OS := $(shell uname)
RUBY_HEADERS := $(wildcard $(ROOT)/lib/cext/*.h) $(wildcard $(ROOT)/lib/cext/ruby/*.h)
RBCONFIG := $(ROOT)/src/main/ruby/core/rbconfig.rb
PREPROCESS := $(ROOT)/lib/cext/preprocess.rb
MKMF := $(ROOT)/lib/mri/mkmf.rb
BASIC_EXTCONF_DEPS := $(RUBY_HEADERS) $(RBCONFIG) $(PREPROCESS) $(MKMF)
TRUFFLE_MOCK := $(ROOT)/lib/cext/trufflemock.bc
RUBY_LIBRARY := $(ROOT)/lib/cext/ruby.su
# These 2 libraries are needed for try_link() in extconf.rb
EXTCONF_DEPS := $(BASIC_EXTCONF_DEPS) $(TRUFFLE_MOCK) $(RUBY_LIBRARY)
CEXTS_DIR := $(ROOT)/lib/mri

ifeq ($(TRUFFLERUBY_CEXT_ENABLED),false)
goal: no_cexts
else
goal: all
endif

no_cexts: clean
	@echo "WARNING: TRUFFLERUBY_CEXT_ENABLED is set to false - not building cexts" 1>&2

all: $(RUBY_LIBRARY) $(CEXTS_DIR)/openssl.su $(CEXTS_DIR)/zlib.su $(CEXTS_DIR)/syslog.su

clean:
	rm -f $(RUBY_LIBRARY)
	rm -f $(ROOT)/lib/cext/ruby.bc
	rm -f cext/Makefile cext/*.bc cext/ruby.su
	rm -f $(CEXTS_DIR)/openssl.su
	rm -f openssl/Makefile openssl/*.bc openssl/openssl.su
	rm -f $(CEXTS_DIR)/zlib.su
	rm -f zlib/Makefile zlib/*.bc zlib/zlib.su
	rm -f $(CEXTS_DIR)/syslog.su
	rm -f syslog/Makefile syslog/*.bc syslog/syslog.su
	rm -f trufflemock/Makefile trufflemock/*.bc trufflemock/trufflemock.su
	rm -f $(ROOT)/lib/cext/trufflemock.bc

# trufflemock.bc
trufflemock/Makefile: trufflemock/extconf.rb $(BASIC_EXTCONF_DEPS)
	cd trufflemock && $(RUBY) extconf.rb

trufflemock/trufflemock.bc: trufflemock/Makefile trufflemock/trufflemock.c
	cd trufflemock && $(MAKE)

$(ROOT)/lib/cext/trufflemock.bc: trufflemock/trufflemock.bc
	cp $< $@

# ruby.su
cext/Makefile: cext/extconf.rb $(BASIC_EXTCONF_DEPS)
	cd cext && $(RUBY) extconf.rb

cext/ruby.su: cext/Makefile cext/ruby.c
	cd cext && $(MAKE)

$(RUBY_LIBRARY): cext/ruby.su
	cp $< $@
	cp cext/ruby.bc $(ROOT)/lib/cext/ruby.bc

# openssl.su
openssl/Makefile: openssl/extconf.rb $(EXTCONF_DEPS)
	cd openssl && $(RUBY) extconf.rb

openssl/openssl.su: openssl/Makefile openssl/*.c openssl/*.h
	cd openssl && $(MAKE)

$(CEXTS_DIR)/openssl.su: openssl/openssl.su
	cp $< $@

# zlib.su
zlib/Makefile: zlib/extconf.rb $(EXTCONF_DEPS)
	cd zlib && $(RUBY) extconf.rb

zlib/zlib.su: zlib/Makefile zlib/zlib.c
	cd zlib && $(MAKE)

$(CEXTS_DIR)/zlib.su: zlib/zlib.su
	cp $< $@

# syslog.su
syslog/Makefile: syslog/extconf.rb $(EXTCONF_DEPS)
	cd syslog && $(RUBY) extconf.rb

syslog/syslog.su: syslog/Makefile syslog/syslog.c
	cd syslog && $(MAKE)

$(CEXTS_DIR)/syslog.su: syslog/syslog.su
	cp $< $@
