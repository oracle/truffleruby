ROOT := $(realpath ../../..)
RUBY := $(ROOT)/bin/truffleruby
OS := $(shell uname)

ifeq ($(TRUFFLERUBY_CEXT_ENABLED),false)
goal: no_cexts
else
goal: all
endif

no_cexts: clean
	@echo "WARNING: TRUFFLERUBY_CEXT_ENABLED is set to false - not building cexts" 1>&2

all: $(ROOT)/lib/cext/ruby.su $(ROOT)/lib/mri/openssl.su $(ROOT)/lib/mri/zlib.su $(ROOT)/lib/mri/syslog.su

clean:
	rm -f $(ROOT)/lib/cext/ruby.su
	rm -f $(ROOT)/lib/cext/ruby.bc
	rm -f cext/Makefile cext/*.bc cext/ruby.su
	rm -f $(ROOT)/lib/mri/openssl.su
	rm -f openssl/Makefile openssl/*.bc openssl/openssl.su
	rm -f $(ROOT)/lib/mri/zlib.su
	rm -f zlib/Makefile zlib/*.bc zlib/zlib.su
	rm -f $(ROOT)/lib/mri/syslog.su
	rm -f syslog/Makefile syslog/*.bc syslog/syslog.su
	rm -f trufflemock/Makefile trufflemock/*.bc trufflemock/trufflemock.su
	rm -f $(ROOT)/lib/cext/trufflemock.bc

# ruby.su
cext/Makefile: cext/extconf.rb $(ROOT)/lib/cext/trufflemock.bc
	cd cext && $(RUBY) extconf.rb

cext/ruby.su: cext/Makefile cext/ruby.c $(ROOT)/lib/cext/ruby.h
	cd cext && $(MAKE)

$(ROOT)/lib/cext/ruby.su: cext/ruby.su
	cp $< $@
	cp cext/ruby.bc $(ROOT)/lib/cext/ruby.bc

# trufflemock.bc
trufflemock/Makefile: trufflemock/extconf.rb
	cd trufflemock && $(RUBY) extconf.rb

trufflemock/trufflemock.bc: trufflemock/Makefile trufflemock/trufflemock.c $(ROOT)/lib/cext/truffle.h
	cd trufflemock && $(MAKE)

$(ROOT)/lib/cext/trufflemock.bc: trufflemock/trufflemock.bc
	cp $< $@

# openssl.su
openssl/Makefile: $(ROOT)/lib/cext/ruby.su openssl/extconf.rb
	cd openssl && $(RUBY) extconf.rb

openssl/openssl.su: openssl/Makefile openssl/*.c openssl/*.h
	cd openssl && $(MAKE)

$(ROOT)/lib/mri/openssl.su: openssl/openssl.su
	cp $< $@

# zlib.su
zlib/Makefile: $(ROOT)/lib/cext/ruby.su zlib/extconf.rb
	cd zlib && $(RUBY) extconf.rb

zlib/zlib.su: zlib/Makefile zlib/zlib.c
	cd zlib && $(MAKE)

$(ROOT)/lib/mri/zlib.su: zlib/zlib.su
	cp $< $@

# syslog.su
syslog/Makefile: $(ROOT)/lib/cext/ruby.su syslog/extconf.rb
	cd syslog && $(RUBY) extconf.rb

syslog/syslog.su: syslog/Makefile syslog/syslog.c
	cd syslog && $(MAKE)

$(ROOT)/lib/mri/syslog.su: syslog/syslog.su
	cp $< $@
