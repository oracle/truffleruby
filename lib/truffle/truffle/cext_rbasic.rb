# truffleruby_primitives: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::CExt
  def RBASIC(object)
    if Truffle::Type::object_class(object).include? ImmediateValue
      raise TypeError, "immediate values don't include the RBasic struct"
    end
    RBasic.new(object)
  end
end

class Truffle::CExt::RBasic

  def initialize(object)
    @object = object
  end

  USER_FLAGS        = Object.new

  # RUBY_FL* values are from ruby.h
  RUBY_FL_TAINT     = (1<<8)
  RUBY_FL_FREEZE    = (1<<11)

  private

  RUBY_FL_USHIFT = 12
  USER_FLAGS_MASK = (1 << (RUBY_FL_USHIFT + 19)) - (1 << (RUBY_FL_USHIFT))
  private_constant :RUBY_FL_USHIFT, :USER_FLAGS_MASK

  def user_flags
    Primitive.object_hidden_var_get(@object, USER_FLAGS) || 0
  end

  def set_user_flags(flags)
    Primitive.object_hidden_var_set(@object, USER_FLAGS, flags)
  end

  def compute_flags
    flags = 0
    flags |= RUBY_FL_FREEZE if @object.frozen?
    flags |= RUBY_FL_TAINT  if @object.tainted?
    flags | user_flags
  end

  def flag_to_string(flag)
    case flag
    when 1<<5;        'RUBY_FL_WB_PROTECTED (1<<5)'
    when 1<<6;        'RUBY_FL_PROMOTED1 (1<<6)'
    when 1<<5 | 1<<6; 'RUBY_FL_PROMOTED (1<<5 + 1<<6)'
    when 1<<7;        'RUBY_FL_FINALIZE (1<<7)'
    when 1<<8;        'RUBY_FL_FREEZE (1<<8)'
    when 1<<10;       'RUBY_FL_EXIVAR (1<<10)'
    when 1<<11;       'RUBY_FL_TAINT (1<<11)'
    when 1<<2 | 1<<3; 'RUBY_FL_USHIFT (1<<2 + 1<<3)'
    else;             "unknown flag (#{flag})"
    end
  end

  def flags_to_string(flags)
    ushift   = flags[2] == 1 && flags[3] == 1
    promoted = flags[5] == 1 && flags[6] == 1

    decoded = (0...flags.bit_length).reject do |i|
      flags[i] == 0 ||
          ushift && (i == 2 || i == 3) ||
          promoted && (i == 5 || i == 6)
    end

    decoded << (1<<5 | 1<<6) if promoted
    decoded << (1<<2 | 1<<3) if ushift
    decoded.map(&:flag_to_string)
  end

  def set_flags(flags)
    if flags & RUBY_FL_TAINT != 0
      @object.taint
      flags &= ~RUBY_FL_TAINT
    elsif @object.tainted?
      @object.untaint
    end


    set_user_flags(flags & USER_FLAGS_MASK)
    flags &= ~USER_FLAGS_MASK

    # handle last!
    if flags & RUBY_FL_FREEZE != 0
      @object.freeze
      flags &= ~RUBY_FL_FREEZE
    elsif @object.frozen?
      raise ArgumentError, "can't unfreeze object"
    end

    raise ArgumentError "unsupported remaining flags: #{flags_to_string(flags)}" if flags != 0
  end

  def polyglot_has_members?
    true
  end

  def polyglot_members(internal)
    %w[flags]
  end

  def polyglot_read_member(name)
    case name
    when 'flags'
      compute_flags
    else
      raise Truffle::Interop::UnknownIdentifierException
    end
  end

  def polyglot_write_member(name, value)
    raise Truffle::Interop::UnknownIdentifierException unless name == 'flags'
    set_flags value
  end

  def polyglot_remove_member(name)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_invoke_member(name, *args)
    raise Truffle::Interop::UnsupportedMessageException
  end

  def polyglot_member_readable?(name)
    name == 'flags'
  end

  def polyglot_member_modifiable?(name)
    name == 'flags'
  end

  def polyglot_member_removable?(name)
    false
  end

  def polyglot_member_insertable?(name)
    false
  end

  def polyglot_member_invocable?(name)
    false
  end

  def polyglot_member_internal?(name)
    false
  end

  def polyglot_has_member_read_side_effects?(name)
    false
  end

  def polyglot_has_member_write_side_effects?(name)
    true
  end
end
