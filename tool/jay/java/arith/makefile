# jay/java/arith [leaf]

yydebug = ../../yydebug/yydebug.jar

CLASSPATH = ..:$(yydebug)
JAYFLAGS = -t

JAVAC	= javac -classpath $(CLASSPATH)
JAY	= ../../src/jay < ../skeleton.java
JAYT	= ../../src/jay < ../skeleton.tables

all:	arith.jar tables.jar
clean:			; rm -rf Arith.java Arith.tables \
				manifest *.class y.output yacc.?
dist:	all
distclean: clean	; rm -f *.jar
test:	all
	@ set -x; \
	  for i in arith.jar tables.jar; do \
	    { echo 36/5; echo '5 * (3+4)'; } | java -jar $$i trace; done

arith.jar: Arith.jay
	$(JAY) $(JAYFLAGS) Arith.jay > Arith.java
	$(JAVAC) $(JAVACFLAGS) Arith.java
	echo 'Main-Class: arith.Arith' > manifest
	( cd .. && \
	    jar um arith/manifest arith/*.class ) < $(yydebug) > $@
	$(MAKE) clean

tables.jar: Arith.jay
	$(JAYT) $(JAYFLAGS) Arith.jay | tee Arith.java | \
	  sed -n 's/^\/\/yy//p' > Arith.tables
	$(JAVAC) $(JAVACFLAGS) Arith.java
	echo 'Main-Class: arith.Arith' > manifest
	( cd .. && \
	    jar um arith/manifest arith/*.class arith/Arith.tables ) \
		< $(yydebug) > $@
	$(MAKE) clean
