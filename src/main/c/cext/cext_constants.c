// From tool/generate-cext-constants.rb

VALUE rb_tr_get_undef(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "Qundef");
}

VALUE rb_tr_get_true(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "Qtrue");
}

VALUE rb_tr_get_false(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "Qfalse");
}

VALUE rb_tr_get_nil(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "Qnil");
}

VALUE rb_tr_get_Array(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cArray");
}

VALUE rb_tr_get_Class(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cClass");
}

VALUE rb_tr_get_Comparable(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_mComparable");
}

VALUE rb_tr_get_Data(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cData");
}

VALUE rb_tr_get_Encoding(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cEncoding");
}

VALUE rb_tr_get_Enumerable(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_mEnumerable");
}

VALUE rb_tr_get_FalseClass(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cFalseClass");
}

VALUE rb_tr_get_File(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cFile");
}

VALUE rb_tr_get_Float(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cFloat");
}

VALUE rb_tr_get_Hash(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cHash");
}

VALUE rb_tr_get_Integer(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cInteger");
}

VALUE rb_tr_get_IO(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cIO");
}

VALUE rb_tr_get_Kernel(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_mKernel");
}

VALUE rb_tr_get_Match(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cMatch");
}

VALUE rb_tr_get_Module(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cModule");
}

VALUE rb_tr_get_NilClass(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cNilClass");
}

VALUE rb_tr_get_Numeric(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cNumeric");
}

VALUE rb_tr_get_Object(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cObject");
}

VALUE rb_tr_get_Range(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cRange");
}

VALUE rb_tr_get_Regexp(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cRegexp");
}

VALUE rb_tr_get_String(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cString");
}

VALUE rb_tr_get_Struct(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cStruct");
}

VALUE rb_tr_get_Symbol(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cSymbol");
}

VALUE rb_tr_get_Time(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cTime");
}

VALUE rb_tr_get_Thread(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cThread");
}

VALUE rb_tr_get_TrueClass(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cTrueClass");
}

VALUE rb_tr_get_Proc(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cProc");
}

VALUE rb_tr_get_Method(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cMethod");
}

VALUE rb_tr_get_Dir(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_cDir");
}

VALUE rb_tr_get_ArgError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eArgError");
}

VALUE rb_tr_get_EOFError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eEOFError");
}

VALUE rb_tr_get_Errno(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_mErrno");
}

VALUE rb_tr_get_Exception(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eException");
}

VALUE rb_tr_get_FloatDomainError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eFloatDomainError");
}

VALUE rb_tr_get_IndexError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eIndexError");
}

VALUE rb_tr_get_Interrupt(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eInterrupt");
}

VALUE rb_tr_get_IOError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eIOError");
}

VALUE rb_tr_get_KeyError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eKeyError");
}

VALUE rb_tr_get_LoadError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eLoadError");
}

VALUE rb_tr_get_LocalJumpError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eLocalJumpError");
}

VALUE rb_tr_get_MathDomainError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eMathDomainError");
}

VALUE rb_tr_get_EncCompatError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eEncCompatError");
}

VALUE rb_tr_get_NameError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eNameError");
}

VALUE rb_tr_get_NoMemError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eNoMemError");
}

VALUE rb_tr_get_NoMethodError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eNoMethodError");
}

VALUE rb_tr_get_NotImpError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eNotImpError");
}

VALUE rb_tr_get_RangeError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eRangeError");
}

VALUE rb_tr_get_RegexpError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eRegexpError");
}

VALUE rb_tr_get_RuntimeError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eRuntimeError");
}

VALUE rb_tr_get_ScriptError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eScriptError");
}

VALUE rb_tr_get_SecurityError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eSecurityError");
}

VALUE rb_tr_get_Signal(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eSignal");
}

VALUE rb_tr_get_StandardError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eStandardError");
}

VALUE rb_tr_get_SyntaxError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eSyntaxError");
}

VALUE rb_tr_get_SystemCallError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eSystemCallError");
}

VALUE rb_tr_get_SystemExit(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eSystemExit");
}

VALUE rb_tr_get_SysStackError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eSysStackError");
}

VALUE rb_tr_get_TypeError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eTypeError");
}

VALUE rb_tr_get_ThreadError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eThreadError");
}

VALUE rb_tr_get_WaitReadable(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_mWaitReadable");
}

VALUE rb_tr_get_WaitWritable(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_mWaitWritable");
}

VALUE rb_tr_get_ZeroDivError(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_eZeroDivError");
}

VALUE rb_tr_get_stdin(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_stdin");
}

VALUE rb_tr_get_stdout(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_stdout");
}

VALUE rb_tr_get_stderr(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_stderr");
}

VALUE rb_tr_get_output_fs(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_output_fs");
}

VALUE rb_tr_get_rs(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_rs");
}

VALUE rb_tr_get_output_rs(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_output_rs");
}

VALUE rb_tr_get_default_rs(void) {
  return (VALUE) polyglot_invoke(RUBY_CEXT, "rb_default_rs");
}
