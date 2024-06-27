# frozen_string_literal: true

module Net
  class IMAP

    # Pluggable authentication mechanisms for protocols which support SASL
    # (Simple Authentication and Security Layer), such as IMAP4, SMTP, LDAP, and
    # XMPP.  {RFC-4422}[https://tools.ietf.org/html/rfc4422] specifies the
    # common SASL framework and the +EXTERNAL+ mechanism, and the
    # {SASL mechanism registry}[https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml]
    # lists the specification for others.
    #
    # "SASL is conceptually a framework that provides an abstraction layer
    # between protocols and mechanisms as illustrated in the following diagram."
    #
    #               SMTP    LDAP    XMPP   Other protocols ...
    #                  \       |    |      /
    #                   \      |    |     /
    #                  SASL abstraction layer
    #                   /      |    |     \
    #                  /       |    |      \
    #           EXTERNAL   GSSAPI  PLAIN   Other mechanisms ...
    #
    module SASL

      # autoloading to avoid loading all of the regexps when they aren't used.

      autoload :StringPrep, File.expand_path("sasl/stringprep", __dir__)
      autoload :SASLprep, File.expand_path("#{__dir__}/sasl/saslprep", __dir__)

      # ArgumentError raised when +string+ is invalid for the stringprep
      # +profile+.
      class StringPrepError < ArgumentError
        attr_reader :string, :profile

        def initialize(*args, string: nil, profile: nil)
          @string  = -string.to_str  unless string.nil?
          @profile = -profile.to_str unless profile.nil?
          super(*args)
        end
      end

      # StringPrepError raised when +string+ contains a codepoint prohibited by
      # +table+.
      class ProhibitedCodepoint < StringPrepError
        attr_reader :table

        def initialize(table, *args, **kwargs)
          @table = -table.to_str
          details = (title = StringPrep::TABLE_TITLES[table]) ?
            "%s [%s]" % [title, table] : table
          message = "String contains a prohibited codepoint: %s" % [details]
          super(message, *args, **kwargs)
        end
      end

      # StringPrepError raised when +string+ contains bidirectional characters
      # which violate the StringPrep requirements.
      class BidiStringError < StringPrepError
      end

      #--
      # We could just extend SASLprep module directly.  It's done this way so
      # SASLprep can be lazily autoloaded.  Most users won't need it.
      #++
      extend self

      # See SASLprep#saslprep.
      def saslprep(string, **opts)
        SASLprep.saslprep(string, **opts)
      end

    end
  end

end

Net::IMAP.extend Net::IMAP::SASL
