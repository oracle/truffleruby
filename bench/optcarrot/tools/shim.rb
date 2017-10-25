# This is a shim for Ruby implementations other than MRI 2.
#
# Fortunately, most of these methods are not used in hot-spot (except
# Array#rotate!), so I don't think that this shim will degrade the performance.
# However, some implementations may stop optimization when a built-in classes
# are modified by monkey-patching.  In this case, the speed will be reduced.

# I want to make this shim so simple that you don't need doc...
# rubocop:disable Style/Documentation

RUBY_ENGINE = "ruby" if RUBY_VERSION == "1.8.7" && !Module.const_defined?(:RUBY_ENGINE)
if RUBY_ENGINE == "opal"
  require "opal-parser" # for eval
  require "nodejs"
end

unless [].respond_to?(:rotate!)
  # Array#rotate! is used in hotspot; this shim will reduce the performance terribly.
  # This shim is for MRI 1.8.7.  1.8.7 has a handicap.
  $stderr.puts "[shim] Array#rotate!"
  class Array
    def rotate!(n)
      if n > 0
        concat(shift(n))
      elsif n < 0
        unshift(*pop(-n))
      end
      self
    end
  end
end

unless [].respond_to?(:slice!)
  $stderr.puts "[shim] Array#slice!"
  class Array
    def slice!(_zero_assumed, len)
      a = []
      len.times { a << shift }
      a
    end
  end
end

unless [].respond_to?(:flat_map)
  $stderr.puts "[shim] Array#flat_map"
  class Array
    def flat_map(&blk)
      map(&blk).flatten(1)
    end
  end
end

unless [].respond_to?(:transpose)
  $stderr.puts "[shim] Array#transpose"
  class Array
    def transpose
      ret = self[0].map { [] }
      self[0].size.times do |i|
        size.times do |j|
          ret[i] << self[j][i]
        end
      end
      ret
    end
  end
end

if ![].respond_to?(:freeze) || RUBY_ENGINE == "opal"
  $stderr.puts "[shim] Array#freeze"
  class Array
    def freeze
      self
    end
  end
end

unless [].respond_to?(:pack) && [33, 33].pack("C*") == "!!"
  $stderr.puts "[shim] Array#pack"
  class Array
    alias pack_orig pack if [].respond_to?(:pack)
    def pack(fmt)
      if fmt == "C*"
        map {|n| n.chr }.join
      else
        pack_orig(fmt)
      end
    end
  end
end

if {}.respond_to?(:compare_by_identity)
  # https://github.com/jruby/jruby/issues/3650
  h = {}.compare_by_identity
  a = [0]
  h[a] = 42
  a[0] = 1
  need_custom_identity_hash = !h[a]
else
  need_custom_identity_hash = true
end
if need_custom_identity_hash
  $stderr.puts "[shim] Hash#compare_by_identity"
  class IdentityHash
    def initialize
      @h = {}
    end

    def [](key)
      @h[key.object_id]
    end

    def []=(key, val)
      @h[key.object_id] = val
    end
  end

  class Hash
    def compare_by_identity
      IdentityHash.new
    end
  end
end

unless "".respond_to?(:b)
  $stderr.puts "[shim] String#b"
  class String
    def b
      self
    end
  end
end

unless "".respond_to?(:sum)
  $stderr.puts "[shim] String#sum"
  class String
    def sum(bits = 16)
      s = 0
      each_byte {|c| s += c }
      return 0 if s == 0
      s & ((1 << bits) - 1)
    end
  end
end

unless "".respond_to?(:bytes) && "".bytes == []
  if "".respond_to?(:unpack)
    $stderr.puts "[shim] String#bytes (by using unpack)"
    class String
      remove_method(:bytes) if "".respond_to?(:bytes)
      def bytes
        unpack("C*")
      end
    end
  else
    class String
      $stderr.puts "[shim] String#bytes (by aliasing)"
      alias bytes_orig bytes
      def bytes
        bytes_orig.to_a
      end
    end
  end
end

if RUBY_ENGINE == "opal"
  $stderr.puts "[shim] String#bytes (force_encoding)"
  class String
    alias bytes_orig bytes
    def bytes
      force_encoding("BINARY").bytes_orig
    end
  end
end

unless "".respond_to?(:tr) && !Module.const_defined?(:Topaz)
  $stderr.puts "[shim] String#tr"
  class String
    alias tr gsub
  end
end

if Module.const_defined?(:Topaz)
  # Topaz aborts when evaluating String#%...
  $stderr.puts "[shim] String#%"
  class String
    def %(*_args)
      "<String#format unavailable>"
    end

    def unpack(fmt)
      if fmt == "C*"
        return each_byte.to_a.map {|ch| ch.ord }
      else
        raise
      end
    end
  end

  begin
    $stderr.puts "[shim] FFI patched for topaz"
    require "ffi" if RUBY_ENGINE != "opal"
    module FFI
      class MemoryPointer
        def read_bytes(nbytes)
          get_bytes(0, nbytes)
        end
      end

      class Struct
        def self.layout(*_args)
          # ignore
        end

        def self.ptr
          :pointer
        end
      end

      module Library
        alias orig_attach_function attach_function
        def attach_function(name, *args)
          if name == :GetVersion
            # structs aren't complete, just say all our fields are 0
            self.class.send(:define_method, :GetVersion) do |version|
              def version.[](_n)
                0
              end
            end
          elsif name == :SetWindowIcon
            # this segfaults
            self.class.send(:define_method, :SetWindowIcon) {|*_args| }
          else
            orig_attach_function(name, *args)
          end
        end
      end
    end
  rescue LoadError
    $stderr.puts "[shim] (failed to load FFI for topaz)"
  end
