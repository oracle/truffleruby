// From ./tool/generate-cext-constants.rb

VALUE rb_tr_get_undef(void) {
  return RUBY_CEXT_INVOKE("Qundef");
}

VALUE rb_tr_get_true(void) {
  return RUBY_CEXT_INVOKE("Qtrue");
}

VALUE rb_tr_get_false(void) {
  return RUBY_CEXT_INVOKE("Qfalse");
}

VALUE rb_tr_get_nil(void) {
  return RUBY_CEXT_INVOKE("Qnil");
}

VALUE rb_tr_get_Array(void) {
  return RUBY_CEXT_INVOKE("rb_cArray");
}

VALUE rb_tr_get_Class(void) {
  return RUBY_CEXT_INVOKE("rb_cClass");
}

VALUE rb_tr_get_Comparable(void) {
  return RUBY_CEXT_INVOKE("rb_mComparable");
}

VALUE rb_tr_get_Data(void) {
  return RUBY_CEXT_INVOKE("rb_cData");
}

VALUE rb_tr_get_Encoding(void) {
  return RUBY_CEXT_INVOKE("rb_cEncoding");
}

VALUE rb_tr_get_EncodingError(void) {
  return RUBY_CEXT_INVOKE("rb_eEncodingError");
}

VALUE rb_tr_get_Enumerable(void) {
  return RUBY_CEXT_INVOKE("rb_mEnumerable");
}

VALUE rb_tr_get_FalseClass(void) {
  return RUBY_CEXT_INVOKE("rb_cFalseClass");
}

VALUE rb_tr_get_File(void) {
  return RUBY_CEXT_INVOKE("rb_cFile");
}

VALUE rb_tr_get_Float(void) {
  return RUBY_CEXT_INVOKE("rb_cFloat");
}

VALUE rb_tr_get_Hash(void) {
  return RUBY_CEXT_INVOKE("rb_cHash");
}

VALUE rb_tr_get_Integer(void) {
  return RUBY_CEXT_INVOKE("rb_cInteger");
}

VALUE rb_tr_get_IO(void) {
  return RUBY_CEXT_INVOKE("rb_cIO");
}

VALUE rb_tr_get_Kernel(void) {
  return RUBY_CEXT_INVOKE("rb_mKernel");
}

VALUE rb_tr_get_Match(void) {
  return RUBY_CEXT_INVOKE("rb_cMatch");
}

VALUE rb_tr_get_Module(void) {
  return RUBY_CEXT_INVOKE("rb_cModule");
}

VALUE rb_tr_get_NilClass(void) {
  return RUBY_CEXT_INVOKE("rb_cNilClass");
}

VALUE rb_tr_get_Numeric(void) {
  return RUBY_CEXT_INVOKE("rb_cNumeric");
}

VALUE rb_tr_get_Object(void) {
  return RUBY_CEXT_INVOKE("rb_cObject");
}

VALUE rb_tr_get_Range(void) {
  return RUBY_CEXT_INVOKE("rb_cRange");
}

VALUE rb_tr_get_Regexp(void) {
  return RUBY_CEXT_INVOKE("rb_cRegexp");
}

VALUE rb_tr_get_String(void) {
  return RUBY_CEXT_INVOKE("rb_cString");
}

VALUE rb_tr_get_Struct(void) {
  return RUBY_CEXT_INVOKE("rb_cStruct");
}

VALUE rb_tr_get_Symbol(void) {
  return RUBY_CEXT_INVOKE("rb_cSymbol");
}

VALUE rb_tr_get_Time(void) {
  return RUBY_CEXT_INVOKE("rb_cTime");
}

VALUE rb_tr_get_Thread(void) {
  return RUBY_CEXT_INVOKE("rb_cThread");
}

VALUE rb_tr_get_TrueClass(void) {
  return RUBY_CEXT_INVOKE("rb_cTrueClass");
}

VALUE rb_tr_get_Proc(void) {
  return RUBY_CEXT_INVOKE("rb_cProc");
}

VALUE rb_tr_get_Method(void) {
  return RUBY_CEXT_INVOKE("rb_cMethod");
}

VALUE rb_tr_get_Dir(void) {
  return RUBY_CEXT_INVOKE("rb_cDir");
}

VALUE rb_tr_get_ArgError(void) {
  return RUBY_CEXT_INVOKE("rb_eArgError");
}

VALUE rb_tr_get_EOFError(void) {
  return RUBY_CEXT_INVOKE("rb_eEOFError");
}

VALUE rb_tr_get_Errno(void) {
  return RUBY_CEXT_INVOKE("rb_mErrno");
}

VALUE rb_tr_get_Exception(void) {
  return RUBY_CEXT_INVOKE("rb_eException");
}

