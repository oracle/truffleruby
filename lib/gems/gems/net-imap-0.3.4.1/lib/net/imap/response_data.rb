# frozen_string_literal: true

module Net
  class IMAP < Protocol

    # Net::IMAP::ContinuationRequest represents command continuation requests.
    #
    # The command continuation request response is indicated by a "+" token
    # instead of a tag.  This form of response indicates that the server is
    # ready to accept the continuation of a command from the client.  The
    # remainder of this response is a line of text.
    #
    class ContinuationRequest < Struct.new(:data, :raw_data)
      ##
      # method: data
      # :call-seq: data -> ResponseText
      #
      # Returns a ResponseText object

      ##
      # method: raw_data
      # :call-seq: raw_data -> string
      #
      # the raw response data
    end

    # Net::IMAP::UntaggedResponse represents untagged responses.
    #
    # Data transmitted by the server to the client and status responses
    # that do not indicate command completion are prefixed with the token
    # <tt>"*"</tt>, and are called untagged responses.
    #
    class UntaggedResponse < Struct.new(:name, :data, :raw_data)
      ##
      # method: name
      # :call-seq: name -> string
      #
      # The uppercase response name, e.g. "FLAGS", "LIST", "FETCH", etc.

      ##
      # method: data
      # :call-seq: data -> object or nil
      #
      # The parsed response data, e.g: an array of flag symbols, an array of
      # capabilities strings, a ResponseText object, a MailboxList object, a
      # FetchData object, a Namespaces object, etc.  The response #name
      # determines what form the data can take.

      ##
      # method: raw_data
      # :call-seq: raw_data -> string
      #
      # The raw response data.
    end

    # Net::IMAP::IgnoredResponse represents intentionally ignored responses.
    #
    # This includes untagged response "NOOP" sent by eg. Zimbra to avoid some
    # clients to close the connection.
    #
    # It matches no IMAP standard.
    #
    class IgnoredResponse < Struct.new(:raw_data)
      ##
      # method: raw_data
      # :call-seq: raw_data -> string
      #
      # The raw response data.
    end

    # Net::IMAP::TaggedResponse represents tagged responses.
    #
    # The server completion result response indicates the success or
    # failure of the operation.  It is tagged with the same tag as the
    # client command which began the operation.
    #
    class TaggedResponse < Struct.new(:tag, :name, :data, :raw_data)
      ##
      # method: tag
      # :call-seq: tag -> string
      #
      # Returns the command tag

      ##
      # method: name
      # :call-seq: name -> string
      #
      # Returns the name, one of "OK", "NO", or "BAD".

      ##
      # method: data
      # :call-seq: data -> ResponseText
      #
      # Returns a ResponseText object

      ##
      # method: raw_data
      # :call-seq: raw_data -> string
      #
      # The raw response data.
    end

    # Net::IMAP::ResponseText represents texts of responses.
    #
    # The text may be prefixed by a ResponseCode.
    #
    # ResponseText is returned from TaggedResponse#data, or from
    # UntaggedResponse#data when the response type is a "condition" ("OK", "NO",
    # "BAD", "PREAUTH", or "BYE").
    class ResponseText < Struct.new(:code, :text)
      ##
      # method: code
      # :call-seq: code -> ResponseCode or nil
      #
      # Returns a ResponseCode, if the response contains one

      ##
      # method: text
      # :call-seq: text -> string
      #
      # Returns the response text, not including any response code
    end

    # Net::IMAP::ResponseCode represents response codes.  Response codes can be
    # retrieved from ResponseText#code and can be included in any "condition"
    # response: any TaggedResponse and UntaggedResponse when the response type
    # is a "condition" ("OK", "NO", "BAD", "PREAUTH", or "BYE").
    #
    # Some response codes come with additional data which will be parsed by
    # Net::IMAP.  Others return +nil+ for #data, but are used as a
    # machine-readable annotation for the human-readable ResponseText#text in
    # the same response.  When Net::IMAP does not know how to parse response
    # code text, #data returns the unparsed string.
    #
    # Untagged response code #data is pushed directly onto Net::IMAP#responses,
    # keyed by #name, unless it is removed by the command that generated it.
    # Use Net::IMAP#add_response_handler to view tagged response codes for
    # command methods that do not return their TaggedResponse.
    #
    # \IMAP extensions may define new codes and the data that comes with them.
    # The IANA {IMAP Response
    # Codes}[https://www.iana.org/assignments/imap-response-codes/imap-response-codes.xhtml]
    # registry has links to specifications for all standard response codes.
    # Response codes are backwards compatible:  Servers are allowed to send new
    # response codes even if the client has not enabled the extension that
    # defines them.  When unknown response code data is encountered, #data
    # will return an unparsed string.
    #
    # See [IMAP4rev1[https://www.rfc-editor.org/rfc/rfc3501]] {§7.1, "Server
    # Responses - Status
    # Responses"}[https://www.rfc-editor.org/rfc/rfc3501#section-7.1] for full
    # definitions of the basic set of IMAP4rev1 response codes:
    # * +ALERT+, the ResponseText#text contains a special alert that MUST be
    #   brought to the user's attention.
    # * +BADCHARSET+, #data will be an array of charset strings, or +nil+.
    # * +CAPABILITY+, #data will be an array of capability strings.
    # * +PARSE+, the ResponseText#text presents an error parsing a message's
    #   \[RFC5322] or [MIME-IMB] headers.
    # * +PERMANENTFLAGS+, followed by an array of flags.  System flags will be
    #   symbols, and keyword flags will be strings.  See
    #   rdoc-ref:Net::IMAP@System+flags
    # * +READ-ONLY+, the mailbox was selected read-only, or changed to read-only
    # * +READ-WRITE+, the mailbox was selected read-write, or changed to
    #   read-write
    # * +TRYCREATE+, when #append or #copy fail because the target mailbox
    #   doesn't exist.
    # * +UIDNEXT+, #data is an Integer, the next UID value of the mailbox.  See
    #   [{IMAP4rev1}[https://www.rfc-editor.org/rfc/rfc3501]],
    #   {§2.3.1.1, "Unique Identifier (UID) Message
    #   Attribute}[https://www.rfc-editor.org/rfc/rfc3501#section-2.3.1.1].
    # * +UIDVALIDITY+, #data is an Integer, the UID validity value of the
    #   mailbox  See [{IMAP4rev1}[https://www.rfc-editor.org/rfc/rfc3501]],
    #   {§2.3.1.1, "Unique Identifier (UID) Message
    #   Attribute}[https://www.rfc-editor.org/rfc/rfc3501#section-2.3.1.1].
    # * +UNSEEN+, #data is an Integer, the number of messages which do not have
    #   the <tt>\Seen</tt> flag set.
    #
    # See RFC5530[https://www.rfc-editor.org/rfc/rfc5530], "IMAP Response
    # Codes" for the definition of the following response codes, which are all
    # machine-readable annotations for the human-readable ResponseText#text, and
    # have +nil+ #data of their own:
    # * +UNAVAILABLE+
    # * +AUTHENTICATIONFAILED+
    # * +AUTHORIZATIONFAILED+
    # * +EXPIRED+
    # * +PRIVACYREQUIRED+
    # * +CONTACTADMIN+
    # * +NOPERM+
    # * +INUSE+
    # * +EXPUNGEISSUED+
    # * +CORRUPTION+
    # * +SERVERBUG+
    # * +CLIENTBUG+
    # * +CANNOT+
    # * +LIMIT+
    # * +OVERQUOTA+
    # * +ALREADYEXISTS+
    # * +NONEXISTENT+
    #
    class ResponseCode < Struct.new(:name, :data)
      ##
      # method: name
      # :call-seq: name -> string
      #
      # Returns the response code name, such as "ALERT", "PERMANENTFLAGS", or
      # "UIDVALIDITY".

      ##
      # method: data
      # :call-seq: data -> object or nil
      #
      # Returns the parsed response code data, e.g: an array of capabilities
      # strings, an array of character set strings, a list of permanent flags,
      # an Integer, etc.  The response #code determines what form the response
      # code data can take.
    end

    # Net::IMAP::UIDPlusData represents the ResponseCode#data that accompanies
    # the +APPENDUID+ and +COPYUID+ response codes.
    #
    # See [[UIDPLUS[https://www.rfc-editor.org/rfc/rfc4315.html]].
    #
    # ==== Capability requirement
    #
    # The +UIDPLUS+ capability[rdoc-ref:Net::IMAP#capability] must be supported.
    # A server that supports +UIDPLUS+ should send a UIDPlusData object inside
    # every TaggedResponse returned by the append[rdoc-ref:Net::IMAP#append],
    # copy[rdoc-ref:Net::IMAP#copy], move[rdoc-ref:Net::IMAP#move], {uid
    # copy}[rdoc-ref:Net::IMAP#uid_copy], and {uid
    # move}[rdoc-ref:Net::IMAP#uid_move] commands---unless the destination
    # mailbox reports +UIDNOTSTICKY+.
    #
    #--
    # TODO: support MULTIAPPEND
    #++
    #
    class UIDPlusData < Struct.new(:uidvalidity, :source_uids, :assigned_uids)
      ##
      # method: uidvalidity
      # :call-seq: uidvalidity -> nonzero uint32
      #
      # The UIDVALIDITY of the destination mailbox.

      ##
      # method: source_uids
      # :call-seq: source_uids -> nil or an array of nonzero uint32
      #
      # The UIDs of the copied or moved messages.
      #
      # Note:: Returns +nil+ for Net::IMAP#append.

      ##
      # method: assigned_uids
      # :call-seq: assigned_uids -> an array of nonzero uint32
      #
      # The newly assigned UIDs of the copied, moved, or appended messages.
      #
      # Note:: This always returns an array, even when it contains only one UID.

      ##
      # :call-seq: uid_mapping -> nil or a hash
      #
      # Returns a hash mapping each source UID to the newly assigned destination
      # UID.
      #
      # Note:: Returns +nil+ for Net::IMAP#append.
      def uid_mapping
        source_uids&.zip(assigned_uids)&.to_h
      end
    end

    # Net::IMAP::MailboxList represents contents of the LIST response,
    # representing a single mailbox path.
    #
    # Net::IMAP#list returns an array of MailboxList objects.
    #
    class MailboxList < Struct.new(:attr, :delim, :name)
      ##
      # method: attr
      # :call-seq: attr -> array of Symbols
      #
      # Returns the name attributes. Each name attribute is a symbol capitalized
      # by String#capitalize, such as :Noselect (not :NoSelect).  For the
      # semantics of each attribute, see:
      # * rdoc-ref:Net::IMAP@Basic+Mailbox+Attributes
      # * rdoc-ref:Net::IMAP@Mailbox+role+Attributes
      # * Net::IMAP@SPECIAL-USE
      # * The IANA {IMAP Mailbox Name Attributes
      #   registry}[https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml]

      ##
      # method: delim
      # :call-seq: delim -> single character string
      #
      # Returns the hierarchy delimiter for the mailbox path.

      ##
      # method: name
      # :call-seq: name -> string
      #
      # Returns the mailbox name.
    end

    # Net::IMAP::MailboxQuota represents contents of GETQUOTA response.
    # This object can also be a response to GETQUOTAROOT.  In the syntax
    # specification below, the delimiter used with the "#" construct is a
    # single space (SPACE).
    #
    # Net:IMAP#getquota returns an array of MailboxQuota objects.
    #
    # Net::IMAP#getquotaroot returns an array containing both MailboxQuotaRoot
    # and MailboxQuota objects.
    #
    class MailboxQuota < Struct.new(:mailbox, :usage, :quota)
      ##
      # method: mailbox
      # :call-seq: mailbox -> string
      #
      # The mailbox with the associated quota.

      ##
      # method: usage
      # :call-seq: usage -> Integer
      #
      # Current storage usage of the mailbox.

      ##
      # method: quota
      # :call-seq: quota -> Integer
      #
      # Quota limit imposed on the mailbox.
      #
    end

    # Net::IMAP::MailboxQuotaRoot represents part of the GETQUOTAROOT
    # response. (GETQUOTAROOT can also return Net::IMAP::MailboxQuota.)
    #
    # Net::IMAP#getquotaroot returns an array containing both MailboxQuotaRoot
    # and MailboxQuota objects.
    #
    class MailboxQuotaRoot < Struct.new(:mailbox, :quotaroots)
      ##
      # method: mailbox
      # :call-seq: mailbox -> string
      #
      # The mailbox with the associated quota.

      ##
      # method: mailbox
      # :call-seq: quotaroots -> array of strings
      #
      # Zero or more quotaroots that affect the quota on the specified mailbox.
    end

    # Net::IMAP::MailboxACLItem represents the response from GETACL.
    #
    # Net::IMAP#getacl returns an array of MailboxACLItem objects.
    #
    # ==== Required capability
    # +ACL+ - described in [ACL[https://tools.ietf.org/html/rfc4314]]
    class MailboxACLItem < Struct.new(:user, :rights, :mailbox)
      ##
      # method: mailbox
      # :call-seq: mailbox -> string
      #
      # The mailbox to which the indicated #user has the specified #rights.

      ##
      # method: user
      # :call-seq: user -> string
      #
      # Login name that has certain #rights to the #mailbox that was specified
      # with the getacl command.

      ##
      # method: rights
      # :call-seq: rights -> string
      #
      # The access rights the indicated #user has to the #mailbox.
    end

    # Net::IMAP::Namespace represents a single namespace contained inside a
    # NAMESPACE response.
    #
    # Returned by Net::IMAP#namespace, contained inside a Namespaces object.
    #
    class Namespace < Struct.new(:prefix, :delim, :extensions)
      ##
      # method: prefix
      # :call-seq: prefix -> string
      #
      # Returns the namespace prefix string.

      ##
      # method: delim
      # :call-seq: delim -> single character string or nil
      #
      # Returns a hierarchy delimiter character, if it exists.

      ##
      # method: extensions
      # :call-seq: extensions -> Hash[String, Array[String]]
      #
      # A hash of parameters mapped to arrays of strings, for extensibility.
      # Extension parameter semantics would be defined by the extension.
    end

    # Net::IMAP::Namespaces represents a +NAMESPACE+ server response, which
    # contains lists of #personal, #shared, and #other namespaces.
    #
    # Net::IMAP#namespace returns a Namespaces object.
    #
    class Namespaces < Struct.new(:personal, :other, :shared)
      ##
      # method: personal
      # :call-seq: personal -> array of Namespace
      #
      # Returns an array of Personal Namespace objects.

      ##
      # method: other
      # :call-seq: other -> array of Namespace
      #
      # Returns an array of Other Users' Namespace objects.

      ##
      # method: shared
      # :call-seq: shared -> array of Namespace
      #
      # Returns an array of Shared Namespace objects.
    end

    # Net::IMAP::StatusData represents the contents of the STATUS response.
    #
    # Net::IMAP#status returns the contents of #attr.
    class StatusData < Struct.new(:mailbox, :attr)
      ##
      # method: mailbox
      # :call-seq: mailbox -> string
      #
      # The mailbox name.

      ##
      # method: attr
      # :call-seq: attr -> Hash[String, Integer]
      #
      # A hash.  Each key is one of "MESSAGES", "RECENT", "UIDNEXT",
      # "UIDVALIDITY", "UNSEEN". Each value is a number.
    end

    # Net::IMAP::FetchData represents the contents of a FETCH response.
    #
    # Net::IMAP#fetch and Net::IMAP#uid_fetch both return an array of
    # FetchData objects.
    #
    # === Fetch attributes
    #
    #--
    # TODO: merge branch with accessor methods for each type of attr.  Then
    # move nearly all of the +attr+ documentation onto the appropriate
    # accessor methods.
    #++
    #
    # Each key of the #attr hash is the data item name for the fetched value.
    # Each data item represents a message attribute, part of one, or an
    # interpretation of one.  #seqno is not a message attribute.  Most message
    # attributes are static and must never change for a given <tt>[server,
    # account, mailbox, UIDVALIDITY, UID]</tt> tuple.  A few message attributes
    # can be dynamically changed, e.g. using the {STORE
    # command}[rdoc-ref:Net::IMAP#store].
    #
    # See {[IMAP4rev1] §7.4.2}[https://www.rfc-editor.org/rfc/rfc3501.html#section-7.4.2]
    # and {[IMAP4rev2] §7.5.2}[https://www.rfc-editor.org/rfc/rfc9051.html#section-7.5.2]
    # for full description of the standard fetch response data items, and
    # Net::IMAP@Message+envelope+and+body+structure for other relevant RFCs.
    #
    # ==== Static fetch data items
    #
    # The static data items
    # defined by [IMAP4rev1[https://www.rfc-editor.org/rfc/rfc3501.html]] are:
    #
    # [<tt>"UID"</tt>]
    #   A number expressing the unique identifier of the message.
    #
    # [<tt>"BODY[]"</tt>, <tt>"BODY[]<#{offset}>"</tt>]
    #   The [RFC5322[https://tools.ietf.org/html/rfc5322]] expression of the
    #   entire message, as a string.
    #
    #   If +offset+ is specified, this returned string is a substring of the
    #   entire contents, starting at that origin octet.  This means that
    #   <tt>BODY[]<0></tt> MAY be truncated, but <tt>BODY[]</tt> is NEVER
    #   truncated.
    #
    #   <em>Messages can be parsed using the "mail" gem.</em>
    #
    #   [Note]
    #     When fetching <tt>BODY.PEEK[#{specifier}]</tt>, the data will be
    #     returned in <tt>BODY[#{specifier}]</tt>, without the +PEEK+.  This is
    #     true for all of the <tt>BODY[...]</tt> attribute forms.
    #
    # [<tt>"BODY[HEADER]"</tt>, <tt>"BODY[HEADER]<#{offset}>"</tt>]
    #   The [RFC5322[https://tools.ietf.org/html/rfc5322]] header of the
    #   message.
    #
    #   <em>Message headers can be parsed using the "mail" gem.</em>
    #
    # [<tt>"BODY[HEADER.FIELDS (#{fields.join(" ")})]"</tt>,]
    # [<tt>"BODY[HEADER.FIELDS (#{fields.join(" ")})]<#{offset}>"</tt>]
    #   When field names are given, the subset contains only the header fields
    #   that matches one of the names in the list.  The field names are based
    #   on what was requested, not on what was returned.
    #
    # [<tt>"BODY[HEADER.FIELDS.NOT (#{fields.join(" ")})]"</tt>,]
    # [<tt>"BODY[HEADER.FIELDS.NOT (#{fields.join(" ")})]<#{offset}>"</tt>]
    #   When the <tt>HEADER.FIELDS.NOT</tt> is used, the subset is all of the
    #   fields that <em>do not</em> match any names in the list.
    #
    # [<tt>"BODY[TEXT]"</tt>, <tt>"BODY[TEXT]<#{offset}>"</tt>]
    #   The text body of the message, omitting
    #   the [RFC5322[https://tools.ietf.org/html/rfc5322]] header.
    #
    # [<tt>"BODY[#{part}]"</tt>, <tt>"BODY[#{part}]<#{offset}>"</tt>]
    #   The text of a particular body section, if it was fetched.
    #
    #   Multiple part specifiers will be joined with <tt>"."</tt>.  Numeric
    #   part specifiers refer to the MIME part number, counting up from +1+.
    #   Messages that don't use MIME, or MIME messages that are not multipart
    #   and don't hold an encapsulated message, only have a part +1+.
    #
    #   8-bit textual data is permitted if
    #   a [CHARSET[https://tools.ietf.org/html/rfc2978]] identifier is part of
    #   the body parameter parenthesized list for this section.  See
    #   BodyTypeBasic.
    #
    #   MESSAGE/RFC822 or MESSAGE/GLOBAL message, or a subset of the header, if
    #   it was fetched.
    #
    # [<tt>"BODY[#{part}.HEADER]"</tt>,]
    # [<tt>"BODY[#{part}.HEADER]<#{offset}>"</tt>,]
    # [<tt>"BODY[#{part}.HEADER.FIELDS.NOT (#{fields.join(" ")})]"</tt>,]
    # [<tt>"BODY[#{part}.HEADER.FIELDS.NOT (#{fields.join(" ")})]<#{offset}>"</tt>,]
    # [<tt>"BODY[#{part}.TEXT]"</tt>,]
    # [<tt>"BODY[#{part}.TEXT]<#{offset}>"</tt>,]
    # [<tt>"BODY[#{part}.MIME]"</tt>,]
    # [<tt>"BODY[#{part}.MIME]<#{offset}>"</tt>]
    #   +HEADER+, <tt>HEADER.FIELDS</tt>, <tt>HEADER.FIELDS.NOT</tt>, and
    #   <tt>TEXT</tt> can be prefixed by numeric part specifiers, if it refers
    #   to a part of type <tt>message/rfc822</tt> or <tt>message/global</tt>.
    #
    #   +MIME+ refers to the [MIME-IMB[https://tools.ietf.org/html/rfc2045]]
    #   header for this part.
    #
    # [<tt>"BODY"</tt>]
    #   A form of +BODYSTRUCTURE+, without any extension data.
    #
    # [<tt>"BODYSTRUCTURE"</tt>]
    #   Returns a BodyStructure object that describes
    #   the [MIME-IMB[https://tools.ietf.org/html/rfc2045]] body structure of
    #   a message, if it was fetched.
    #
    # [<tt>"ENVELOPE"</tt>]
    #   An Envelope object that describes the envelope structure of a message.
    #   See the documentation for Envelope for a description of the envelope
    #   structure attributes.
    #
    # [<tt>"INTERNALDATE"</tt>]
    #   The internal date and time of the message on the server.  This is not
    #   the date and time in
    #   the [RFC5322[https://tools.ietf.org/html/rfc5322]] header, but rather
    #   a date and time which reflects when the message was received.
    #
    # [<tt>"RFC822.SIZE"</tt>]
    #   A number expressing the [RFC5322[https://tools.ietf.org/html/rfc5322]]
    #   size of the message.
    #
    #   [Note]
    #     \IMAP was originally developed for the older RFC-822 standard, and
    #     as a consequence several fetch items in \IMAP incorporate "RFC822"
    #     in their name.  With the exception of +RFC822.SIZE+, there are more
    #     modern replacements; for example, the modern version of
    #     +RFC822.HEADER+ is <tt>BODY.PEEK[HEADER]</tt>.  In all cases,
    #     "RFC822" should be interpreted as a reference to the
    #     updated [RFC5322[https://tools.ietf.org/html/rfc5322]] standard.
    #
    # [<tt>"RFC822"</tt>]
    #   Semantically equivalent to <tt>BODY[]</tt>.
    # [<tt>"RFC822.HEADER"</tt>]
    #   Semantically equivalent to <tt>BODY[HEADER]</tt>.
    # [<tt>"RFC822.TEXT"</tt>]
    #   Semantically equivalent to <tt>BODY[TEXT]</tt>.
    #
    # [Note:]
    #   >>>
    #     Additional static fields are defined in \IMAP extensions and
    #     [IMAP4rev2[https://www.rfc-editor.org/rfc/rfc9051.html]], but
    #     Net::IMAP can't parse them yet.
    #
    #--
    # <tt>"BINARY[#{section_binary}]<#{offset}>"</tt>:: TODO...
    # <tt>"BINARY.SIZE[#{sectionbinary}]"</tt>::        TODO...
    # <tt>"EMAILID"</tt>::                              TODO...
    # <tt>"THREADID"</tt>::                             TODO...
    # <tt>"SAVEDATE"</tt>::                             TODO...
    #++
    #
    # ==== Dynamic message attributes
    # The only dynamic item defined
    # by [{IMAP4rev1}[https://www.rfc-editor.org/rfc/rfc3501.html]] is:
    # [<tt>"FLAGS"</tt>]
    #   An array of flags that are set for this message.  System flags are
    #   symbols that have been capitalized by String#capitalize.  Keyword
    #   flags are strings and their case is not changed.
    #
    # \IMAP extensions define new dynamic fields, e.g.:
    #
    # [<tt>"MODSEQ"</tt>]
    #   The modification sequence number associated with this IMAP message.
    #
    #   Requires the [CONDSTORE[https://tools.ietf.org/html/rfc7162]]
    #   server {capability}[rdoc-ref:Net::IMAP#capability].
    #
    # [Note:]
    #   >>>
    #     Additional dynamic fields are defined in \IMAP extensions, but
    #     Net::IMAP can't parse them yet.
    #
    #--
    # <tt>"ANNOTATE"</tt>:: TODO...
    # <tt>"PREVIEW"</tt>::  TODO...
    #++
    #
    class FetchData < Struct.new(:seqno, :attr)
      ##
      # method: seqno
      # :call-seq: seqno -> Integer
      #
      # The message sequence number.
      #
      # [Note]
      #   This is never the unique identifier (UID), not even for the
      #   Net::IMAP#uid_fetch result.  If it was returned, the UID is available
      #   from <tt>attr["UID"]</tt>.

      ##
      # method: attr
      # :call-seq: attr -> hash
      #
      # A hash.  Each key is specifies a message attribute, and the value is the
      # corresponding data item.
      #
      # See rdoc-ref:FetchData@Fetch+attributes for descriptions of possible
      # values.
    end

    # Net::IMAP::Envelope represents envelope structures of messages.
    #
    # [Note]
    #   When the #sender and #reply_to fields are absent or empty, they will
    #   return the same value as #from.  Also, fields may return values that are
    #   invalid for well-formed [RFC5322[https://tools.ietf.org/html/rfc5322]]
    #   messages when the message is malformed or a draft message.
    #
    # See [{IMAP4rev1 §7.4.2}[https://www.rfc-editor.org/rfc/rfc3501.html#section-7.4.2]]
    # and [{IMAP4rev2 §7.5.2}[https://www.rfc-editor.org/rfc/rfc9051.html#section-7.5.2]]
    # for full description of the envelope fields, and
    # Net::IMAP@Message+envelope+and+body+structure for other relevant RFCs.
    #
    class Envelope < Struct.new(:date, :subject, :from, :sender, :reply_to,
                                :to, :cc, :bcc, :in_reply_to, :message_id)
      ##
      # method: date
      # call-seq: date -> string
      #
      # Returns a string that represents the +Date+ header.
      #
      # [Note]
      #   For a well-formed [RFC5322[https://tools.ietf.org/html/rfc5322]]
      #   message, the #date field must not be +nil+.  However it can be +nil+
      #   for a malformed or draft message.

      ##
      # method: subject
      # call-seq: subject -> string or nil
      #
      # Returns a string that represents the +Subject+ header, if it is present.
      #
      # [Note]
      #   Servers should return +nil+ when the header is absent and an empty
      #   string when it is present but empty.  Some servers may return a +nil+
      #   envelope member in the "present but empty" case.  Clients should treat
      #   +nil+ and empty string as identical.

      ##
      # method: from
      # call-seq: from -> array of Net::IMAP::Address or nil
      #
      # Returns an array of Address that represents the +From+ header.
      #
      # If the +From+ header is absent, or is present but empty, the server
      # returns +nil+ for this envelope field.
      #
      # [Note]
      #   For a well-formed [RFC5322[https://tools.ietf.org/html/rfc5322]]
      #   message, the #from field must not be +nil+.  However it can be +nil+
      #   for a malformed or draft message.

      ##
      # method: sender
      # call-seq: sender -> array of Net::IMAP::Address or nil
      #
      # Returns an array of Address that represents the +Sender+ header.
      #
      # [Note]
      #   If the <tt>Sender</tt> header is absent, or is present but empty, the
      #   server sets this field to be the same value as #from.  Therefore, in a
      #   well-formed [RFC5322[https://tools.ietf.org/html/rfc5322]] message,
      #   the #sender envelope field must not be +nil+.  However it can be
      #   +nil+ for a malformed or draft message.

      ##
      # method: reply_to
      # call-seq: reply_to -> array of Net::IMAP::Address or nil
      #
      # Returns an array of Address that represents the <tt>Reply-To</tt>
      # header.
      #
      # [Note]
      #   If the <tt>Reply-To</tt> header is absent, or is present but empty,
      #   the server sets this field to be the same value as #from.  Therefore,
      #   in a well-formed [RFC5322[https://tools.ietf.org/html/rfc5322]]
      #   message, the #reply_to envelope field must not be +nil+.  However it
      #   can be +nil+ for a malformed or draft message.

      ##
      # method: to
      # call-seq: to -> array of Net::IMAP::Address
      #
      # Returns an array of Address that represents the +To+ header.

      ##
      # method: cc
      # call-seq: cc -> array of Net::IMAP::Address
      #
      # Returns an array of Address that represents the +Cc+ header.

      ##
      # method: bcc
      # call-seq: bcc -> array of Net::IMAP::Address
      #
      # Returns an array of Address that represents the +Bcc+ header.

      ##
      # method: in_reply_to
      # call-seq: in_reply_to -> string
      #
      # Returns a string that represents the <tt>In-Reply-To</tt> header.
      #
      # [Note]
      #   For a well-formed [RFC5322[https://tools.ietf.org/html/rfc5322]]
      #   message, the #in_reply_to field, if present, must not be empty.  But
      #   it can still return an empty string for malformed messages.
      #
      #   Servers should return +nil+ when the header is absent and an empty
      #   string when it is present but empty.  Some servers may return a +nil+
      #   envelope member in the "present but empty" case.  Clients should treat
      #   +nil+ and empty string as identical.

      ##
      # method: message_id
      # call-seq: message_id -> string
      #
      # Returns a string that represents the <tt>Message-ID</tt>.
      #
      # [Note]
      #   For a well-formed [RFC5322[https://tools.ietf.org/html/rfc5322]]
      #   message, the #message_id field, if present, must not be empty.  But it
      #   can still return an empty string for malformed messages.
      #
      #   Servers should return +nil+ when the header is absent and an empty
      #   string when it is present but empty.  Some servers may return a +nil+
      #   envelope member in the "present but empty" case.  Clients should treat
      #   +nil+ and empty string as identical.
    end

    # Net::IMAP::Address represents an electronic mail address, which has been
    # parsed into its component parts by the server.  Address objects are
    # returned within Envelope fields.
    #
    # === Group syntax
    #
    # When the #host field is +nil+, this is a special form of address structure
    # that indicates the [RFC5322[https://tools.ietf.org/html/rfc5322]] group
    # syntax.  If the #mailbox name field is also +nil+, this is an end-of-group
    # marker (semicolon in RFC-822 syntax).  If the #mailbox name field is
    # non-+NIL+, this is the start of a group marker, and the mailbox #name
    # field holds the group name phrase.
    class Address < Struct.new(:name, :route, :mailbox, :host)
      ##
      # method: name
      # :call-seq: name -> string or nil
      #
      # Returns the [RFC5322[https://tools.ietf.org/html/rfc5322]] address
      # +display-name+ (or the mailbox +phrase+ in the RFC-822 grammar).

      ##
      # method: route
      # :call-seq: route -> string or nil
      #
      # Returns the route from RFC-822 route-addr.
      #
      # Note:: Generating this obsolete route addressing syntax is not allowed
      #        by [RFC5322[https://tools.ietf.org/html/rfc5322]].  However,
      #        addresses with this syntax must still be accepted and parsed.

      ##
      # method: mailbox
      # :call-seq: mailbox -> string or nil
      #
      # Returns the [RFC5322[https://tools.ietf.org/html/rfc5322]] address
      # +local-part+, if #host is not +nil+.
      #
      # When #host is +nil+, this returns
      # an [RFC5322[https://tools.ietf.org/html/rfc5322]] group name and a +nil+
      # mailbox indicates the end of a group.

      ##
      # method: host
      # :call-seq: host -> string or nil
      #
      # Returns the [RFC5322[https://tools.ietf.org/html/rfc5322]] addr-spec
      # +domain+ name.
      #
      # +nil+ indicates [RFC5322[https://tools.ietf.org/html/rfc5322]] group
      # syntax.
    end

    # Net::IMAP::ContentDisposition represents Content-Disposition fields.
    #
    class ContentDisposition < Struct.new(:dsp_type, :param)
      ##
      # method: dsp_type
      # :call-seq: dsp_type -> string
      #
      # Returns the content disposition type, as defined by
      # [DISPOSITION[https://tools.ietf.org/html/rfc2183]].

      ##
      # method: param
      # :call-seq: param -> hash
      #
      # Returns a hash representing parameters of the Content-Disposition
      # field, as defined by [DISPOSITION[https://tools.ietf.org/html/rfc2183]].
    end

    # Net::IMAP::ThreadMember represents a thread-node returned
    # by Net::IMAP#thread.
    #
    class ThreadMember < Struct.new(:seqno, :children)
      ##
      # method: seqno
      # :call-seq: seqno -> Integer
      #
      # The message sequence number.

      ##
      # method: children
      # :call-seq: children -> array of ThreadMember
      #
      # An array of Net::IMAP::ThreadMember objects for mail items that are
      # children of this in the thread.
    end

    # Net::IMAP::BodyStructure is included by all of the structs that can be
    # returned from a <tt>"BODYSTRUCTURE"</tt> or <tt>"BODY"</tt>
    # FetchData#attr value.  Although these classes don't share a base class,
    # this module can be used to pattern match all of them.
    #
    # See {[IMAP4rev1] §7.4.2}[https://www.rfc-editor.org/rfc/rfc3501.html#section-7.4.2]
    # and {[IMAP4rev2] §7.5.2}[https://www.rfc-editor.org/rfc/rfc9051.html#section-7.5.2-4.9]
    # for full description of all +BODYSTRUCTURE+ fields, and also
    # Net::IMAP@Message+envelope+and+body+structure for other relevant RFCs.
    #
    # === Classes that include BodyStructure
    # BodyTypeBasic:: Represents any message parts that are not handled by
    #                 BodyTypeText, BodyTypeMessage, or BodyTypeMultipart.
    # BodyTypeText:: Used by <tt>text/*</tt> parts.  Contains all of the
    #                BodyTypeBasic fields.
    # BodyTypeMessage:: Used by <tt>message/rfc822</tt> and
    #                   <tt>message/global</tt> parts.  Contains all of the
    #                   BodyTypeBasic fields.  Other <tt>message/*</tt> types
    #                   should use BodyTypeBasic.
    # BodyTypeMultipart:: for <tt>multipart/*</tt> parts
    #
    # ==== Deprecated BodyStructure classes
    # The following classes represent invalid server responses or parser bugs:
    # BodyTypeExtension:: parser bug: used for <tt>message/*</tt> where
    #                     BodyTypeBasic should have been used.
    # BodyTypeAttachment:: server bug: some servers sometimes return the
    #                      "Content-Disposition: attachment" data where the
    #                      entire body structure for a message part is expected.
    module BodyStructure
    end

    # Net::IMAP::BodyTypeBasic represents basic body structures of messages and
    # message parts, unless they have a <tt>Content-Type</tt> that is handled by
    # BodyTypeText, BodyTypeMessage, or BodyTypeMultipart.
    #
    # See {[IMAP4rev1] §7.4.2}[https://www.rfc-editor.org/rfc/rfc3501.html#section-7.4.2]
    # and {[IMAP4rev2] §7.5.2}[https://www.rfc-editor.org/rfc/rfc9051.html#section-7.5.2-4.9]
    # for full description of all +BODYSTRUCTURE+ fields, and also
    # Net::IMAP@Message+envelope+and+body+structure for other relevant RFCs.
    #
    class BodyTypeBasic < Struct.new(:media_type, :subtype,
                                     :param, :content_id,
                                     :description, :encoding, :size,
                                     :md5, :disposition, :language,
                                     :extension)
      include BodyStructure

      ##
      # method: media_type
      # :call-seq: media_type -> string
      #
      # The top-level media type as defined in
      # [MIME-IMB[https://tools.ietf.org/html/rfc2045]].

      ##
      # method: subtype
      # :call-seq: subtype -> string
      #
      # The media subtype name as defined in
      # [MIME-IMB[https://tools.ietf.org/html/rfc2045]].

      ##
      # method: param
      # :call-seq: param -> string
      #
      # Returns a hash that represents parameters as defined in
      # [MIME-IMB[https://tools.ietf.org/html/rfc2045]].

      ##
      # method: content_id
      # :call-seq: content_id -> string
      #
      # Returns a string giving the content id as defined
      # in [MIME-IMB[https://tools.ietf.org/html/rfc2045]]
      # {§7}[https://tools.ietf.org/html/rfc2045#section-7].

      ##
      # method: description
      # :call-seq: description -> string
      #
      # Returns a string giving the content description as defined
      # in [MIME-IMB[https://tools.ietf.org/html/rfc2045]]
      # {§8}[https://tools.ietf.org/html/rfc2045#section-8].

      ##
      # method: encoding
      # :call-seq: encoding -> string
      #
      # Returns a string giving the content transfer encoding as defined
      # in [MIME-IMB[https://tools.ietf.org/html/rfc2045]]
      # {§6}[https://tools.ietf.org/html/rfc2045#section-6].

      ##
      # method: size
      # :call-seq: size -> integer
      #
      # Returns a number giving the size of the body in octets.

      ##
      # method: md5
      # :call-seq: md5 -> string
      #
      # Returns a string giving the body MD5 value as defined in
      # [MD5[https://tools.ietf.org/html/rfc1864]].

      ##
      # method: disposition
      # :call-seq: disposition -> ContentDisposition
      #
      # Returns a ContentDisposition object giving the content
      # disposition, as defined by
      # [DISPOSITION[https://tools.ietf.org/html/rfc2183]].

      ##
      # method: language
      # :call-seq: language -> string
      #
      # Returns a string or an array of strings giving the body
      # language value as defined in
      # [LANGUAGE-TAGS[https://www.rfc-editor.org/info/rfc3282]].

      #--
      ##
      # method: location
      # :call-seq: location -> string
      #
      # A string list giving the body content URI as defined in
      # [LOCATION[https://www.rfc-editor.org/info/rfc2557]].
      #++

      ##
      # method: extension
      # :call-seq: extension -> string
      #
      # Returns extension data.  The +BODYSTRUCTURE+ fetch attribute
      # contains extension data, but +BODY+ does not.

      ##
      # :call-seq: multipart? -> false
      #
      # BodyTypeBasic is not used for multipart MIME parts.
      def multipart?
        return false
      end

      # :call-seq: media_subtype -> subtype
      #
      # >>>
      #   [Obsolete]
      #     Use +subtype+ instead.  Calling this will generate a warning message
      #     to +stderr+, then return the value of +subtype+.
      #--
      # TODO: why not just keep this as an alias?  Would "media_subtype" be used
      # for something else?
      #++
      def media_subtype
        warn("media_subtype is obsolete, use subtype instead.\n", uplevel: 1)
        return subtype
      end
    end

    # Net::IMAP::BodyTypeText represents the body structures of messages and
    # message parts, when <tt>Content-Type</tt> is <tt>text/*</tt>.
    #
    # BodyTypeText contains all of the fields of BodyTypeBasic.  See
    # BodyTypeBasic for documentation of the following:
    # * {media_type}[rdoc-ref:BodyTypeBasic#media_type]
    # * subtype[rdoc-ref:BodyTypeBasic#subtype]
    # * param[rdoc-ref:BodyTypeBasic#param]
    # * {content_id}[rdoc-ref:BodyTypeBasic#content_id]
    # * description[rdoc-ref:BodyTypeBasic#description]
    # * encoding[rdoc-ref:BodyTypeBasic#encoding]
    # * size[rdoc-ref:BodyTypeBasic#size]
    #
    class BodyTypeText < Struct.new(:media_type, :subtype,
                                    :param, :content_id,
                                    :description, :encoding, :size,
                                    :lines,
                                    :md5, :disposition, :language,
                                    :extension)
      include BodyStructure

      ##
      # method: lines
      # :call-seq: lines -> Integer
      #
      # Returns the size of the body in text lines.

      ##
      # :call-seq: multipart? -> false
      #
      # BodyTypeText is not used for multipart MIME parts.
      def multipart?
        return false
      end

      # Obsolete: use +subtype+ instead.  Calling this will
      # generate a warning message to +stderr+, then return
      # the value of +subtype+.
      def media_subtype
        warn("media_subtype is obsolete, use subtype instead.\n", uplevel: 1)
        return subtype
      end
    end

    # Net::IMAP::BodyTypeMessage represents the body structures of messages and
    # message parts, when <tt>Content-Type</tt> is <tt>message/rfc822</tt> or
    # <tt>message/global</tt>.
    #
    # BodyTypeMessage contains all of the fields of BodyTypeBasic.  See
    # BodyTypeBasic for documentation of the following fields:
    # * {media_type}[rdoc-ref:BodyTypeBasic#media_type]
    # * subtype[rdoc-ref:BodyTypeBasic#subtype]
    # * param[rdoc-ref:BodyTypeBasic#param]
    # * {content_id}[rdoc-ref:BodyTypeBasic#content_id]
    # * description[rdoc-ref:BodyTypeBasic#description]
    # * encoding[rdoc-ref:BodyTypeBasic#encoding]
    # * size[rdoc-ref:BodyTypeBasic#size]
    #
    class BodyTypeMessage < Struct.new(:media_type, :subtype,
                                       :param, :content_id,
                                       :description, :encoding, :size,
                                       :envelope, :body, :lines,
                                       :md5, :disposition, :language,
                                       :extension)
      include BodyStructure

      ##
      # method: envelope
      # :call-seq: envelope -> Envelope
      #
      # Returns a Net::IMAP::Envelope giving the envelope structure.

      ##
      # method: body
      # :call-seq: body -> BodyStructure
      #
      # Returns a Net::IMAP::BodyStructure for the message's body structure.

      ##
      # :call-seq: multipart? -> false
      #
      # BodyTypeMessage is not used for multipart MIME parts.
      def multipart?
        return false
      end

      # Obsolete: use +subtype+ instead.  Calling this will
      # generate a warning message to +stderr+, then return
      # the value of +subtype+.
      def media_subtype
        warn("media_subtype is obsolete, use subtype instead.\n", uplevel: 1)
        return subtype
      end
    end

    # === WARNING
    # BodyTypeAttachment represents a <tt>body-fld-dsp</tt> that is
    # incorrectly in a position where the IMAP4rev1 grammar expects a nested
    # +body+ structure.
    #
    # >>>
    #   \IMAP body structures are parenthesized lists and assign their fields
    #   positionally, so missing fields change the intepretation of all
    #   following fields.  Buggy \IMAP servers sometimes leave fields missing
    #   rather than empty, which inevitably confuses parsers.
    #   BodyTypeAttachment was an attempt to parse a common type of buggy body
    #   structure without crashing.
    #
    #   Currently, when Net::IMAP::ResponseParser sees "attachment" as the first
    #   entry in a <tt>body-type-1part</tt>, which is where the MIME type should
    #   be, it uses BodyTypeAttachment to capture the rest.  "attachment" is not
    #   a valid MIME type, but _is_ a common <tt>Content-Disposition</tt>.  What
    #   might have happened was that buggy server could not parse the message
    #   (which might have been incorrectly formatted) and output a
    #   <tt>body-type-dsp</tt> where a Net::IMAP::ResponseParser expected to see
    #   a +body+.
    #
    # A future release will replace this, probably with a ContentDisposition
    # nested inside another body structure object, maybe BodyTypeBasic, or
    # perhaps a new body structure class that represents any unparsable body
    # structure.
    #
    class BodyTypeAttachment < Struct.new(:dsp_type, :_unused_, :param)
      include BodyStructure

      # *invalid for BodyTypeAttachment*
      def media_type
        warn(<<~WARN, uplevel: 1)
          BodyTypeAttachment#media_type is obsolete.  Use dsp_type instead.
        WARN
        dsp_type
      end

      # *invalid for BodyTypeAttachment*
      def subtype
        warn("BodyTypeAttachment#subtype is obsolete.\n", uplevel: 1)
        nil
      end

      ##
      # method: dsp_type
      # :call-seq: dsp_type -> string
      #
      # Returns the content disposition type, as defined by
      # [DISPOSITION[https://tools.ietf.org/html/rfc2183]].

      ##
      # method: param
      # :call-seq: param -> hash
      #
      # Returns a hash representing parameters of the Content-Disposition
      # field, as defined by [DISPOSITION[https://tools.ietf.org/html/rfc2183]].

      ##
      def multipart?
        return false
      end
    end

    # Net::IMAP::BodyTypeMultipart represents body structures of messages and
    # message parts, when <tt>Content-Type</tt> is <tt>multipart/*</tt>.
    class BodyTypeMultipart < Struct.new(:media_type, :subtype,
                                         :parts,
                                         :param, :disposition, :language,
                                         :extension)
      include BodyStructure

      ##
      # method: media_type
      # call-seq: media_type -> "multipart"
      #
      # BodyTypeMultipart is only used with <tt>multipart/*</tt> media types.

      ##
      # method: subtype
      # call-seq: subtype -> string
      #
      # Returns the content subtype name
      # as defined in [MIME-IMB[https://tools.ietf.org/html/rfc2045]].

      ##
      # method: parts
      # call-seq: parts -> array of BodyStructure objects
      #
      # Returns an array with a BodyStructure object for each part contained in
      # this part.

      ##
      # method: param
      # call-seq: param -> hash
      #
      # Returns a hash that represents parameters
      # as defined in [MIME-IMB[https://tools.ietf.org/html/rfc2045]].

      ##
      # method: disposition
      # call-seq: disposition -> ContentDisposition
      #
      # Returns a Net::IMAP::ContentDisposition object giving the content
      # disposition.

      ##
      # method: language
      # :call-seq: language -> string
      #
      # Returns a string or an array of strings giving the body
      # language value as defined in
      # [LANGUAGE-TAGS[https://www.rfc-editor.org/info/rfc3282]].

      ##
      # method: extension
      # call-seq: extension -> array
      #
      # Returns extension data as an array of numbers strings, and nested
      # arrays (of numbers, strings, etc).

      ##
      # :call-seq: multipart? -> true
      #
      # BodyTypeMultipart is used for multipart MIME parts.
      def multipart?
        return true
      end

      ##
      # Obsolete: use +subtype+ instead.  Calling this will
      # generate a warning message to +stderr+, then return
      # the value of +subtype+.
      def media_subtype
        warn("media_subtype is obsolete, use subtype instead.\n", uplevel: 1)
        return subtype
      end
    end

    # === WARNING:
    # >>>
    #   BodyTypeExtension is (incorrectly) used for <tt>message/*</tt> parts
    #   (besides <tt>message/rfc822</tt>, which correctly uses BodyTypeMessage).
    #
    # A future release will replace this class with:
    # * BodyTypeMessage for <tt>message/rfc822</tt> and <tt>message/global</tt>
    # * BodyTypeBasic for any other <tt>message/*</tt>
    class BodyTypeExtension < Struct.new(:media_type, :subtype,
                                         :params, :content_id,
                                         :description, :encoding, :size)
      include BodyStructure

      def multipart?
        return false
      end
    end

  end
end
