# jay

d	= src conflict yydebug java # cs
dist	= doc-files/jay-mosx.tgz doc-files/src.zip \
		doc-files/jay-win.zip

dist::	doc-files/src.zip

doc-files/src.zip:
	$(MAKE) distclean
#	cd .. && find jay -type f -not -name .DS_Store | \
#	  grep -v 'src/jay.*\.exe' | zip -@9 jay/$@

all clean dist distclean test::
	for d in $d; do \
	  echo; echo ' ----- jay/'$$d; echo; \
	  ( cd $$d && $(MAKE) $@ ) done

clean::			; rm -f $(dist)
dist::	doc-files/jay-mosx.tgz doc-files/jay-win.zip
distclean::		; rm -f $(dist)

doc-files/jay-mosx.tgz: cs/skeleton.cs cs/yyDebug.cs \
		java/skeleton.* src/jay yydebug/yydebug.jar
	cd src && tar cfv ../$(@:.tgz=.tar) jay 
	tar rfv $(@:.tgz=.tar) cs/skeleton.cs cs/yyDebug.cs \
		java/skeleton.*
	cd yydebug && tar rfv ../$(@:.tgz=.tar) yydebug.jar
	gzip -c9 < $(@:.tgz=.tar) > $@
	rm $(@:.tgz=.tar)

doc-files/jay-win.zip: cs/skeleton.cs cs/yyDebug.cs \
		java/skeleton.* src/jay*.exe yydebug/yydebug.jar
	rm -f $@
	cd src && zip -9 ../$@ jay*.exe
	zip -9 $@ cs/skeleton.cs cs/yyDebug.cs java/skeleton.*
	cd yydebug && zip -9 ../$@ yydebug.jar
