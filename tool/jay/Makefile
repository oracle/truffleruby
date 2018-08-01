# jay/src [leaf]

# Windows Services For Unix:
#   comment 'depend' stuff below.
#   make will create jay.exe with GNU C.
#   make CC=c89 will create jay.exe with Visual C++.

CC	= `if [ -n "$$COMSPEC" ]; then echo gcc; else echo cc; fi`
CFLAGS	= #-s # -g

c	= closure.c error.c lalr.c lr0.c main.c mkpar.c output.c reader.c \
	  symtab.c verbose.c warshall.c
e	= `if [ -n "$$COMSPEC" ]; then echo jay.exe; else echo jay; fi`

all: jay
clean:			; rm -f *.o
dist: all
distclean: clean	; rm -f jay jay.exe
test: all

jay: $(c:.c=.o)		; $(CC) -o $e $(CFLAGS) $(c:.c=.o)

depend: $c		; $(CC) -MM $(CFLAGS) $c > $@
-include depend
