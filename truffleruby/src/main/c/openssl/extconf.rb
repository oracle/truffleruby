require 'mkmf'
have = %w{
  OPENSSL_110_THREADING_API
  HMAC_CTX_COPY
  EVP_CIPHER_CTX_COPY
  BN_RAND_RANGE
  BN_PSEUDO_RAND_RANGE
  X509V3_EXT_NCONF_NID
  OBJ_NAME_DO_ALL_SORTED
}
$CFLAGS += " -I #{ENV['OPENSSL_INCLUDE']} #{have.map { |h| "-DHAVE_#{h}" }.join(' ')}"
$LIBS += " -l #{ENV['OPENSSL_LIB']}"
create_makefile('openssl')
