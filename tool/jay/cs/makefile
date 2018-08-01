SSCLI = /usr/local/sscli#	ROTOR_DIR of MS' shared source CLI

ENV = [ -z "$$ROTOR_DIR" ] && { d=`pwd`; cd $(SSCLI) && . env.sh; cd $$d; }

AL = al
GACUTIL = gacutil
SN = sn

all:	yyDebug.dll
clean:			; rm -f yyDebug.key
distclean: clean	; rm -f yyDebug.key yyDebug.dll yyDebug.netmodule
			- $(ENV); $(GACUTIL) /u yyDebug.dll
test:

.SUFFIXES: .dll .netmodule .cs
.cs.netmodule:	; $(ENV); csc /target:module $*.cs
.netmodule.dll:
	  $(ENV); $(SN) /k $*.key
	  $(ENV); $(AL) /keyfile:$*.key /out:$*.dll $*.netmodule
	- $(ENV); $(GACUTIL) /u $*.dll
	  $(ENV); $(GACUTIL) /i $*.dll
	  rm -f $*.key
.cs.dll:
	  $(ENV); $(ENV); csc /target:module $*.cs
	  $(ENV); $(SN) /k $*.key
	  $(ENV); $(AL) /keyfile:$*.key /out:$*.dll $*.netmodule
	- $(ENV); $(GACUTIL) /u $*.dll
	  $(ENV); $(GACUTIL) /i $*.dll
	  rm -f $*.key
