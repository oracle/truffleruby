# frozen_string_literal: false
# truffleruby_primitives: true

# Copyright (c) 2013, Brian Shirai
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
#    list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. Neither the name of the library nor the names of its contributors may be
#    used to endorse or promote products derived from this software without
#    specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
# OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
require 'truffle/cext'

Truffle::CExt.rb_define_module_under(IO, 'generic_readable').module_eval do
  # This is why we need undefined in Ruby
  Undefined = Object.new

  def readchar
    raise EOFError, 'end of file reached' if eof?
    getc
  end

  def readbyte
    readchar.getbyte(0)
  end

  def readline(sep=$/, limit=::Undefined)
    check_readable
    raise EOFError, 'end of file reached' if eof?

    Primitive.io_last_line_set(Primitive.caller_special_variables, getline(true, sep, limit))
  end

  def sysread(length = nil, buffer = nil)
    str = read(length, buffer)

    if str.nil?
      raise EOFError, 'end of file reached'
    end

    str
  end
  alias_method :readpartial, :sysread

  def read_nonblock(length, buffer = nil, exception: true)
    str = read(length, buffer)

    if exception and str.nil?
      raise EOFError, 'end of file reached'
    end

    str
  end

end

Truffle::CExt.rb_define_module_under(IO, 'generic_writable').module_eval do
  def <<(str)
    write(str)
    self
  end

  def print(*args)
    check_writable
    args << Primitive.io_last_line_get(Primitive.caller_special_variables) if args.empty?
    write((args << $\).flatten.join)
    nil
  end

  def printf(*args)
    check_writable

    if args.size > 1
      write(args.shift % args)
    else
      write(args.first)
    end

    nil
  end

  def puts(*args)
    Truffle::IOOperations.puts(self, *args)
  end

  def write_nonblock(str)
    write(str)
  end

  def syswrite(str)
    write(str)
  end
end

