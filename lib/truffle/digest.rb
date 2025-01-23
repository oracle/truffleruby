# frozen_string_literal: true
# truffleruby_primitives: true

# Copyright (c) 2015, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# The part enclosed between START from MRI/END from MRI is licensed
# under LICENSE.RUBY as it is derived from lib/ruby/stdlib/digest.rb.

require_relative 'digest/version'

module Digest

  # START from MRI
  class ::Digest::Class
    # Creates a digest object and reads a given file, _name_.
    # Optional arguments are passed to the constructor of the digest
    # class.
    #
    #   p Digest::SHA256.file("X11R6.8.2-src.tar.bz2").hexdigest
    #   # => "f02e3c85572dc9ad7cb77c2a638e3be24cc1b5bea9fdbb0b0299c9668475c534"
    def self.file(name, *args)
      new(*args).file(name)
    end

    # Returns the base64 encoded hash value of a given _string_.  The
    # return value is properly padded with '=' and contains no line
    # feeds.
    def self.base64digest(str, *args)
      [digest(str, *args)].pack('m0')
    end
  end

  module Instance
    # Updates the digest with the contents of a given file _name_ and
    # returns self.
    def file(name)
      File.open(name, 'rb') do |f|
        buf = String.new
        while f.read(16384, buf)
          update buf
        end
      end
      self
    end

    # If none is given, returns the resulting hash value of the digest
    # in a base64 encoded form, keeping the digest's state.
    #
    # If a +string+ is given, returns the hash value for the given
    # +string+ in a base64 encoded form, resetting the digest to the
    # initial state before and after the process.
    #
    # In either case, the return value is properly padded with '=' and
    # contains no line feeds.
    def base64digest(str = nil)
      [str ? digest(str) : digest].pack('m0')
    end

    # Returns the resulting hash value and resets the digest to the
    # initial state.
    def base64digest!
      [digest!].pack('m0')
    end
  end
  # END from MRI

  NO_MESSAGE = Object.new

  def Digest.hexencode(message)
    StringValue(message).unpack('H*').first
  end

  module Instance
    def new
      copy = clone
      copy.reset
      copy
    end

    def block_length
      raise RuntimeError, "#{Primitive.class(self).name} does not implement block_length()"
    end

    def update(message)
      raise RuntimeError, "#{Primitive.class(self).name} does not implement update()"
    end
    alias_method :<<, :update

    def reset
      raise RuntimeError, "#{Primitive.class(self).name} does not implement reset()"
    end

    def digest(message = NO_MESSAGE)
      if NO_MESSAGE == message
        clone.__send__ :finish
      else
        reset
        update message
        digested = finish
        reset
        digested
      end
    end

    def hexdigest(message = NO_MESSAGE)
      Digest.hexencode(digest(message))
    end
    alias_method :to_s, :hexdigest

    def digest!
      digested = finish
      reset
      digested
    end

    def finish
      Truffle::Digest.digest @digest
    end

    def hexdigest!
      digested = hexdigest
      reset
      digested
    end

    def digest_length
      StringValue(digest).length
    end

    def size
      digest_length
    end
    alias_method :length, :size

    def ==(other)
      if Primitive.is_a?(other, Digest::Instance)
        self_str = self.digest
        other_str = other.digest
      else
        self_str = self.to_s
        other_str = Truffle::Type.rb_check_convert_type(other, String, :to_str)
        return false if Primitive.nil?(other_str)
      end
      StringValue(self_str) == StringValue(other_str)
    end

    def inspect
      "#<#{Primitive.class(self).name}: #{hexdigest}>"
    end
  end

  class ::Digest::Class
    include Instance

    def self.digest(message, *parameters)
      digest = new(*parameters)
      digest.update message
      digest.digest
    end

    def self.hexdigest(str, *args)
      Digest.hexencode(digest(str, *args))
    end
  end

  class Base < ::Digest::Class
    def block_length
      Truffle::Digest.digest_block_length @digest
    end

    def digest_length
      Truffle::Digest.digest_length @digest
    end

    def reset
      Truffle::Digest.reset @digest
      self
    end

    def update(str)
      str = StringValue(str)
      Truffle::Digest.update(@digest, str)
      self
    end
    alias_method :<<, :update
  end

  class MD5 < Base
    def initialize
      @digest = Truffle::Digest.md5
    end
  end

  class SHA1 < Base
    def initialize
      @digest = Truffle::Digest.sha1
    end
  end

  class SHA256 < Base
    def initialize
      @digest = Truffle::Digest.sha256
    end
  end

  class SHA384 < Base
    def initialize
      @digest = Truffle::Digest.sha384
    end
  end

  class SHA512 < Base
    def initialize
      @digest = Truffle::Digest.sha512
    end
  end

  autoload :SHA2, 'digest/sha2'
end

def Digest(name)
  Digest.const_get(name.to_sym)
end
