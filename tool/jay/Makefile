c	= closure.c error.c lalr.c lr0.c main.c mkpar.c output.c reader.c \
	  symtab.c verbose.c warshall.c

jay: $(c:.c=.o)
	$(CC) -o $@ $(CFLAGS) $^

clean:
	rm -f *.o

depend: $c
	$(CC) -MM $(CFLAGS) $c > $@

-include depend
