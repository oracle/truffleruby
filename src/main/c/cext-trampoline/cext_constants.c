/*
 * Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

// From tool/generate-cext-constants.rb

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
VALUE rb_argv0;

void rb_tr_trampoline_init_global_constants(VALUE (*get_constant)(const char*)) {
  rb_cArray = get_constant("rb_cArray");
  rb_cBasicObject = get_constant("rb_cBasicObject");
  rb_cBinding = get_constant("rb_cBinding");
  rb_cClass = get_constant("rb_cClass");
  rb_mComparable = get_constant("rb_mComparable");
  rb_cComplex = get_constant("rb_cComplex");
  rb_cDir = get_constant("rb_cDir");
  rb_cEncoding = get_constant("rb_cEncoding");
  rb_mEnumerable = get_constant("rb_mEnumerable");
  rb_cEnumerator = get_constant("rb_cEnumerator");
  rb_cFalseClass = get_constant("rb_cFalseClass");
  rb_cFile = get_constant("rb_cFile");
  rb_mFileTest = get_constant("rb_mFileTest");
  rb_cStat = get_constant("rb_cStat");
  rb_cFloat = get_constant("rb_cFloat");
  rb_mGC = get_constant("rb_mGC");
  rb_cHash = get_constant("rb_cHash");
  rb_cInteger = get_constant("rb_cInteger");
  rb_cIO = get_constant("rb_cIO");
  rb_mKernel = get_constant("rb_mKernel");
  rb_cMatch = get_constant("rb_cMatch");
  rb_mMath = get_constant("rb_mMath");
  rb_cMethod = get_constant("rb_cMethod");
  rb_cModule = get_constant("rb_cModule");
  rb_cNilClass = get_constant("rb_cNilClass");
  rb_cNumeric = get_constant("rb_cNumeric");
  rb_cObject = get_constant("rb_cObject");
  rb_cProc = get_constant("rb_cProc");
  rb_mProcess = get_constant("rb_mProcess");
  rb_cRandom = get_constant("rb_cRandom");
  rb_cRange = get_constant("rb_cRange");
  rb_cRational = get_constant("rb_cRational");
  rb_cRegexp = get_constant("rb_cRegexp");
  rb_cString = get_constant("rb_cString");
  rb_cStruct = get_constant("rb_cStruct");
  rb_cSymbol = get_constant("rb_cSymbol");
  rb_cTime = get_constant("rb_cTime");
  rb_cThread = get_constant("rb_cThread");
  rb_cTrueClass = get_constant("rb_cTrueClass");
  rb_cUnboundMethod = get_constant("rb_cUnboundMethod");
  rb_eArgError = get_constant("rb_eArgError");
  rb_eEncodingError = get_constant("rb_eEncodingError");
  rb_eEOFError = get_constant("rb_eEOFError");
  rb_mErrno = get_constant("rb_mErrno");
  rb_eException = get_constant("rb_eException");
  rb_eFloatDomainError = get_constant("rb_eFloatDomainError");
  rb_eFrozenError = get_constant("rb_eFrozenError");
  rb_eIndexError = get_constant("rb_eIndexError");
  rb_eInterrupt = get_constant("rb_eInterrupt");
  rb_eIOError = get_constant("rb_eIOError");
  rb_eKeyError = get_constant("rb_eKeyError");
  rb_eLoadError = get_constant("rb_eLoadError");
  rb_eLocalJumpError = get_constant("rb_eLocalJumpError");
  rb_eMathDomainError = get_constant("rb_eMathDomainError");
  rb_eEncCompatError = get_constant("rb_eEncCompatError");
  rb_eNameError = get_constant("rb_eNameError");
  rb_eNoMemError = get_constant("rb_eNoMemError");
  rb_eNoMethodError = get_constant("rb_eNoMethodError");
  rb_eNotImpError = get_constant("rb_eNotImpError");
  rb_eRangeError = get_constant("rb_eRangeError");
  rb_eRegexpError = get_constant("rb_eRegexpError");
  rb_eRuntimeError = get_constant("rb_eRuntimeError");
  rb_eScriptError = get_constant("rb_eScriptError");
  rb_eSecurityError = get_constant("rb_eSecurityError");
  rb_eSignal = get_constant("rb_eSignal");
  rb_eStandardError = get_constant("rb_eStandardError");
  rb_eStopIteration = get_constant("rb_eStopIteration");
  rb_eSyntaxError = get_constant("rb_eSyntaxError");
  rb_eSystemCallError = get_constant("rb_eSystemCallError");
  rb_eSystemExit = get_constant("rb_eSystemExit");
  rb_eSysStackError = get_constant("rb_eSysStackError");
  rb_eTypeError = get_constant("rb_eTypeError");
  rb_eThreadError = get_constant("rb_eThreadError");
  rb_mWaitReadable = get_constant("rb_mWaitReadable");
  rb_mWaitWritable = get_constant("rb_mWaitWritable");
  rb_eZeroDivError = get_constant("rb_eZeroDivError");
  rb_eFatal = get_constant("rb_eFatal");
  rb_argv0 = get_constant("rb_argv0");
}
