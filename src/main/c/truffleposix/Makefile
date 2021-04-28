Q$(MX_VERBOSE) = @

OS := $(shell uname)
ifeq ($(OS),Darwin)
SOEXT := dylib
else
SOEXT := so
endif

CC := $(GRAALVM_TOOLCHAIN_CC)
CFLAGS := -Wall -Werror -fPIC -std=c99 -g
LDFLAGS := -m64

ifeq ($(OS),Darwin)
DARWIN_VERSION := $(shell uname -r | sed 's/\..*//')
MACOS_SIERRA := $(shell test $(DARWIN_VERSION) -gt 15 || echo no)
ifeq ($(MACOS_SIERRA),no)
CFLAGS := $(CFLAGS) -DNO_CLOCK_GETTIME
endif
endif

ifeq ($(OS),Linux)
LDFLAGS += -lrt
endif

ifeq ($(OS),SunOS)
LDFLAGS += -lrt
endif

ROOT := $(realpath ../../../..)
RUBY_HDR_DIR := $(ROOT)/lib/cext/include

libtruffleposix.$(SOEXT): truffleposix.o Makefile
	$(Q) $(CC) -shared $(LDFLAGS) -o $@ $<

truffleposix.o: truffleposix.c Makefile
	$(Q) $(CC) -o $@ -c $(CFLAGS) $(LDFLAGS) -I$(NFI_HEADERS_DIR) -I$(RUBY_HDR_DIR) $<
