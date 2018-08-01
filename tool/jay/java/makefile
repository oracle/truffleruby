# jay/java

d	= arith recover

all clean dist distclean test::
	for d in $d; do \
	  echo; echo ' ----- jay/java/'$$d; echo; \
	  ( cd $$d && $(MAKE) $@ ) done

