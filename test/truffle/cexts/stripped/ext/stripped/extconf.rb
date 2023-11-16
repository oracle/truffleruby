require 'mkmf'

# This test tests stripping like grpc does it:
# https://github.com/grpc/grpc/blob/54f65e0dbd2151a3ba2ad364327c0c31b200a5ae/src/ruby/ext/grpc/extconf.rb#L125-L126
strip = RbConfig::CONFIG['STRIP']
if RUBY_PLATFORM.include? 'darwin'
  # This is necessary on macOS, otherwise it fails like:
  # .../strip: error: symbols referenced by indirect symbol table entries that can't be stripped in: .../stripped.bundle
  # _rb_define_module
  # _rb_define_singleton_method
  # _rb_str_new_static
  # dyld_stub_binder
  #
  # on both CRuby and TruffleRuby.
  strip += ' -x'
end

create_makefile('stripped')

contents = File.read('Makefile')
contents = "hijack: all strip-ext\n\n" + contents + <<EOF
strip-ext: $(DLLIB)
\t$(ECHO) Stripping $(DLLIB)
\t#{strip} $(DLLIB)

.PHONY: strip-ext
EOF
File.write('Makefile', contents)
