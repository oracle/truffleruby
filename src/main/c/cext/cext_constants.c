/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

// From ./tool/generate-cext-constants.rb

#include <ruby.h>

VALUE rb_cArray;
VALUE rb_cBasicObject;
VALUE rb_cBinding;
VALUE rb_cClass;
VALUE rb_mComparable;
VALUE rb_cComplex;
VALUE rb_cDir;
VALUE rb_cEncoding;
VALUE rb_mEnumerable;
VALUE rb_cEnumerator;
VALUE rb_cFalseClass;
VALUE rb_cFile;
VALUE rb_mFileTest;
VALUE rb_cStat;
VALUE rb_cFloat;
VALUE rb_mGC;
VALUE rb_cHash;
VALUE rb_cInteger;
VALUE rb_cIO;
VALUE rb_mKernel;
VALUE rb_cMatch;
VALUE rb_mMath;
VALUE rb_cMethod;
VALUE rb_cModule;
VALUE rb_cNilClass;
VALUE rb_cNumeric;
VALUE rb_cObject;
VALUE rb_cProc;
VALUE rb_mProcess;
VALUE rb_cRandom;
VALUE rb_cRange;
VALUE rb_cRational;
VALUE rb_cRegexp;
VALUE rb_cString;
VALUE rb_cStruct;
VALUE rb_cSymbol;
VALUE rb_cTime;
VALUE rb_cThread;
VALUE rb_cTrueClass;
VALUE rb_cUnboundMethod;
VALUE rb_eArgError;
VALUE rb_eEncodingError;
VALUE rb_eEOFError;
VALUE rb_mErrno;
VALUE rb_eException;
VALUE rb_eFloatDomainError;
VALUE rb_eFrozenError;
VALUE rb_eIndexError;
VALUE rb_eInterrupt;
VALUE rb_eIOError;
VALUE rb_eKeyError;
VALUE rb_eLoadError;
VALUE rb_eLocalJumpError;
VALUE rb_eMathDomainError;
VALUE rb_eEncCompatError;
VALUE rb_eNameError;
VALUE rb_eNoMemError;
VALUE rb_eNoMethodError;
VALUE rb_eNotImpError;
VALUE rb_eRangeError;
VALUE rb_eRegexpError;
VALUE rb_eRuntimeError;
VALUE rb_eScriptError;
VALUE rb_eSecurityError;
VALUE rb_eSignal;
VALUE rb_eStandardError;
VALUE rb_eStopIteration;
VALUE rb_eSyntaxError;
VALUE rb_eSystemCallError;
VALUE rb_eSystemExit;
VALUE rb_eSysStackError;
VALUE rb_eTypeError;
VALUE rb_eThreadError;
VALUE rb_mWaitReadable;
VALUE rb_mWaitWritable;
VALUE rb_eZeroDivError;
VALUE rb_eFatal;

