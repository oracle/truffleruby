# frozen_string_literal: true

# Registry for SASL authenticators used by Net::IMAP.
module Net::IMAP::Authenticators

  # Adds an authenticator for Net::IMAP#authenticate to use.  +mechanism+ is the
  # {SASL mechanism}[https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml]
  # implemented by +authenticator+ (for instance, <tt>"PLAIN"</tt>).
  #
  # The +authenticator+ must respond to +#new+ (or #call), receiving the
  # authenticator configuration and return a configured authentication session.
  # The authenticator session must respond to +#process+, receiving the server's
  # challenge and returning the client's response.
  #
  # See PlainAuthenticator, XOauth2Authenticator, and DigestMD5Authenticator for
  # examples.
  def add_authenticator(auth_type, authenticator)
    authenticators[auth_type] = authenticator
  end

  # :call-seq:
  #   authenticator(mechanism, ...)                            -> authenticator
  #   authenticator(mech, *creds, **props) {|prop, auth| val } -> authenticator
  #   authenticator(mechanism, authnid, creds, authzid=nil)    -> authenticator
  #   authenticator(mechanism, **properties)                   -> authenticator
  #   authenticator(mechanism) {|propname, authctx| value }    -> authenticator
  #
  # Builds a new authentication session context for +mechanism+.
  #
  # [Note]
  #   This method is intended for internal use by connection protocol code only.
  #   Protocol client users should see refer to their client's documentation,
  #   e.g. Net::IMAP#authenticate for Net::IMAP.
  #
  # The call signatures documented for this method are recommendations for
  # authenticator implementors.  All arguments (other than +mechanism+) are
  # forwarded to the registered authenticator's +#new+ (or +#call+) method, and
  # each authenticator must document its own arguments.
  #
  # The returned object represents a single authentication exchange and <em>must
  # not</em> be reused for multiple authentication attempts.
  def authenticator(mechanism, *authargs, **properties, &callback)
    authenticator = authenticators.fetch(mechanism.upcase) do
      raise ArgumentError, 'unknown auth type - "%s"' % mechanism
    end
    if authenticator.respond_to?(:new)
      authenticator.new(*authargs, **properties, &callback)
    else
      authenticator.call(*authargs, **properties, &callback)
    end
  end

  private

  def authenticators
    @authenticators ||= {}
  end

end

Net::IMAP.extend Net::IMAP::Authenticators

require_relative "authenticators/plain"

require_relative "authenticators/login"
require_relative "authenticators/cram_md5"
require_relative "authenticators/digest_md5"
require_relative "authenticators/xoauth2"