VALUE rb_tr_get_FloatDomainError(void) {
  return RUBY_CEXT_INVOKE("rb_eFloatDomainError");
}

VALUE rb_tr_get_IndexError(void) {
  return RUBY_CEXT_INVOKE("rb_eIndexError");
}

VALUE rb_tr_get_Interrupt(void) {
  return RUBY_CEXT_INVOKE("rb_eInterrupt");
}

VALUE rb_tr_get_IOError(void) {
  return RUBY_CEXT_INVOKE("rb_eIOError");
}

VALUE rb_tr_get_KeyError(void) {
  return RUBY_CEXT_INVOKE("rb_eKeyError");
}

VALUE rb_tr_get_LoadError(void) {
  return RUBY_CEXT_INVOKE("rb_eLoadError");
}

VALUE rb_tr_get_LocalJumpError(void) {
  return RUBY_CEXT_INVOKE("rb_eLocalJumpError");
}

VALUE rb_tr_get_MathDomainError(void) {
  return RUBY_CEXT_INVOKE("rb_eMathDomainError");
}

VALUE rb_tr_get_EncCompatError(void) {
  return RUBY_CEXT_INVOKE("rb_eEncCompatError");
}

VALUE rb_tr_get_NameError(void) {
  return RUBY_CEXT_INVOKE("rb_eNameError");
}

VALUE rb_tr_get_NoMemError(void) {
  return RUBY_CEXT_INVOKE("rb_eNoMemError");
}

VALUE rb_tr_get_NoMethodError(void) {
  return RUBY_CEXT_INVOKE("rb_eNoMethodError");
}

VALUE rb_tr_get_NotImpError(void) {
  return RUBY_CEXT_INVOKE("rb_eNotImpError");
}

VALUE rb_tr_get_RangeError(void) {
  return RUBY_CEXT_INVOKE("rb_eRangeError");
}

VALUE rb_tr_get_RegexpError(void) {
  return RUBY_CEXT_INVOKE("rb_eRegexpError");
}

VALUE rb_tr_get_RuntimeError(void) {
  return RUBY_CEXT_INVOKE("rb_eRuntimeError");
}

VALUE rb_tr_get_ScriptError(void) {
  return RUBY_CEXT_INVOKE("rb_eScriptError");
}

VALUE rb_tr_get_SecurityError(void) {
  return RUBY_CEXT_INVOKE("rb_eSecurityError");
}

VALUE rb_tr_get_Signal(void) {
  return RUBY_CEXT_INVOKE("rb_eSignal");
}

VALUE rb_tr_get_StandardError(void) {
  return RUBY_CEXT_INVOKE("rb_eStandardError");
}

VALUE rb_tr_get_SyntaxError(void) {
  return RUBY_CEXT_INVOKE("rb_eSyntaxError");
}

VALUE rb_tr_get_SystemCallError(void) {
  return RUBY_CEXT_INVOKE("rb_eSystemCallError");
}

VALUE rb_tr_get_SystemExit(void) {
  return RUBY_CEXT_INVOKE("rb_eSystemExit");
}

VALUE rb_tr_get_SysStackError(void) {
  return RUBY_CEXT_INVOKE("rb_eSysStackError");
}

VALUE rb_tr_get_TypeError(void) {
  return RUBY_CEXT_INVOKE("rb_eTypeError");
}

VALUE rb_tr_get_ThreadError(void) {
  return RUBY_CEXT_INVOKE("rb_eThreadError");
}

VALUE rb_tr_get_WaitReadable(void) {
  return RUBY_CEXT_INVOKE("rb_mWaitReadable");
}

VALUE rb_tr_get_WaitWritable(void) {
  return RUBY_CEXT_INVOKE("rb_mWaitWritable");
}

VALUE rb_tr_get_ZeroDivError(void) {
  return RUBY_CEXT_INVOKE("rb_eZeroDivError");
}

VALUE rb_tr_get_eFatal(void) {
  return RUBY_CEXT_INVOKE("rb_eFatal");
}

VALUE rb_tr_get_stdin(void) {
  return RUBY_CEXT_INVOKE("rb_stdin");
}

VALUE rb_tr_get_stdout(void) {
  return RUBY_CEXT_INVOKE("rb_stdout");
}

VALUE rb_tr_get_stderr(void) {
  return RUBY_CEXT_INVOKE("rb_stderr");
}

VALUE rb_tr_get_output_fs(void) {
  return RUBY_CEXT_INVOKE("rb_output_fs");
}

VALUE rb_tr_get_rs(void) {
  return RUBY_CEXT_INVOKE("rb_rs");
}

VALUE rb_tr_get_output_rs(void) {
  return RUBY_CEXT_INVOKE("rb_output_rs");
}

VALUE rb_tr_get_default_rs(void) {
  return RUBY_CEXT_INVOKE("rb_default_rs");
}
