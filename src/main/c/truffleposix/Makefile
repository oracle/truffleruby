OS := $(shell uname)
ifeq ($(OS),Darwin)
DLEXT := dylib
else
DLEXT := so
endif

ifeq ($(OS),SunOS)
CC := gcc
else
CC := cc
endif

CFLAGS := -Wall -Werror -fPIC
LDFLAGS := -m64

truffleposix.$(DLEXT): truffleposix.o Makefile
	$(CC) -shared $(LDFLAGS) -o $@ $<

truffleposix.o: truffleposix.c Makefile
	$(CC) -o $@ -c $(CFLAGS) $(LDFLAGS) $<
