require 'mkmf'

require 'truffle/openssl-prefix'

if ENV['OPENSSL_PREFIX']
  $CFLAGS += " -I #{ENV['OPENSSL_PREFIX']}/include"
  $LIBS += " -l #{ENV['OPENSSL_PREFIX']}/lib/libssl.#{RbConfig::CONFIG['SOEXT']}"
else
  $LIBS += " -l libssl.#{RbConfig::CONFIG['SOEXT']}"
end

create_makefile('xopenssl')
