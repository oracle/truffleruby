/*
 * 'OpenSSL for Ruby' team members
 * Copyright (C) 2003
 * All rights reserved.
 */
/*
 * This program is licensed under the same licence as Ruby.
 * (See the file 'LICENCE'.)
 */
#if !defined(_OSSL_BIO_H_)
#define _OSSL_BIO_H_

// TruffleRuby: _x version added that doesn't take a pointer to a local
// variable - the variable is apparently only to protect against conservative GC
BIO *ossl_obj2bio(volatile VALUE *);
BIO *ossl_obj2bio_x(VALUE);

VALUE ossl_membio2str0(BIO*);
VALUE ossl_membio2str(BIO*);
VALUE ossl_protect_membio2str(BIO*,int*);

#endif
