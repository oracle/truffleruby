SSCLI = /usr/local/sscli#	ROTOR_DIR of MS' shared source CLI

ENV = [ -z "$$ROTOR_DIR" ] && { d=`pwd`; cd $(SSCLI) && . env.sh; cd $$d; }

CSCFLAGS = /debug+ /lib:.. /reference:yyDebug.dll /main:Arith.Arith
JAYFLAGS = -c -t

CLIX	= clix
CSC	= csc
JAY	= ../../jay/jay.$(OSTYPE) < ../skeleton.cs

all:	arith.exe
clean:			; rm -rf arith.cs arith.ildb y.output y.tab.?
distclean: clean	; rm -f arith.exe
test:	arith.exe
	@ $(ENV); set -x; \
	  { echo 36/5; echo '5 * (3+4)'; } | $(CLIX) arith.exe trace

.SUFFIXES: .exe .cs .jay
.jay.cs:	; $(JAY) $(JAYFLAGS) $*.jay > $*.cs
.cs.exe:	; $(ENV); $(CSC) $(CSCFLAGS) $*.cs
.jay.exe:	; $(JAY) $(JAYFLAGS) $*.jay > $*.cs
		  $(ENV); $(CSC) $(CSCFLAGS) $*.cs
