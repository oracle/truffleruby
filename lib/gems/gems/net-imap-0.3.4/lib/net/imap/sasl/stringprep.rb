# frozen_string_literal: true

require_relative "stringprep_tables"

module Net::IMAP::SASL

  # Regexps and utility methods for implementing stringprep profiles.  The
  # \StringPrep algorithm is defined by
  # {RFC-3454}[https://www.rfc-editor.org/rfc/rfc3454.html].  Each
  # codepoint table defined in the RFC-3454 appendices is matched by a Regexp
  # defined in this module.
  #--
  # TODO: generic StringPrep mapping (not needed for SASLprep implementation)
  #++
  module StringPrep

    # Returns a Regexp matching the given +table+ name.
    def self.[](table)
      TABLE_REGEXPS.fetch(table)
    end

    module_function

    # Checks +string+ for any codepoint in +tables+. Raises a
    # ProhibitedCodepoint describing the first matching table.
    #
    # Also checks bidirectional characters, when <tt>bidi: true</tt>, which may
    # raise a BidiStringError.
    #
    # +profile+ is an optional string which will be added to any exception that
    # is raised (it does not affect behavior).
    def check_prohibited!(string, *tables, bidi: false, profile: nil)
      tables = TABLE_TITLES.keys.grep(/^C/) if tables.empty?
      tables |= %w[C.8] if bidi
      table = tables.find {|t| TABLE_REGEXPS[t].match?(string) }
      raise ProhibitedCodepoint.new(
        table, string: string, profile: nil
      ) if table
      check_bidi!(string, profile: profile) if bidi
    end

    # Checks that +string+ obeys all of the "Bidirectional Characters"
    # requirements in RFC-3454, §6:
    #
    # * The characters in \StringPrep\[\"C.8\"] MUST be prohibited
    # * If a string contains any RandALCat character, the string MUST NOT
    #   contain any LCat character.
    # * If a string contains any RandALCat character, a RandALCat
    #   character MUST be the first character of the string, and a
    #   RandALCat character MUST be the last character of the string.
    #
    # This is usually combined with #check_prohibited!, so table "C.8" is only
    # checked when <tt>c_8: true</tt>.
    #
    # Raises either ProhibitedCodepoint or BidiStringError unless all
    # requirements are met.  +profile+ is an optional string which will be
    # added to any exception that is raised (it does not affect behavior).
    def check_bidi!(string, c_8: false, profile: nil)
      check_prohibited!(string, "C.8", profile: profile) if c_8
      if BIDI_FAILS_REQ2.match?(string)
        raise BidiStringError.new(
          BIDI_DESC_REQ2, string: string, profile: profile,
        )
      elsif BIDI_FAILS_REQ3.match?(string)
        raise BidiStringError.new(
          BIDI_DESC_REQ3, string: string, profile: profile,
        )
      end
    end

  end
end
