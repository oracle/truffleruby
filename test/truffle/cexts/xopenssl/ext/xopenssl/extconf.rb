require 'mkmf'

require 'truffle/openssl-prefix'

if ENV['OPENSSL_PREFIX']
  $CFLAGS += " -I #{ENV['OPENSSL_PREFIX']}/include"
  $LIBS += " #{ENV['OPENSSL_PREFIX']}/lib/libssl.#{RbConfig::CONFIG['SOEXT']} #{ENV['OPENSSL_PREFIX']}/lib/libcrypto.#{RbConfig::CONFIG['SOEXT']}"
else
  $LIBS += " -lssl -lcrypto"
end

create_makefile('xopenssl')
