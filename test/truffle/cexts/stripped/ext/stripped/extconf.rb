require 'mkmf'
create_makefile('stripped')

contents = File.read('Makefile')
contents = "hijack: all strip-ext\n\n" + contents + <<EOF
strip-ext: $(DLLIB)
	$(ECHO) Stripping $(DLLIB)
	#{RbConfig::CONFIG['STRIP']} $(DLLIB)

.PHONY: strip-ext
EOF
File.write('Makefile', contents)
