# jay/java/recover [leaf]

yydebug = ../../yydebug/yydebug.jar

CLASSPATH = ..:$(yydebug)
JAYFLAGS = -t

JAVAC	= javac -classpath $(CLASSPATH)
JAY	= ../../src/jay < ../skeleton.java
JAYT	= ../../src/jay < ../skeleton.tables

all:	recover.jar tables.jar
clean:			; rm -rf Recover.java Recover.tables \
				manifest *.class y.output yacc.?
dist:	all
distclean: clean	; rm -f *.jar
test:	all
	@ set -x; \
	  for i in recover.jar tables.jar; do \
	    { echo opt; echo opt ,; echo opt 1 2; } | java -jar $$i; \
	    { echo seq 1; echo seq; echo seq 1,; } | java -jar $$i; \
	    { echo list 1,2; echo list; echo list 1,; } | \
		java -jar $$i; done

recover.jar: Recover.jay
	$(JAY) $(JAYFLAGS) Recover.jay > Recover.java
	$(JAVAC) $(JAVACFLAGS) Recover.java
	echo 'Main-Class: Recover' > manifest
	jar um manifest *.class < $(yydebug) > $@
	$(MAKE) clean

tables.jar: Recover.jay
	$(JAYT) $(JAYFLAGS) Recover.jay | tee Recover.java | \
	  sed -n 's/^\/\/yy//p' > Recover.tables
	$(JAVAC) $(JAVACFLAGS) Recover.java
	echo 'Main-Class: Recover' > manifest
	jar um manifest *.class Recover.tables < $(yydebug) > $@
	$(MAKE) clean