end

unless 0.respond_to?(:[]) && -1[0] == 1
  $stderr.puts "[shim] Fixnum#[]"
  # rubocop:disable Lint/UnifiedInteger
  class Fixnum
    # rubocop:enable Lint/UnifiedInteger
    def [](i)
      (self >> i) & 1
    end
  end
end

unless 0.respond_to?(:even?)
  $stderr.puts "[shim] Fixnum#even?"
  # rubocop:disable Lint/UnifiedInteger
  class Fixnum
    # rubocop:enable Lint/UnifiedInteger
    def even?
      # rubocop:disable Style/EvenOdd
      self % 2 == 0
      # rubocop:enable Style/EvenOdd
    end
  end
end

begin
  1.step(3, 2)
rescue LocalJumpError
  $stderr.puts "[shim] Fixnum#step without block"
  # rubocop:disable Lint/UnifiedInteger
  class Fixnum
    # rubocop:enable Lint/UnifiedInteger
    alias step_org step
    def step(*args, &blk)
      if blk
        step_org(*args, &blk)
      else
        enum_for(:step_org, *args)
      end
    end
  end
end

unless Kernel.respond_to?(:__dir__)
  $stderr.puts "[shim] Kernel#__dir__"
  def __dir__
    File.join(File.dirname(File.dirname(__FILE__)), "bin")
  end
end

unless Kernel.respond_to?(:require)
  $stderr.puts "[shim] Kernel#require"
  DIRS = %w(lib lib/optcarrot).map {|f| File.join(File.dirname(File.dirname(__FILE__)), f) }
  $LOAD_PATH = []
  LOADED = {}
  def require(f)
    f = DIRS.map {|d| File.join(d, f + ".rb") }.find {|fn| File.exist?(fn) }
    return if LOADED[f]
    LOADED[f] = true
    eval(File.read(f), nil, f)
  end
end

unless Kernel.respond_to?(:require_relative)
  $stderr.puts "[shim] Kernel#require_relative"
  dir = File.join(File.dirname(File.dirname(__FILE__)), "lib")
  $LOAD_PATH << dir << File.join(dir, "optcarrot")
  unless RUBY_ENGINE == "opal"
    def require_relative(f)
      f = "optcarrot" if f == "../lib/optcarrot"
      require(f)
    end
  end
end

unless File.respond_to?(:extname)
  $stderr.puts "[shim] File.extname"
  def File.extname(f)
    f =~ /\..*\z/
    $&
  end
end

unless File.respond_to?(:binread)
  if RUBY_ENGINE == "opal"
    $stderr.puts "[shim] File.binread (for opal/nodejs)"
    class Blob
      def initialize(buf)
        @buf = buf
      end

      # rubocop:disable Style/CommandLiteral
      def bytes
        %x{
          var __buf__ = #{ @buf };
          var __ary__ = [];
          for (var i = 0, length = __buf__.length; i < length; i++) {
            __ary__.push(__buf__[i]);
          }
          return __ary__;
        }
      end
      # rubocop:enable Style/CommandLiteral
    end

    class File
      def self.binread(f)
        Blob.new(`#{ node_require(:fs) }.readFileSync(#{ f })`)
      end
    end
  else
    $stderr.puts "[shim] File.binread (by using open)"
    class File
      def self.binread(file)
        open(file, "rb") {|f| f.read }
      end
    end
  end
end

unless Module.const_defined?(:Process)
  module Process
  end
end
unless Process.respond_to?(:clock_gettime) && Process.const_defined?(:CLOCK_MONOTONIC)
  $stderr.puts "[shim] Process.clock_gettime"
  def Process.clock_gettime(*)
    Time.now.to_f
  end
  Process::CLOCK_MONOTONIC = nil unless Process.const_defined?(:CLOCK_MONOTONIC)
end

module M
  module_function

  def foo
  end
end
unless M.respond_to?(:foo)
  $stderr.puts "[shim] Module#module_function"
  class Module
    def module_function
      extend(self)
    end
  end
end

unless "".method(:b).respond_to?(:[])
  $stderr.puts "[shim] Method#[]"
  class Method
    alias [] call
  end
end

if !Module.const_defined?(:Fiber) && RUBY_ENGINE != "opal"
  $stderr.puts "[shim] Fiber"
  require "thread"

  Thread.abort_on_exception = true
  class Fiber
    # rubocop:disable Style/ClassVars
    def initialize
      @@mutex1 = Mutex.new
      @@mutex2 = Mutex.new
      @@mutex1.lock
      @@mutex2.lock
      Thread.new do
        @@mutex1.lock
        yield
        @@mutex2.unlock
      end
    end

    def resume
      @@mutex1.unlock
      @@mutex2.lock
      @@value
    end

    def self.yield(v = nil)
      @@mutex2.unlock
      @@value = v
      @@mutex1.lock
    end
    # rubocop:enable Style/ClassVars
  end
end

# directly executes bin/optcarrt since mruby does not support -r option
if RUBY_ENGINE == "mruby"
  eval(File.read(File.join(File.dirname(File.dirname(__FILE__)), "bin/optcarrot")))
end