void rb_tr_init_global_constants(void) {
  rb_cArray = RUBY_CEXT_INVOKE("rb_cArray");
  rb_cBasicObject = RUBY_CEXT_INVOKE("rb_cBasicObject");
  rb_cBinding = RUBY_CEXT_INVOKE("rb_cBinding");
  rb_cClass = RUBY_CEXT_INVOKE("rb_cClass");
  rb_mComparable = RUBY_CEXT_INVOKE("rb_mComparable");
  rb_cComplex = RUBY_CEXT_INVOKE("rb_cComplex");
  rb_cDir = RUBY_CEXT_INVOKE("rb_cDir");
  rb_cEncoding = RUBY_CEXT_INVOKE("rb_cEncoding");
  rb_mEnumerable = RUBY_CEXT_INVOKE("rb_mEnumerable");
  rb_cEnumerator = RUBY_CEXT_INVOKE("rb_cEnumerator");
  rb_cFalseClass = RUBY_CEXT_INVOKE("rb_cFalseClass");
  rb_cFile = RUBY_CEXT_INVOKE("rb_cFile");
  rb_mFileTest = RUBY_CEXT_INVOKE("rb_mFileTest");
  rb_cStat = RUBY_CEXT_INVOKE("rb_cStat");
  rb_cFloat = RUBY_CEXT_INVOKE("rb_cFloat");
  rb_mGC = RUBY_CEXT_INVOKE("rb_mGC");
  rb_cHash = RUBY_CEXT_INVOKE("rb_cHash");
  rb_cInteger = RUBY_CEXT_INVOKE("rb_cInteger");
  rb_cIO = RUBY_CEXT_INVOKE("rb_cIO");
  rb_mKernel = RUBY_CEXT_INVOKE("rb_mKernel");
  rb_cMatch = RUBY_CEXT_INVOKE("rb_cMatch");
  rb_mMath = RUBY_CEXT_INVOKE("rb_mMath");
  rb_cMethod = RUBY_CEXT_INVOKE("rb_cMethod");
  rb_cModule = RUBY_CEXT_INVOKE("rb_cModule");
  rb_cNilClass = RUBY_CEXT_INVOKE("rb_cNilClass");
  rb_cNumeric = RUBY_CEXT_INVOKE("rb_cNumeric");
  rb_cObject = RUBY_CEXT_INVOKE("rb_cObject");
  rb_cProc = RUBY_CEXT_INVOKE("rb_cProc");
  rb_mProcess = RUBY_CEXT_INVOKE("rb_mProcess");
  rb_cRandom = RUBY_CEXT_INVOKE("rb_cRandom");
  rb_cRange = RUBY_CEXT_INVOKE("rb_cRange");
  rb_cRational = RUBY_CEXT_INVOKE("rb_cRational");
  rb_cRegexp = RUBY_CEXT_INVOKE("rb_cRegexp");
  rb_cString = RUBY_CEXT_INVOKE("rb_cString");
  rb_cStruct = RUBY_CEXT_INVOKE("rb_cStruct");
  rb_cSymbol = RUBY_CEXT_INVOKE("rb_cSymbol");
  rb_cTime = RUBY_CEXT_INVOKE("rb_cTime");
  rb_cThread = RUBY_CEXT_INVOKE("rb_cThread");
  rb_cTrueClass = RUBY_CEXT_INVOKE("rb_cTrueClass");
  rb_cUnboundMethod = RUBY_CEXT_INVOKE("rb_cUnboundMethod");
  rb_eArgError = RUBY_CEXT_INVOKE("rb_eArgError");
  rb_eEncodingError = RUBY_CEXT_INVOKE("rb_eEncodingError");
  rb_eEOFError = RUBY_CEXT_INVOKE("rb_eEOFError");
  rb_mErrno = RUBY_CEXT_INVOKE("rb_mErrno");
  rb_eException = RUBY_CEXT_INVOKE("rb_eException");
  rb_eFloatDomainError = RUBY_CEXT_INVOKE("rb_eFloatDomainError");
  rb_eFrozenError = RUBY_CEXT_INVOKE("rb_eFrozenError");
  rb_eIndexError = RUBY_CEXT_INVOKE("rb_eIndexError");
  rb_eInterrupt = RUBY_CEXT_INVOKE("rb_eInterrupt");
  rb_eIOError = RUBY_CEXT_INVOKE("rb_eIOError");
  rb_eKeyError = RUBY_CEXT_INVOKE("rb_eKeyError");
  rb_eLoadError = RUBY_CEXT_INVOKE("rb_eLoadError");
  rb_eLocalJumpError = RUBY_CEXT_INVOKE("rb_eLocalJumpError");
  rb_eMathDomainError = RUBY_CEXT_INVOKE("rb_eMathDomainError");
  rb_eEncCompatError = RUBY_CEXT_INVOKE("rb_eEncCompatError");
  rb_eNameError = RUBY_CEXT_INVOKE("rb_eNameError");
  rb_eNoMemError = RUBY_CEXT_INVOKE("rb_eNoMemError");
  rb_eNoMethodError = RUBY_CEXT_INVOKE("rb_eNoMethodError");
  rb_eNotImpError = RUBY_CEXT_INVOKE("rb_eNotImpError");
  rb_eRangeError = RUBY_CEXT_INVOKE("rb_eRangeError");
  rb_eRegexpError = RUBY_CEXT_INVOKE("rb_eRegexpError");
  rb_eRuntimeError = RUBY_CEXT_INVOKE("rb_eRuntimeError");
  rb_eScriptError = RUBY_CEXT_INVOKE("rb_eScriptError");
  rb_eSecurityError = RUBY_CEXT_INVOKE("rb_eSecurityError");
  rb_eSignal = RUBY_CEXT_INVOKE("rb_eSignal");
  rb_eStandardError = RUBY_CEXT_INVOKE("rb_eStandardError");
  rb_eStopIteration = RUBY_CEXT_INVOKE("rb_eStopIteration");
  rb_eSyntaxError = RUBY_CEXT_INVOKE("rb_eSyntaxError");
  rb_eSystemCallError = RUBY_CEXT_INVOKE("rb_eSystemCallError");
  rb_eSystemExit = RUBY_CEXT_INVOKE("rb_eSystemExit");
  rb_eSysStackError = RUBY_CEXT_INVOKE("rb_eSysStackError");
  rb_eTypeError = RUBY_CEXT_INVOKE("rb_eTypeError");
  rb_eThreadError = RUBY_CEXT_INVOKE("rb_eThreadError");
  rb_mWaitReadable = RUBY_CEXT_INVOKE("rb_mWaitReadable");
  rb_mWaitWritable = RUBY_CEXT_INVOKE("rb_mWaitWritable");
  rb_eZeroDivError = RUBY_CEXT_INVOKE("rb_eZeroDivError");
  rb_eFatal = RUBY_CEXT_INVOKE("rb_eFatal");
}
