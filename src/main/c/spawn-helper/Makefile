Q$(MX_VERBOSE) = @

CC := $(GRAALVM_TOOLCHAIN_CC)
CFLAGS := -Wall -Werror -std=c99
LDFLAGS := -m64

spawn-helper: spawn-helper.c
	$(Q) $(CC) $(LDFLAGS) -o $@ $<
