// From tool/generate-cext-constants.rb

VALUE rb_tr_get_undef(void);
VALUE rb_tr_get_true(void);
VALUE rb_tr_get_false(void);
VALUE rb_tr_get_nil(void);
VALUE rb_tr_get_Array(void);
VALUE rb_tr_get_Class(void);
VALUE rb_tr_get_Comparable(void);
VALUE rb_tr_get_Data(void);
VALUE rb_tr_get_Encoding(void);
VALUE rb_tr_get_EncodingError(void);
VALUE rb_tr_get_Enumerable(void);
VALUE rb_tr_get_FalseClass(void);
VALUE rb_tr_get_File(void);
VALUE rb_tr_get_Float(void);
VALUE rb_tr_get_Hash(void);
VALUE rb_tr_get_Integer(void);
VALUE rb_tr_get_IO(void);
VALUE rb_tr_get_Kernel(void);
VALUE rb_tr_get_Match(void);
VALUE rb_tr_get_Module(void);
VALUE rb_tr_get_NilClass(void);
VALUE rb_tr_get_Numeric(void);
VALUE rb_tr_get_Object(void);
VALUE rb_tr_get_Range(void);
VALUE rb_tr_get_Regexp(void);
VALUE rb_tr_get_String(void);
VALUE rb_tr_get_Struct(void);
VALUE rb_tr_get_Symbol(void);
VALUE rb_tr_get_Time(void);
VALUE rb_tr_get_Thread(void);
VALUE rb_tr_get_TrueClass(void);
VALUE rb_tr_get_Proc(void);
VALUE rb_tr_get_Method(void);
VALUE rb_tr_get_Dir(void);
VALUE rb_tr_get_ArgError(void);
VALUE rb_tr_get_EOFError(void);
VALUE rb_tr_get_Errno(void);
VALUE rb_tr_get_Exception(void);
VALUE rb_tr_get_FloatDomainError(void);
VALUE rb_tr_get_IndexError(void);
VALUE rb_tr_get_Interrupt(void);
VALUE rb_tr_get_IOError(void);
VALUE rb_tr_get_KeyError(void);
VALUE rb_tr_get_LoadError(void);
VALUE rb_tr_get_LocalJumpError(void);
VALUE rb_tr_get_MathDomainError(void);
VALUE rb_tr_get_EncCompatError(void);
VALUE rb_tr_get_NameError(void);
VALUE rb_tr_get_NoMemError(void);
VALUE rb_tr_get_NoMethodError(void);
VALUE rb_tr_get_NotImpError(void);
VALUE rb_tr_get_RangeError(void);
VALUE rb_tr_get_RegexpError(void);
VALUE rb_tr_get_RuntimeError(void);
VALUE rb_tr_get_ScriptError(void);
VALUE rb_tr_get_SecurityError(void);
VALUE rb_tr_get_Signal(void);
VALUE rb_tr_get_StandardError(void);
VALUE rb_tr_get_SyntaxError(void);
VALUE rb_tr_get_SystemCallError(void);
VALUE rb_tr_get_SystemExit(void);
VALUE rb_tr_get_SysStackError(void);
VALUE rb_tr_get_TypeError(void);
VALUE rb_tr_get_ThreadError(void);
VALUE rb_tr_get_WaitReadable(void);
VALUE rb_tr_get_WaitWritable(void);
VALUE rb_tr_get_ZeroDivError(void);
VALUE rb_tr_get_stdin(void);
VALUE rb_tr_get_stdout(void);
VALUE rb_tr_get_stderr(void);
VALUE rb_tr_get_output_fs(void);
VALUE rb_tr_get_rs(void);
VALUE rb_tr_get_output_rs(void);
VALUE rb_tr_get_default_rs(void);

