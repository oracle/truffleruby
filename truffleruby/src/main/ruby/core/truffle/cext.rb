# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# TODO CS 3-Feb-17 only load this when people use cexts

class Data

end

module Truffle::CExt
  class RData

    DATA_FIELD_INDEX = 2

    def initialize(object)
      @object = object
    end

    def [](index)
      raise unless index == DATA_FIELD_INDEX
      @object.instance_variable_get(:@data)
    end

    def []=(index, value)
      raise unless index == DATA_FIELD_INDEX
      @object.instance_variable_set :@data, value
    end

  end

  class RbEncoding

    NAME_FIELD_INDEX = 0

    attr_reader :encoding

    def initialize(encoding)
      @encoding = encoding
    end

    def [](index)
      raise unless index == NAME_FIELD_INDEX
      @encoding.name
    end

  end

  class RStringPtr

    attr_reader :string

    def initialize(string)
      @string = string
    end

    def size
      Truffle::CExt.string_pointer_size(@string)
    end

    def unbox
      Truffle::CExt.string_pointer_unbox(@string)
    end

    def [](index)
      Truffle::CExt.string_pointer_read(@string, index)
    end

    def []=(index, value)
      Truffle::CExt.string_pointer_write @string, index, value
    end

    alias_method :to_str, :string
    alias_method :to_s, :string

  end
end

