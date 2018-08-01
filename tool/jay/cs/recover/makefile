SSCLI = /usr/local/sscli#	ROTOR_DIR of MS' shared source CLI

ENV = [ -z "$$ROTOR_DIR" ] && { d=`pwd`; cd $(SSCLI) && . env.sh; cd $$d; }

CSCFLAGS = /debug+ /lib:.. /reference:yyDebug.dll ## /main:Arith.Arith
JAYFLAGS = -c -t

CLIX	= clix
CSC	= csc
JAY	= ../../jay/jay.$(OSTYPE) < ../skeleton.cs

all:	recover.exe
clean:			; rm -rf recover.cs recover.ildb y.output y.tab.?
distclean: clean	; rm -f recover.exe
test:	recover.exe
	@ $(ENV); set -x; \
	  { echo opt; echo opt ,; echo opt 1 2; } | $(CLIX) recover.exe; \
	  { echo seq 1; echo seq; echo seq 1,; } | $(CLIX) recover.exe; \
	  { echo list 1,2; echo list; echo list 1,; } | $(CLIX) recover.exe

.SUFFIXES: .exe .cs .jay
.jay.cs:	; $(JAY) $(JAYFLAGS) $*.jay > $*.cs
.cs.exe:	; $(ENV); $(CSC) $(CSCFLAGS) $*.cs
.jay.exe:	; $(JAY) $(JAYFLAGS) $*.jay > $*.cs
		  $(ENV); $(CSC) $(CSCFLAGS) $*.cs
