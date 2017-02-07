/*
 * Copyright (c) 2016, 2107 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

#ifndef RUBYSPEC_CAPI_TRUFFLERUBY_H
#undef RUBYSPEC_CAPI_TRUFFLERUBY_H

/* Data */
#undef HAVE_DATA_WRAP_STRUCT
#undef HAVE_RDATA

#undef HAVE_TYPEDDATA_WRAP_STRUCT
#undef HAVE_RTYPEDDATA

/* Encoding */
#undef HAVE_RB_ENCDB_ALIAS
#undef HAVE_RB_ENC_ASSOCIATE
#undef HAVE_RB_ENC_ASSOCIATE_INDEX
#undef HAVE_RB_ENC_COMPATIBLE
#undef HAVE_RB_ENC_COPY
#undef HAVE_RB_ENC_FIND
#undef HAVE_RB_ENC_FIND_INDEX
#undef HAVE_RB_ENC_FROM_ENCODING
#undef HAVE_RB_ENC_FROM_INDEX
#undef HAVE_RB_ENC_STR_CODERANGE
#undef HAVE_RB_ENC_STR_NEW
#undef HAVE_RB_ENC_TO_INDEX
#undef HAVE_RB_OBJ_ENCODING

#undef HAVE_RB_STR_ENCODE
#undef HAVE_RB_USASCII_STR_NEW
#undef HAVE_RB_USASCII_STR_NEW_CSTR
#undef HAVE_RB_EXTERNAL_STR_NEW
#undef HAVE_RB_EXTERNAL_STR_NEW_CSTR
#undef HAVE_RB_EXTERNAL_STR_NEW_WITH_ENC

#undef HAVE_RB_TO_ENCODING
#undef HAVE_RB_TO_ENCODING_INDEX
#undef HAVE_RB_ENC_NTH

#undef HAVE_RSTRING_LENINT
#undef HAVE_TIMET2NUM

#undef HAVE_RB_LONG2INT

#undef HAVE_RB_ITER_BREAK
#undef HAVE_RB_SOURCEFILE
#undef HAVE_RB_SOURCELINE
#undef HAVE_RB_METHOD_BOUNDP

/* Enumerable */
#undef HAVE_RB_ENUMERATORIZE

/* Exception */
#undef HAVE_RB_EXC_NEW
#undef HAVE_RB_EXC_NEW2
#undef HAVE_RB_EXC_NEW3
#undef HAVE_RB_EXC_RAISE
#undef HAVE_RB_SET_ERRINFO

/* File */
#undef HAVE_RB_FILE_OPEN
#undef HAVE_RB_FILE_OPEN_STR
#undef HAVE_FILEPATHVALUE

/* Globals */
#undef HAVE_RB_DEFAULT_RS
#undef HAVE_RB_DEFINE_HOOKED_VARIABLE
#undef HAVE_RB_DEFINE_READONLY_VARIABLE
#undef HAVE_RB_DEFINE_VARIABLE
#undef HAVE_RB_F_GLOBAL_VARIABLES
#undef HAVE_RB_GV_GET
#undef HAVE_RB_GV_SET
#undef HAVE_RB_RS
#undef HAVE_RB_OUTPUT_RS
#undef HAVE_RB_OUTPUT_FS
#undef HAVE_RB_STDERR
#undef HAVE_RB_STDIN
#undef HAVE_RB_STDOUT
#undef HAVE_RB_DEFOUT

#undef HAVE_RB_LASTLINE_SET
#undef HAVE_RB_LASTLINE_GET

/* Integer */
#undef HAVE_RB_INTEGER_PACK

/* Kernel */
#undef HAVE_RB_EVAL_STRING
#undef HAVE_RB_EXEC_RECURSIVE
#undef HAVE_RB_F_SPRINTF
#undef HAVE_RB_NEED_BLOCK
#undef HAVE_RB_SET_END_PROC
#undef HAVE_RB_WARN

#endif