class << Truffle::CExt

  T_NONE     = 0x00

  T_OBJECT   = 0x01
  T_CLASS    = 0x02
  T_MODULE   = 0x03
  T_FLOAT    = 0x04
  T_STRING   = 0x05
  T_REGEXP   = 0x06
  T_ARRAY    = 0x07
  T_HASH     = 0x08
  T_STRUCT   = 0x09
  T_BIGNUM   = 0x0a
  T_FILE     = 0x0b
  T_DATA     = 0x0c
  T_MATCH    = 0x0d
  T_COMPLEX  = 0x0e
  T_RATIONAL = 0x0f

  T_NIL      = 0x11
  T_TRUE     = 0x12
  T_FALSE    = 0x13
  T_SYMBOL   = 0x14
  T_FIXNUM   = 0x15
  T_UNDEF    = 0x16

  T_IMEMO    = 0x1a
  T_NODE     = 0x1b
  T_ICLASS   = 0x1c
  T_ZOMBIE   = 0x1d

  T_MASK     = 0x1f

  RUBY_ENC_CODERANGE_UNKNOWN = 0
  RUBY_ENC_CODERANGE_7BIT = 1
  RUBY_ENC_CODERANGE_VALID = 2
  RUBY_ENC_CODERANGE_BROKEN = 4

  def supported?
    Interop.mime_type_supported?('application/x-sulong-library')
  end

  def rb_type(value)
    # TODO CS 23-Jul-16 we could do with making this a kind of specialising case
    # that puts never seen cases behind a transfer

    case value
    when Class
      T_CLASS
    when Module
      T_MODULE
    when Float
      T_FLOAT
    when String
      T_STRING
    when Regexp
      T_REGEXP
    when Array
      T_ARRAY
    when Hash
      T_HASH
    when Struct
      T_STRUCT
    when Bignum
      T_BIGNUM
    when File
      T_FILE
    when Complex
      T_COMPLEX
    when Rational
      T_RATIONAL
    when NilClass
      T_NIL
    when TrueClass
      T_TRUE
    when FalseClass
      T_FALSE
    when Symbol
      T_SYMBOL
    when Fixnum
      T_FIXNUM
    when Time
      T_DATA
    when Data
      T_DATA
    when Object
      T_OBJECT
    else
      raise "unknown type #{value.class}"
    end
  end

  def RB_TYPE_P(value, type)
    # TODO CS 23-Jul-16 we could do with making this a kind of specialising case
    # that puts never seen cases behind a transfer

    case type
    when T_SYMBOL
      value.is_a?(Symbol)
    when T_STRING
      value.is_a?(String)
    when T_FIXNUM
      value.is_a?(Fixnum)
    when T_BIGNUM
      value.is_a?(Bignum)
    when T_ARRAY
      value.is_a?(Array)
    when T_FILE
      value.is_a?(File)
    else
      raise "unknown type #{type}"
    end
  end

  def rb_check_type(value, type)
    # TODO CS 23-Jul-16 there's more to this method than this...
    if rb_type(value) != type
      raise 'unexpected type'
    end
  end

  def rb_method_boundp(klass, id, ex)
    if ex == 0
      (klass.method_defined?(id) || klass.private_method_defined?(id) || klass.protected_method_defined?(id)) ? 1 : 0
    else
      klass.method_defined?(id) ? 1 : 0
    end
  end

  def rb_obj_is_instance_of(object, ruby_class)
    object.class == ruby_class
  end

  def SYMBOL_P(value)
    value.is_a?(Symbol)
  end

  # START from tool/generate-cext-constants.rb

  def Qundef
    Rubinius::UNDEFINED
  end

  def Qtrue
    true
  end

  def Qfalse
    false
  end

  def Qnil
    nil
  end

  def rb_cArray
    Array
  end

  def rb_cBignum
    Bignum
  end

  def rb_cClass
    Class
  end

  def rb_mComparable
    Comparable
  end

  def rb_cData
    Data
  end

  def rb_cEncoding
    Encoding
  end

  def rb_mEnumerable
    Enumerable
  end

  def rb_cFalseClass
    FalseClass
  end

  def rb_cFile
    File
  end

  def rb_cFixnum
    Fixnum
  end

  def rb_cFloat
    Float
  end

  def rb_cHash
    Hash
  end

  def rb_cInteger
    Integer
  end

  def rb_cIO
    IO
  end

  def rb_mKernel
    Kernel
  end

  def rb_cMatch
    MatchData
  end

  def rb_cModule
    Module
  end

  def rb_cNilClass
    NilClass
  end

  def rb_cNumeric
    Numeric
  end

  def rb_cObject
    Object
  end

  def rb_cRange
    Range
  end

  def rb_cRegexp
    Regexp
  end

  def rb_cString
    String
  end

  def rb_cStruct
    Struct
  end

  def rb_cSymbol
    Symbol
  end

  def rb_cTime
    Time
  end

  def rb_cThread
    Thread
  end

  def rb_cTrueClass
    TrueClass
  end

  def rb_cProc
    Proc
  end

  def rb_cMethod
    Method
  end

  def rb_cDir
    Dir
  end

  def rb_eArgError
    ArgumentError
  end

  def rb_eEOFError
    EOFError
  end

  def rb_mErrno
    Errno
  end

  def rb_eException
    Exception
  end

  def rb_eFloatDomainError
    FloatDomainError
  end

  def rb_eIndexError
    IndexError
  end

  def rb_eInterrupt
    Interrupt
  end

  def rb_eIOError
    IOError
  end

  def rb_eLoadError
    LoadError
  end

  def rb_eLocalJumpError
    LocalJumpError
  end

  def rb_eMathDomainError
    Math::DomainError
  end

  def rb_eEncCompatError
    Encoding::CompatibilityError
  end

  def rb_eNameError
    NameError
  end

  def rb_eNoMemError
    NoMemoryError
  end

  def rb_eNoMethodError
    NoMethodError
  end

  def rb_eNotImpError
    NotImplementedError
  end

  def rb_eRangeError
    RangeError
  end

  def rb_eRegexpError
    RegexpError
  end

  def rb_eRuntimeError
    RuntimeError
  end

  def rb_eScriptError
    ScriptError
  end

  def rb_eSecurityError
    SecurityError
  end

  def rb_eSignal
    SignalException
  end

  def rb_eStandardError
    StandardError
  end

  def rb_eSyntaxError
    SyntaxError
  end

  def rb_eSystemCallError
    SystemCallError
  end

  def rb_eSystemExit
    SystemExit
  end

  def rb_eSysStackError
    SystemStackError
  end

  def rb_eTypeError
    TypeError
  end

  def rb_eThreadError
    ThreadError
  end

  def rb_mWaitReadable
    IO::WaitReadable
  end

  def rb_mWaitWritable
    IO::WaitWritable
  end

  def rb_eZeroDivError
    ZeroDivisionError
  end

  def rb_stdin
    $stdin
  end

  def rb_stdout
    $stdout
  end

  def rb_stderr
    $stderr
  end

  def rb_output_fs
    $,
  end

  def rb_rs
    $/
  end

  def rb_output_rs
    $\
  end

  def rb_default_rs
    "\n"
  end

  # END from tool/generate-cext-constants.rb

  def rb_to_int(val)
    Rubinius::Type.rb_to_int(val)
  end

  def rb_fix2int(value)
    if value.nil?
      raise TypeError
    else
      int = value.to_int
      raise RangeError if int >= 2**32
      int
    end
  end

  def rb_fix2uint(value)
    if value.nil?
      raise TypeError
    else
      int = value.to_int
      raise RangeError if int >= 2**32
      int
    end
  end

  def RB_NIL_P(value)
    nil.equal?(value)
  end

  def RB_FIXNUM_P(value)
    value.is_a?(Fixnum)
  end

  def RTEST(value)
    !nil.equal?(value) && !false.equal?(value)
  end

  def rb_require(feature)
    require feature
  end

  def RB_OBJ_TAINTABLE(object)
    case object
    when TrueClass, FalseClass, Fixnum, Float, NilClass, Symbol
      true
    else
      false
    end
  end

  def rb_tr_obj_infect(dest, source)
    Rubinius::Type.infect(dest, source)
  end

  def rb_float_new(value)
    value.to_f
  end

  def rb_num2int(val)
    Rubinius::Type.rb_num2int(val)
  end

  def rb_num2long(val)
    Rubinius::Type.rb_num2long(val)
  end

  def rb_big2dbl(val)
    Rubinius::Type.rb_big2dbl(val)
  end

  def rb_big2long(val)
    Rubinius::Type.rb_big2long(val)
  end

  def rb_big2ulong(val)
    Rubinius::Type.rb_big2long(val)
  end

  def rb_dbl2big(val)
    val.to_i
  end


  def rb_num_coerce_bin(x, y, func)
    a, b = do_coerce(x, y, true)
    a.send(func, b)
  end

  def rb_num_coerce_cmp(x, y, func)
    ary = do_coerce(x, y, false)
    if ary.nil?
      nil
    else
      ary[0].send(func, ary[1])
    end
  end

  def rb_num_coerce_relop(x, y, func)
    ary = do_coerce(x, y, false)
    unless ary.nil?
      res = ary[0].send(func, ary[1])
    end
    raise ArgumentError, "comparison of #{x.class} with #{y.class} failed" if res.nil?
    res
  end

  private def do_coerce(x, y, raise_error)
    unless y.respond_to?(:coerce)
      if raise_error
        raise TypeError, "#{y.class} can't be coerced to #{x.class}"
      else
        return nil
      end
    end

    ary = begin
      y.coerce(x)
    rescue
      if raise_error
        raise TypeError, "#{y.class} can't be coerced to #{x.class}"
      else
        warn 'Numerical comparison operators will no more rescue exceptions of #coerce'
        warn 'in the next release. Return nil in #coerce if the coercion is impossible.'
      end
      return nil
    end

    if !ary.is_a?(Array) || ary.size != 2
      if raise_error
        raise TypeError, 'coerce must return [x, y]'
      else
        warn 'Numerical comparison operators will no more rescue exceptions of #coerce'
        warn 'in the next release. Return nil in #coerce if the coercion is impossible.'
      end
      return nil
    end
    ary
  end

  def rb_num2uint(val)
    Rubinius::Type.rb_num2uint(val)
  end

  def rb_num2ulong(val)
    # We're going to create a signed long here, and rely on the C to
    # cast it to an unsigned one.
    Rubinius::Type.rb_num2ulong(val)
  end

  def rb_num2dbl(val)
    Rubinius::Type.rb_num2dbl(val)
  end

  def rb_Integer(value)
    Integer(value)
  end

  def rb_Float(value)
    Float(value)
  end

  def RFLOAT_VALUE(value)
    value
  end

  def rb_obj_classname(object)
    object.class.name
  end

  def rb_class_of(object)
    object.class
  end

  def rb_class_real(ruby_class)
    if ruby_object?(ruby_class)
      while ruby_class.singleton_class?
        ruby_class = ruby_class.superclass
      end
    end

    ruby_class
  end

  def rb_obj_respond_to(object, id, priv)
    Rubinius::Type.object_respond_to?(object, id, priv != 0)
  end

  def rb_check_convert_type(obj, type_name, method)
    Rubinius::Type.rb_check_convert_type(obj, Object.const_get(type_name), method.to_sym)
  end

  def rb_convert_type(obj, type_name, method)
    Rubinius::Type.rb_convert_type(obj, Object.const_get(type_name), method.to_sym)
  end

  def rb_check_to_integer(obj, method)
    Rubinius::Type.rb_check_to_integer(obj, method.to_sym)
  end

  def rb_obj_method_arity(object, id)
    object.method(id).arity
  end

  def rb_ivar_defined(object, id)
    object.instance_variable_defined?(id)
  end

  def rb_f_global_variables
    Kernel.global_variables
  end

  def rb_obj_instance_variables(object)
    object.instance_variables
  end

  def rb_inspect(object)
    Rubinius::Type.rb_inspect(object)
  end

  def rb_range_new(beg, last, exclude_end)
    Range.new(beg, last, exclude_end != 0)
  end

  def rb_reg_new(java_string, options)
    Regexp.new(String.new(java_string), options)
  end

  def rb_reg_new_str(str, options)
    Regexp.new(str, options)
  end

  def rb_marshal_dump(obj, port)
    Marshal.dump(obj, port)
  end

  def rb_marshal_load(port)
    Marshal.load(port)
  end

  def rb_reg_regcomp(str)
    Regexp.compile(str)
  end

  def rb_reg_match_pre(match)
    match.pre_match
  end

  def rb_reg_nth_match(nth, match)
    return nil if match.nil?
    match[nth]
  end

  def rb_reg_options(re)
    re.options
  end

  def ascii8bit_encoding
    Encoding::ASCII_8BIT
  end

  def usascii_encoding
    Encoding::US_ASCII
  end

  def utf8_encoding
    Encoding::UTF_8
  end

  def rb_default_external_encoding
    Encoding.find('external')
  end

  def rb_default_internal_encoding
    Encoding.find('internal')
  end

  def rb_locale_encoding
    Encoding.find('locale')
  end

  def rb_filesystem_encoding
    Encoding.find('filesystem')
  end

  def rb_to_encoding_index(enc)
    enc = Rubinius::Type.coerce_to_encoding(enc)
    return -1 if enc == false
    rb_enc_find_index(enc.name)
  end

  def rb_locale_encindex
    rb_enc_find_index Encoding.find('locale').name
  end

  def rb_filesystem_encindex
    rb_enc_find_index Encoding.find('filesystem').name
  end

  def rb_ascii8bit_encindex
    rb_enc_find_index Encoding::ASCII_8BIT.name
  end

  def rb_usascii_encindex
    rb_enc_find_index Encoding::US_ASCII.name
  end

  def rb_utf8_encindex
    rb_enc_find_index Encoding::UTF_8.name
  end

  def rb_enc_from_index(index)
    Truffle.invoke_primitive :encoding_get_encoding_by_index, index
  end

  def rb_enc_find_index(name)
    key = name.upcase.to_sym
    pair = Encoding::EncodingMap[key]
    if pair
      pair.last
    else
      -1
    end
  end

  def rb_enc_to_index(enc)
    rb_enc_find_index(enc.name)
  end

  def rb_str_new_frozen(value)
    if value.frozen?
      value
    else
      value.dup.freeze
    end
  end

  def rb_thread_alone
    Thread.list.count == 1 ? 1 : 0
  end

  def rb_intern(str)
    str.intern
  end

  def rb_str_new(string, length)
    string.to_s[0, length].b
  end

  def rb_cstr_to_inum(string, base, raise)
    Truffle.invoke_primitive :string_to_inum, string, base, raise != 0
  end

  def rb_str_new_nul(length)
    "\0".b * length
  end

  def rb_str_new_cstr(java_string)
    String.new(java_string)
  end

  def rb_enc_str_coderange(str)
    cr = Truffle.invoke_primitive :string_get_coderange, str
    coderange_java_to_rb(cr)
  end

  def coderange_java_to_rb(cr)
    case cr
    when 0
      RUBY_ENC_CODERANGE_UNKNOWN
    when 1
      RUBY_ENC_CODERANGE_7BIT
    when 2
      RUBY_ENC_CODERANGE_VALID
    when 3
      RUBY_ENC_CODERANGE_BROKEN
    else
      raise "Cannot convert coderange #{cr} to rb code range"
    end
  end

  def RB_ENC_CODERANGE(obj)
    if obj.is_a? String
      rb_enc_str_coderange(obj)
    else
      raise "Unknown coderange for obj with class `#{obj.class}`"
    end
  end

  def rb_enc_associate_index(obj, idx)
    enc = rb_enc_from_index(idx)
    case obj
    when String
      obj.force_encoding(enc)
    else
      raise "rb_enc_associate_index not implemented for class `#{obj.class}`"
    end
  end

  def rb_enc_set_index(obj, idx)
    enc = rb_enc_from_index(idx)
    case obj
    when String
      obj.force_encoding enc
    else
      obj.instance_variable_set :@encoding, enc
    end
  end

  def rb_enc_get(obj)
    case obj
    when Encoding
      obj
    when Symbol
      obj.encoding
    when String
      obj.encoding
    when Regexp
      obj.encoding
    else
      obj.instance_variable_get :@encoding
    end
  end

  def rb_enc_get_index(obj)
    enc = case obj
          when Symbol
            obj.encoding
          when String
            obj.encoding
          when Regexp
            obj.encoding
          when File
            obj.internal_encoding || obj.external_encoding
          when NilClass, Fixnum, Float, TrueClass, FalseClass
            -1
          # TODO BJF Mar-9-2017 Handle T_DATA
          else
            0
          end
    enc = rb_enc_find_index(enc.name) if enc.is_a?(Encoding)
    enc
  end

  def rb_intern_str(string)
    string.intern
  end

  def rb_intern3(string, enc)
    string.force_encoding(enc).intern
  end

  def rb_str_append(str, to_append)
    str << StringValue(to_append)
  end

  def rb_str_encode(str, to, ecflags, ecopts)
    opts = {}
    opts.merge!(ecopts) unless ecopts.nil?

    # TODO BJF 8-Mar-2017 Handle more ecflags
    if ecflags & Encoding::Converter::INVALID_REPLACE != 0
      opts.merge!({:invalid => :replace})
    end

    if opts.empty?
      str.encode(to)
    else
      str.encode(to, opts)
    end
  end

  def rb_str_conv_enc_opts(str, from, to, ecflags, ecopts)
    if (to.ascii_compatible? && str.ascii_only?) || to == Encoding::ASCII_8BIT
      if str.encoding != to
        str = str.dup
        str.force_encoding(to)
      end
      return str
    end
    begin
      rb_str_encode(str, to, ecflags, ecopts)
    rescue Encoding::InvalidByteSequenceError
      str
    end
  end

  def rb_cmpint(val, a, b)
    raise ArgumentError, "comparison of #{a.class} and #{b.class} failed" if val.nil?
    if val > 0
      1
    elsif val < 0
      -1
    else
      0
    end
  end

  def rb_funcall_with_block(recv, meth, args, block)
    recv.public_send(meth, *args, &block)
  end

  def rb_funcallv_public(recv, meth, args)
    recv.public_send(meth, *args)
  end

  def rb_funcallv(recv, meth, args)
    rb_funcall(recv, meth, nil, *args)
  end

  def rb_funcall(recv, meth, n, *args)
    old_c_block = Thread.current[:__C_BLOCK__]
    begin
      block = Thread.current[:__C_BLOCK__]
      Thread.current[:__C_BLOCK__] = nil
      if block
        recv.send(meth, *args, &block)
      else
        recv.send(meth, *args)
      end
    ensure
      Thread.current[:__C_BLOCK__] = old_c_block
    end
  end

  def rb_apply(recv, meth, args)
    recv.send(meth, *args)
  end

  def rb_define_attr(klass, name, read, write)
    if read != 0 && write != 0
      klass.class_eval { attr_accessor name }
    elsif read != 0
      klass.class_eval { attr_reader name }
    elsif write != 0
      klass.class_eval { attr_writer name }
    end
  end

  def rb_make_backtrace
    caller
  end

  def rb_string_value_cstr_check(string)
    !string.include?("\0")
  end

  def rb_String(value)
    String(value)
  end

  def rb_Array(value)
    Array(value)
  end

  def rb_ary_new
    []
  end

  def rb_ary_new_capa(capacity)
    []
  end

  def rb_hash_new
    {}
  end

  def rb_hash_set_ifnone(hash, value)
    hash.default = value
  end

  def rb_path_to_class(path)
    begin
      const = Object.const_get(path, false)
    rescue NameError => e
      raise ArgumentError, e.message
    end
    raise TypeError unless const.is_a?(Class)
    const
  end

  def rb_proc_new(function, value)
    Proc.new do |*args|
      Truffle::Interop.execute(function, *args)
    end
  end

  def rb_yield(value)
    block = get_block
    block.call(value)
  end

  def rb_yield_splat(values)
    block = get_block
    block.call(*values)
  end

  def rb_ivar_lookup(object, name, default_value)
    # TODO CS 24-Jul-16 races - needs a new primitive or be defined in Java?
    if object.instance_variable_defined?(name)
      object.instance_variable_get(name)
    else
      default_value
    end
  end

  def rb_cvar_defined(cls, id)
    id_s = id.to_s
    if id_s.start_with?('@@') || !id_s.start_with?('@')
      cls.class_variable_defined?(id)
    else
      cls.instance_variable_defined?(id)
    end
  end

  def rb_cv_get(cls, name)
    cls.class_variable_get(name.to_sym)
  end

  def rb_cv_set(cls, name, val)
    cls.class_variable_set(name.to_sym, val)
  end

  def rb_cvar_get(cls, id)
    cls.class_variable_get(id)
  end

  def rb_cvar_set(cls, id, val)
    cls.class_variable_set(id, val)
  end

  def rb_exc_raise(exception)
    raise exception
  end

  def rb_set_errinfo(error)
    if !error.nil? && !error.is_a?(Exception)
      raise TypeError, 'assigning non-exception to ?!'
    end
    $! = error
  end

  def rb_raise(object, name)
    raise 'not implemented'
  end

  def rb_ivar_get(object, name)
    Truffle.invoke_primitive :object_ivar_get, object, name.to_sym
  end

  def rb_ivar_set(object, name, value)
    Truffle.invoke_primitive :object_ivar_set, object, name.to_sym, value
  end

  def rb_special_const_p(object)
    object == nil || object == true || object == false || object.class == Symbol || object.class == Fixnum
  end

  def rb_id2str(sym)
    sym.to_s
  end

  def rb_define_class_under(mod, name, superclass)
    if mod.const_defined?(name, false)
      current_class = mod.const_get(name, false)
      unless current_class.class == Class
        raise TypeError, "#{mod}::#{name} is not a class"
      end
      if superclass != current_class.superclass
        raise TypeError, "superclass mismatch for class #{name}"
      end
      current_class
    else
      mod.const_set name, Class.new(superclass)
    end
  end

  def rb_define_module_under(mod, name)
    if mod.const_defined?(name, false)
      val = mod.const_get(name, false)
      unless val.class == Module
        raise TypeError, "#{mod}::#{name} is not a module"
      end
      val
    else
      mod.const_set name, Module.new
    end
  end

  def rb_define_method(mod, name, function, argc)
    mod.send(:define_method, name) do |*args|
      if argc == -1
        args = [args.size, args, self]
      else
        args = [self, *args]
      end

      # Using raw execute instead of #call here to avoid argument conversion
      Truffle::Interop.execute(function, *args)
    end
  end

  def rb_class_new_instance(klass, args)
    klass.new(*args)
  end

  def rb_f_sprintf(args)
    sprintf(*args)
  end

  def rb_io_printf(out, args)
    out.printf(*args)
  end

  def rb_io_print(out, args)
    out.print(*args)
  end

  def rb_io_puts(out, args)
    out.puts(*args)
  end

  def rb_obj_call_init(obj, args)
    obj.send(:initialize, *args)
  end

  def rb_obj_instance_eval(obj, args, block)
    obj.instance_eval(*args, &block)
  end

  def rb_enumeratorize(obj, meth, args)
    obj.to_enum(meth, *args)
  end

  def rb_eval_string(str)
    eval(str)
  end

  def rb_define_private_method(mod, name, function, argc)
    rb_define_method(mod, name, function, argc)
    mod.send :private, name
  end

  def rb_define_protected_method(mod, name, function, argc)
    rb_define_method(mod, name, function, argc)
    mod.send :protected, name
  end

  def rb_define_module_function(mod, name, function, argc)
    rb_define_method(mod, name, function, argc)
    cext_module_function mod, name.to_sym
  end

  def rb_define_singleton_method(object, name, function, argc)
    rb_define_method(object.singleton_class, name, function, argc)
  end

  def rb_define_alloc_func(ruby_class, function)
    ruby_class.singleton_class.send(:define_method, :__allocate__) do
      function.call(self)
    end
    class << ruby_class
      private :__allocate__
    end
  end

  def rb_alias(mod, new_name, old_name)
    mod.send(:alias_method, new_name, old_name)
  end

  def rb_undef(mod, name)
    if mod.frozen? or mod.method_defined?(name)
      mod.send(:undef_method, name)
    end
  end

  def rb_attr(ruby_class, name, read, write, ex)
    if ex.zero?
      private = false
      protected = false
      module_function = false
    else
      private = caller_frame_visibility(:private)
      protected = caller_frame_visibility(:protected)
      module_function = caller_frame_visibility(:module_function)
    end

    if read
      ruby_class.send :attr_reader, name
      ruby_class.send :private, name if private
      ruby_class.send :protected, name if protected
      ruby_class.send :module_function, name if module_function
    end

    if write
      ruby_class.send :attr_writer, name
      setter_name = :"#{name}="
      ruby_class.send :private, setter_name if private
      ruby_class.send :protected, setter_name if protected
      ruby_class.send :module_function, setter_name if module_function
    end
  end

  def rb_Rational(num, den)
    Rational.new(num, den)
  end

  def rb_rational_raw(num, den)
    Rational.new(num, den)
  end

  def rb_rational_new(num, den)
    Rational(num, den)
  end

  def rb_Complex(real, imag)
    Complex.new(real, imag)
  end

  def rb_complex_raw(real, imag)
    Complex.new(real, imag)
  end

  def rb_complex_new(real, imag)
    Complex(real, imag)
  end

  def rb_complex_polar(r, theta)
    Complex.new(r, theta)
  end

  def rb_complex_set_real(complex, real)
    Truffle.privately do
      complex.real = real
    end
  end

  def rb_complex_set_imag(complex, imag)
    Truffle.privately do
      complex.imag = imag
    end
  end

  def rb_mutex_new
    Mutex.new
  end

  def rb_mutex_locked_p(mutex)
    mutex.locked?
  end

  def rb_mutex_trylock(mutex)
    mutex.try_lock
  end

  def rb_mutex_lock(mutex)
    mutex.lock
  end

  def rb_mutex_unlock(mutex)
    mutex.unlock
  end

  def rb_mutex_sleep(mutex, timeout)
    mutex.sleep(timeout)
  end

  def rb_mutex_synchronize(mutex, func, arg)
    mutex.synchronize do
      Truffle::Interop.execute(func, arg)
    end
  end

  def rb_gc_enable
    GC.enable
  end

  def rb_gc_disable
    GC.disable
  end

  def rb_nativethread_self
    Thread.current
  end

  def rb_nativethread_lock_initialize
    Mutex.new
  end

  def rb_data_object_wrap(ruby_class, data, mark, free)
    ruby_class = Object if Truffle::Interop.null?(ruby_class)
    object = ruby_class.internal_allocate
    object.instance_variable_set :@data, data
    ObjectSpace.define_finalizer object, data_finalizer(data, free)
    object
  end

  def data_finalizer(data, free)
    proc {
      Truffle::Debug.log_warning 'native data finalizer not being called; probably leaking resources'
      # TODO CS 14-Mar-17 data pointer has escaped
      # Truffle::Interop.execute(free, data)
    }
  end

  def rb_data_typed_object_wrap(ruby_class, data, data_type)
    object = ruby_class.internal_allocate
    object.instance_variable_set :@data_type, data_type
    object.instance_variable_set :@data, data
    object
  end

  def rb_ruby_verbose_ptr
    $VERBOSE
  end

  def rb_ruby_debug_ptr
    $DEBUG
  end

  def rb_tr_error(message)
    raise RubyTruffleError.new(message)
  end

  def test_kwargs(kwargs, raise_error)
    if kwargs.keys.all? { |k| k.is_a?(Symbol) }
      true
    elsif raise_error
      raise ArgumentError
    else
      false
    end
  end

  def send_splatted(object, method, args)
    object.send(method, *args)
  end

  def rb_block_call(object, method, args, func, data)
    object.send(method, *args) do |*block_args|
      Truffle::Interop.execute(func, block_args.first, data, block_args.size, block_args)
    end
  end

  def rb_ensure(b_proc, data1, e_proc, data2)
    begin
      Truffle::Interop.execute(b_proc, data1)
    ensure
      Truffle::Interop.execute(e_proc, data2)
    end
  end

  def rb_rescue(b_proc, data1, r_proc, data2)
    begin
      Truffle::Interop.execute(b_proc, data1)
    rescue StandardError => e
      Truffle::Interop.execute(r_proc, data2, e)
    end
  end

  def rb_rescue2(b_proc, data1, r_proc, data2, rescued)
    begin
      Truffle::Interop.execute(b_proc, data1)
    rescue *rescued => e
      Truffle::Interop.execute(r_proc, data2, e)
    end
  end

  def rb_exec_recursive(func, obj, arg)
    result = nil

    recursive = Thread.detect_recursion(obj) {
      result = Truffle::Interop.execute(func, obj, arg, 0)
    }

    if recursive
      Truffle::Interop.execute(func, obj, arg, 1)
    else
      result
    end
  end

  def rb_catch_obj(tag, func, data)
    catch tag do |caught|
      Truffle::Interop.execute(func, caught, data)
    end
  end

  def rb_struct_define_no_splat(name, attrs)
    Struct.new(name, *attrs)
  end

  def rb_struct_aref(struct, index)
    struct[index]
  end

  def rb_struct_aset(struct, index, value)
    struct[index] = value
  end

  def rb_struct_size(klass)
    klass.members.size
  end

  def rb_struct_new_no_splat(klass, args)
    klass.new(*args)
  end

  def yield_no_block
    raise LocalJumpError
  end

  def warn?
    !$VERBOSE.nil?
  end

  def warning?
    $VERBOSE
  end

  def rb_syserr_fail(errno, message)
    raise SystemCallError.new(message, errno)
  end

  def rb_sys_fail(message)
    if message.nil?
      raise Errno::EPERM
    else
      raise SystemCallError.new(message)
    end
  end

  def rb_time_nano_new(sec, nsec)
    Time.at sec, Rational(nsec, 1000)
  end

  def rb_time_timespec_new(sec, nsec, offset, is_utc, is_local)
    time = rb_time_nano_new(sec, nsec)
    return time if is_local
    return time.getgm if is_utc
    time.getlocal(offset)
  end

  def rb_time_num_new(timev, off)
    Time.at(timev).getlocal(off)
  end

  def rb_time_interval_acceptable(time_val)
    # TODO (pitr-ch 09-Mar-2017): more precise error messages
    raise TypeError, 'cannot be Time' if time_val.is_a? Time
    raise ArgumentError, 'cannot be negative' if time_val < 0
  end

  def rb_thread_create(fn, args)
    Thread.new do
      Truffle::Interop.execute(fn, args)
    end
  end

  def rb_thread_call_without_gvl(function, data1, unblock, data2)
    unblocker = proc {
      Truffle::Interop.execute unblock, data2
    }

    runner = proc {
      Truffle::Interop.execute function, data1
    }

    Thread.current.unblock unblocker, runner
  end

  def rb_iterate_call_block( iter_block, block_arg, arg2)
    Truffle::Interop.execute iter_block, block_arg, arg2
  end

  def rb_iterate(function, arg1, iter_block, arg2, block)
    if block
      call_c_with_block function, arg1 do |block_arg|
        rb_iterate_call_block(iter_block, block_arg, arg2) do |*args|
          block.call(*args)
        end
      end
    else
      call_c_with_block function, arg1 do |block_arg|
        Truffle::Interop.execute iter_block, block_arg, arg2
      end
    end
  end

  def call_c_with_block(function, arg, &block)
    old_c_block = Thread.current[:__C_BLOCK__]
    begin
      Thread.current[:__C_BLOCK__] = block
      Truffle::Interop.execute function, arg
    ensure
      Thread.current[:__C_BLOCK__] = old_c_block
    end
  end

  def rb_call_super(args)
    rb_call_super_splatted(*args)
  end

  def rb_any_to_s(object)
    object.to_s
  end

  def rb_class_inherited_p(ruby_module, object)
    if object.is_a?(Module)
      ruby_module <= object
    else
      raise TypeError
    end
  end

  def rb_tr_readable(mode)
    mode == File::Constants::RDONLY || mode == File::Constants::RDWR
  end

  def rb_tr_writable(mode)
    mode == File::Constants::WRONLY || mode == File::Constants::RDWR
  end

  def rb_gv_set(name, value)
    binding.eval("#{name} = value")
  end

  def rb_gv_get(name)
    name = "$#{name}" unless name.to_s.start_with?('$')
    if name == '$~'
      rb_backref_get
    else
      eval("#{name}")
    end
  end

  def rb_reg_match(re, str)
    re =~ str
  end

  def rb_hash_aref(object, key)
    object[key]
  end

  def rb_define_hooked_variable(name, data, getter, setter)
    name = "$#{name}" unless name.start_with?('$')
    id = name.to_sym

    gvar = nil

    getter_proc = proc {
      Truffle::Interop.execute getter, id, data, gvar
    }

    setter_proc = proc { |value|
      Truffle::Interop.execute setter, value, id, data, gvar
    }

    rb_define_hooked_variable_inner id, getter_proc, setter_proc
  end

  def rb_tr_log_warning(message)
    Truffle::Debug.log_warning message
  end

  def RDATA(object)
    RData.new(object)
  end

  def rb_to_encoding(encoding)
    encoding = Encoding.find(encoding.to_str) unless encoding.is_a?(Encoding)
    RbEncoding.new(encoding)
  end

  def rb_enc_from_encoding(rb_encoding)
    rb_encoding.encoding
  end

  def RSTRING_PTR(string)
    RStringPtr.new(string)
  end

end

Truffle::Interop.export(:ruby_cext, Truffle::CExt)
