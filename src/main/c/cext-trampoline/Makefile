Q$(MX_VERBOSE) = @

OS := $(shell uname)
ifeq ($(OS),Darwin)
SOEXT := dylib
else
SOEXT := so
endif

# cc should be enough to compile this library but might produce different warnings based on the system compiler.
# If that is the case and fails the build, it might be better to use instead:
# CC := $(GRAALVM_TOOLCHAIN_CC)

# Same values as in RbConfig
ifeq ($(OS),Darwin)
CC := clang -fdeclspec
else
CC := gcc
endif

CFLAGS := -Wall -Werror -fPIC -std=c99 -g
LDFLAGS := -m64

ROOT := $(realpath ../../../..)
RUBY_HDR_DIR := $(ROOT)/lib/cext/include

OBJECT_FILES := trampoline.o st.o cext_constants.o

libtrufflerubytrampoline.$(SOEXT): $(OBJECT_FILES) Makefile
	$(Q) $(CC) -shared $(LDFLAGS) -o $@ $(OBJECT_FILES)

%.o: %.c Makefile
	$(Q) $(CC) -o $@ -c $(CFLAGS) $(LDFLAGS) -I$(RUBY_HDR_DIR) -I$(ROOT)/lib/cext/include/stubs -I$(TRUFFLE_NFI_NATIVE_INCLUDE) $<
