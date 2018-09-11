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

#ifdef TRUFFLERUBY

/*
 * This is ossl_obj2bio copied from ossl_bio.c with the only modification being
 * the addition of MUST_INLINE, so that the local variable referenced by pobj
 * can be put into a registser, and the removal of the volatile, which would
 * prevent that. I think MRI uses the volatile and the address just in order
 * to support conservative GC. We don't need this as our GC is precise.
 */

MUST_INLINE
BIO *
ossl_obj2bio(VALUE *pobj)
{
    VALUE obj = *pobj;
    BIO *bio;

    if (RB_TYPE_P(obj, T_FILE))
	obj = rb_funcallv(obj, rb_intern("read"), 0, NULL);
    StringValue(obj);
    bio = BIO_new_mem_buf(RSTRING_PTR(obj), RSTRING_LENINT(obj));
    if (!bio)
	ossl_raise(eOSSLError, "BIO_new_mem_buf");
    *pobj = obj;
    return bio;
}

#else

BIO *ossl_obj2bio(volatile VALUE *);

#endif

VALUE ossl_membio2str0(BIO*);
VALUE ossl_membio2str(BIO*);
VALUE ossl_protect_membio2str(BIO*,int*);

#endif
