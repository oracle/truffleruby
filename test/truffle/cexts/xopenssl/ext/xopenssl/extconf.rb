require 'mkmf'

MAC = `uname`.chomp == 'Darwin'

if MAC && !ENV['OPENSSL_PREFIX']
  ENV['OPENSSL_PREFIX'] = '/usr/local/opt/openssl'
end

if ENV['OPENSSL_PREFIX']
  $CFLAGS += " -I #{ENV['OPENSSL_PREFIX']}/include"
  $LIBS += " -l #{ENV['OPENSSL_PREFIX']}/lib/libssl.#{RbConfig::CONFIG['NATIVE_DLEXT']}"
else
  $LIBS += " -l libssl.#{RbConfig::CONFIG['NATIVE_DLEXT']}"
end

create_makefile('xopenssl')
