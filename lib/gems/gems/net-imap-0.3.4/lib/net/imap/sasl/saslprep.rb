# frozen_string_literal: true

require_relative "saslprep_tables"

module Net::IMAP::SASL

  # SASLprep#saslprep can be used to prepare a string according to [RFC4013].
  #
  # \SASLprep maps characters three ways: to nothing, to space, and Unicode
  # normalization form KC.  \SASLprep prohibits codepoints from nearly all
  # standard StringPrep tables (RFC3454, Appendix "C"), and uses \StringPrep's
  # standard bidirectional characters requirements (Appendix "D").  \SASLprep
  # also uses \StringPrep's definition of "Unassigned" codepoints (Appendix "A").
  module SASLprep

    # Used to short-circuit strings that don't need preparation.
    ASCII_NO_CTRLS = /\A[\x20-\x7e]*\z/u.freeze

    module_function

    # Prepares a UTF-8 +string+ for comparison, using the \SASLprep profile
    # RFC4013 of the StringPrep algorithm RFC3454.
    #
    # By default, prohibited strings will return +nil+.  When +exception+ is
    # +true+, a StringPrepError describing the violation will be raised.
    #
    # When +stored+ is +true+, "unassigned" codepoints will be prohibited. For
    # \StringPrep and the \SASLprep profile, "unassigned" refers to Unicode 3.2,
    # and not later versions.  See RFC3454 §7 for more information.
    #
    def saslprep(str, stored: false, exception: false)
      return str if ASCII_NO_CTRLS.match?(str) # raises on incompatible encoding
      str = str.encode("UTF-8") # also dups (and raises for invalid encoding)
      str.gsub!(MAP_TO_SPACE, " ")
      str.gsub!(MAP_TO_NOTHING, "")
      str.unicode_normalize!(:nfkc)
      # These regexps combine the prohibited and bidirectional checks
      return str unless str.match?(stored ? PROHIBITED_STORED : PROHIBITED)
      return nil unless exception
      # raise helpful errors to indicate *why* it failed:
      tables = stored ? TABLES_PROHIBITED_STORED : TABLES_PROHIBITED
      StringPrep.check_prohibited! str, *tables, bidi: true, profile: "SASLprep"
      raise StringPrep::InvalidStringError.new(
        "unknown error", string: string, profile: "SASLprep"
      )
    rescue ArgumentError, Encoding::CompatibilityError => ex
      if /invalid byte sequence|incompatible encoding/.match? ex.message
        return nil unless exception
        raise StringPrepError.new(ex.message, string: str, profile: "saslprep")
      end
      raise ex
    end

  end
end