#define rb_cArray rb_tr_get_Array()
#define rb_cClass rb_tr_get_Class()
#define rb_mComparable rb_tr_get_Comparable()
#define rb_cData rb_tr_get_Data()
#define rb_cEncoding rb_tr_get_Encoding()
#define rb_eEncodingError rb_tr_get_EncodingError()
#define rb_mEnumerable rb_tr_get_Enumerable()
#define rb_cFalseClass rb_tr_get_FalseClass()
#define rb_cFile rb_tr_get_File()
#define rb_cFloat rb_tr_get_Float()
#define rb_cHash rb_tr_get_Hash()
#define rb_cInteger rb_tr_get_Integer()
#define rb_cIO rb_tr_get_IO()
#define rb_mKernel rb_tr_get_Kernel()
#define rb_cMatch rb_tr_get_Match()
#define rb_cModule rb_tr_get_Module()
#define rb_cNilClass rb_tr_get_NilClass()
#define rb_cNumeric rb_tr_get_Numeric()
#define rb_cObject rb_tr_get_Object()
#define rb_cRange rb_tr_get_Range()
#define rb_cRegexp rb_tr_get_Regexp()
#define rb_cString rb_tr_get_String()
#define rb_cStruct rb_tr_get_Struct()
#define rb_cSymbol rb_tr_get_Symbol()
#define rb_cTime rb_tr_get_Time()
#define rb_cThread rb_tr_get_Thread()
#define rb_cTrueClass rb_tr_get_TrueClass()
#define rb_cProc rb_tr_get_Proc()
#define rb_cMethod rb_tr_get_Method()
#define rb_cDir rb_tr_get_Dir()
#define rb_eArgError rb_tr_get_ArgError()
#define rb_eEOFError rb_tr_get_EOFError()
#define rb_mErrno rb_tr_get_Errno()
#define rb_eException rb_tr_get_Exception()
#define rb_eFloatDomainError rb_tr_get_FloatDomainError()
#define rb_eIndexError rb_tr_get_IndexError()
#define rb_eInterrupt rb_tr_get_Interrupt()
#define rb_eIOError rb_tr_get_IOError()
#define rb_eKeyError rb_tr_get_KeyError()
#define rb_eLoadError rb_tr_get_LoadError()
#define rb_eLocalJumpError rb_tr_get_LocalJumpError()
#define rb_eMathDomainError rb_tr_get_MathDomainError()
#define rb_eEncCompatError rb_tr_get_EncCompatError()
#define rb_eNameError rb_tr_get_NameError()
#define rb_eNoMemError rb_tr_get_NoMemError()
#define rb_eNoMethodError rb_tr_get_NoMethodError()
#define rb_eNotImpError rb_tr_get_NotImpError()
#define rb_eRangeError rb_tr_get_RangeError()
#define rb_eRegexpError rb_tr_get_RegexpError()
#define rb_eRuntimeError rb_tr_get_RuntimeError()
#define rb_eScriptError rb_tr_get_ScriptError()
#define rb_eSecurityError rb_tr_get_SecurityError()
#define rb_eSignal rb_tr_get_Signal()
#define rb_eStandardError rb_tr_get_StandardError()
#define rb_eSyntaxError rb_tr_get_SyntaxError()
#define rb_eSystemCallError rb_tr_get_SystemCallError()
#define rb_eSystemExit rb_tr_get_SystemExit()
#define rb_eSysStackError rb_tr_get_SysStackError()
#define rb_eTypeError rb_tr_get_TypeError()
#define rb_eThreadError rb_tr_get_ThreadError()
#define rb_mWaitReadable rb_tr_get_WaitReadable()
#define rb_mWaitWritable rb_tr_get_WaitWritable()
#define rb_eZeroDivError rb_tr_get_ZeroDivError()
#define rb_stdin rb_tr_get_stdin()
#define rb_stdout rb_tr_get_stdout()
#define rb_stderr rb_tr_get_stderr()
#define rb_output_fs rb_tr_get_output_fs()
#define rb_rs rb_tr_get_rs()
#define rb_output_rs rb_tr_get_output_rs()
#define rb_default_rs rb_tr_get_default_rs()