class StringIO

  include Enumerable
  include Truffle::CExt.rb_define_module_under(IO, 'generic_readable')
  include Truffle::CExt.rb_define_module_under(IO, 'generic_writable')

  DEFAULT_RECORD_SEPARATOR = "\n" unless defined?(::DEFAULT_RECORD_SEPARATOR)

  class Data
    attr_accessor :string, :pos, :lineno, :encoding

    def initialize(string)
      @string = string
      @encoding = string.encoding
      @pos = @lineno = 0
    end
  end

  def self.open(*args)
    io = new(*args)
    return io unless block_given?

    begin
      yield io
    ensure
      io.close
      io.__data__.string = nil
      self
    end
  end

  attr_reader :__data__

  def initialize(string=nil, mode=nil)
    if string.nil?
      @__data__ = Data.new ''.force_encoding(Encoding.default_external)
      mode = IO::RDWR
    else
      string = Truffle::Type.coerce_to string, String, :to_str
      @__data__ = Data.new string
    end

    if mode
      if mode.is_a?(Integer)
        mode_from_integer(mode)
      else
        mode = StringValue(mode)
        mode_from_string(mode)
      end
    else
      mode_from_string(string.frozen? ? 'r' : 'r+')
    end

    self
  end

  def initialize_copy(from)
    from = Truffle::Type.coerce_to(from, StringIO, :to_strio)

    @append = from.instance_variable_get(:@append)
    @readable = from.instance_variable_get(:@readable)
    @writable = from.instance_variable_get(:@writable)
    @__data__ = from.instance_variable_get(:@__data__)

    self
  end

  # StringIO#inspect should not include the contents of the StringIO
  alias_method :inspect, :to_s

  private def check_readable
    raise IOError, 'not opened for reading' unless @readable
  end

  private def check_writable
    raise IOError, 'not opened for writing' unless @writable
    raise IOError, 'unable to modify data' if @__data__.string.frozen?
  end

  def set_encoding(external, internal=nil, options=nil)
    encoding = external || Encoding.default_external
    @__data__.encoding = encoding
    @__data__.string.force_encoding(encoding) if @writable

    self
  end

  def external_encoding
    @__data__.encoding
  end

  def internal_encoding
    nil
  end

  def each_byte
    return to_enum :each_byte unless block_given?
    check_readable

    d = @__data__
    string = d.string

    while d.pos < string.length
      byte = string.getbyte d.pos
      d.pos += 1
      yield byte
    end

    self
  end
  alias_method :bytes, :each_byte

  def each_char
    return to_enum :each_char unless block_given?
    while s = getc
      yield s
    end

    self
  end
  alias_method :chars, :each_char

  def each_codepoint(&block)
    return to_enum :each_codepoint unless block_given?
    check_readable

    d = @__data__
    string = d.string

    while d.pos < string.bytesize
      char = Primitive.string_chr_at(string, d.pos)

      unless char
        raise ArgumentError, "invalid byte sequence in #{d.encoding}"
      end

      d.pos += char.bytesize
      yield char.ord
    end

    self
  end
  alias_method :codepoints, :each_codepoint

  def each(sep=$/, limit=Undefined)
    return to_enum :each, sep, limit unless block_given?
    check_readable

    while line = getline(true, sep, limit)
      yield line
    end

    self
  end
  alias_method :each_line, :each
  alias_method :lines, :each

  def binmode
    set_encoding(Encoding::BINARY)
    self
  end

  def write(str)
    check_writable

    str = String(str)
    return 0 if str.empty?

    d = @__data__
    pos = d.pos
    string = d.string

    if @append || pos == string.bytesize
      Primitive.string_byte_append(string, str)
      d.pos = string.bytesize
    elsif pos > string.bytesize
      replacement = "\000" * (pos - string.bytesize)
      Primitive.string_splice(string, replacement, string.bytesize, 0, string.encoding)
      Primitive.string_byte_append(string, str)
      d.pos = string.bytesize
    else
      stop = string.bytesize - pos
      if str.bytesize < stop
        stop = str.bytesize
      end
      Primitive.string_splice(string, str, pos, stop, string.encoding)
      d.pos += str.bytesize
    end

    str.bytesize
  end

  def close
    @readable = @writable = nil
  end

  def closed?
    !@readable && !@writable
  end

  def close_read
    check_readable
    @readable = nil
  end

  def closed_read?
    !@readable
  end

  def close_write
    check_writable
    @writable = nil
  end

  def closed_write?
    !@writable
  end

  def eof?
    d = @__data__
    d.pos >= d.string.bytesize
  end
  alias_method :eof, :eof?

  def fcntl
    raise NotImplementedError, 'StringIO#fcntl is not implemented'
  end

  def fileno
    nil
  end

  def flush
    self
  end

  def fsync
    0
  end

  def getc
    check_readable
    d = @__data__

    return nil if eof?

    char = Primitive.string_find_character(d.string, d.pos)
    d.pos += char.bytesize
    char
  end

  def getbyte
    check_readable
    d = @__data__

    return nil if eof?

    byte = d.string.getbyte(d.pos)
    d.pos += 1
    byte
  end

  def gets(sep=$/, limit=Undefined)
    check_readable

    # Truffle: $_ is thread and frame local, so we use a primitive to
    # set it in the caller's frame.
    Primitive.io_last_line_set(Primitive.caller_special_variables, getline(false, sep, limit))
  end

  def isatty
    false
  end
  alias_method :tty?, :isatty

  def lineno
    @__data__.lineno
  end

  def lineno=(line)
    @__data__.lineno = line
  end

  def pid
    nil
  end

  def pos
    @__data__.pos
  end

  def pos=(pos)
    raise Errno::EINVAL if pos < 0
    @__data__.pos = pos
  end

  def putc(obj)
    check_writable

    if obj.is_a?(String)
      char = obj[0]
    else
      c = Truffle::Type.coerce_to obj, Integer, :to_int
      char = (c & 0xff).chr
    end

    d = @__data__
    pos = d.pos
    string = d.string

    if @append || pos == string.bytesize
      Primitive.string_byte_append(string, char)
      d.pos = string.bytesize
    elsif pos > string.bytesize
      replacement = "\000" * (pos - string.bytesize)
      Primitive.string_splice(string, replacement, string.bytesize, 0, string.encoding)
      Primitive.string_byte_append(string, char)
      d.pos = string.bytesize
    else
      Primitive.string_splice(string, char, pos, char.bytesize, string.encoding)
      d.pos += char.bytesize
    end

    obj
  end

  def read(length = nil, buffer = nil)
    check_readable
    d = @__data__
    pos = d.pos
    string = d.string

    if length
      length = Truffle::Type.coerce_to length, Integer, :to_int
      raise ArgumentError if length < 0

      buffer = StringValue(buffer) if buffer

      if eof?
        buffer.clear if buffer
        if length == 0
          return ''.force_encoding(Encoding::ASCII_8BIT)
        else
          return nil
        end
      end

      str = string.byteslice(pos, length)
      str.force_encoding Encoding::ASCII_8BIT

      str = buffer.replace(str) if buffer
    else
      if eof?
        buffer.clear if buffer
        return ''.force_encoding(Encoding::ASCII_8BIT)
      end

      str = string.byteslice(pos..-1)
      buffer.replace str if buffer
    end

    d.pos += str.length
    str
  end

  def readlines(sep=$/, limit=Undefined)
    check_readable

    ary = []
    while line = getline(true, sep, limit)
      ary << line
    end

    ary
  end

  def reopen(string=nil, mode=Undefined)
    if string and not string.kind_of? String and mode.equal? Undefined
      stringio = Truffle::Type.coerce_to(string, StringIO, :to_strio)

      initialize_copy stringio
    else
      mode = nil if mode.equal? Undefined
      string = '' unless string

      initialize string, mode
    end

    self
  end

  def rewind
    d = @__data__
    d.pos = d.lineno = 0
  end

  def seek(to, whence = IO::SEEK_SET)
    raise IOError, 'closed stream' if self.closed?
    to = Truffle::Type.coerce_to to, Integer, :to_int

    case whence
    when IO::SEEK_CUR
      to += @__data__.pos
    when IO::SEEK_END
      to += @__data__.string.bytesize
    when IO::SEEK_SET, nil
    else
      raise Errno::EINVAL, 'invalid whence'
    end

    raise Errno::EINVAL if to < 0

    @__data__.pos = to

    0
  end

  def size
    @__data__.string.bytesize
  end
  alias_method :length, :size

  def string
    @__data__.string
  end

  def string=(string)
    d = @__data__
    d.string = StringValue(string)
    d.pos = 0
    d.lineno = 0
  end

  def sync
    true
  end

  def sync=(val)
    val
  end

  def tell
    @__data__.pos
  end

  def truncate(length)
    check_writable
    len = Truffle::Type.coerce_to length, Integer, :to_int
    raise Errno::EINVAL, 'negative length' if len < 0
    string = @__data__.string

    if len < string.bytesize
      string[len..string.bytesize] = ''
    else
      string << "\000" * (len - string.bytesize)
    end
    length
  end

  def ungetc(char)
    check_readable

    d = @__data__
    pos = d.pos
    string = d.string

    if char.kind_of? Integer
      char = Truffle::Type.coerce_to char, String, :chr
    else
      char = Truffle::Type.coerce_to char, String, :to_str
    end

    if pos > string.bytesize
      string[string.bytesize..pos] = "\000" * (pos - string.bytesize)
      d.pos -= 1
      string[d.pos] = char
    elsif pos > 0
      d.pos -= 1
      string[d.pos] = char
    end

    nil
  end

  def ungetbyte(bytes)
    check_readable

    return unless bytes

    if bytes.kind_of? Integer
      bytes = ''.b << (bytes & 0xff)
    else
      bytes = StringValue(bytes).b
      return if bytes.bytesize == 0
    end

    d = @__data__
    pos = d.pos
    string = d.string.b

    enc = d.string.encoding

    if d.pos == 0
      d.string = bytes << string
    else
      size = bytes.bytesize
      a = string.byteslice(0, pos - size) if size < pos
      b = string.byteslice(pos..-1)
      d.string = "#{a}#{bytes}#{b}"
      d.pos = pos > size ? pos - size : 0
    end

    d.string.force_encoding enc
    nil
  end

  def encode_with(coder)
  end

  def init_with(coder)
    @__data__ = Data.new('')
  end

  def to_yaml_properties
    []
  end

  def yaml_initialize(type, val)
    @__data__ = Data.new('')
  end

  private def mode_from_string(mode)
    @append = truncate = false

    if mode[0] == ?r
      @readable = true
      @writable = mode[-1] == ?+ ? true : false
    end

    if mode[0] == ?w
      @writable = truncate = true
      @readable = mode[-1] == ?+ ? true : false
    end

    if mode[0] == ?a
      @append = @writable = true
      @readable = mode[-1] == ?+ ? true : false
    end

    d = @__data__
    raise Errno::EACCES, 'Permission denied' if @writable && d.string.frozen?
    d.string.replace('') if truncate
  end

  private def mode_from_integer(mode)
    @readable = @writable = @append = false
    d = @__data__

    if mode == 0 or mode & IO::RDWR != 0
      @readable = true
    end

    if mode & (IO::WRONLY | IO::RDWR) != 0
      raise Errno::EACCES, 'Permission denied' if d.string.frozen?
      @writable = true
    end

    @append = true if (mode & IO::APPEND) != 0
    d.string.replace('') if (mode & IO::TRUNC) != 0
  end

  private def getline(arg_error, sep, limit)
    if limit != Undefined
      limit = Primitive.rb_to_int limit if limit
      sep = Truffle::Type.coerce_to sep, String, :to_str if sep
    else
      limit = nil

      unless sep == $/ or sep.nil?
        osep = sep
        sep = Truffle::Type.rb_check_convert_type sep, String, :to_str
        limit = Primitive.rb_to_int osep unless sep
      end
    end

    raise ArgumentError if arg_error and limit == 0

    return nil if eof?

    d = @__data__
    pos = d.pos
    string = d.string

    if sep.nil?
      if limit
        line = string.byteslice(pos, limit)
      else
        line = string.byteslice(pos, string.bytesize - pos)
      end
      d.pos += line.bytesize
    elsif sep.empty?
      if stop = Primitive.find_string(string, "\n\n", pos)
        stop += 2
        line = string.byteslice(pos, stop - pos)
        while string.getbyte(stop) == 10
          stop += 1
        end
        d.pos = stop
      else
        line = string.byteslice(pos, string.bytesize - pos)
        d.pos = string.bytesize
      end
    else
      if stop = Primitive.find_string(string, sep, pos)
        if limit && stop - pos >= limit
          stop = pos + limit
        else
          stop += sep.bytesize
        end
        line = string.byteslice(pos, stop - pos)
        d.pos = stop
      else
        if limit
          line = string.byteslice(pos, limit)
        else
          line = string.byteslice(pos, string.bytesize - pos)
        end
        d.pos += line.bytesize
      end
    end

    d.lineno += 1

    line
  end

  def marshal_dump
    raise TypeError, "can't dump #{self.class}"
  end
end
