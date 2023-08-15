# frozen_string_literal: true
#
# = net/imap.rb
#
# Copyright (C) 2000  Shugo Maeda <shugo@ruby-lang.org>
#
# This library is distributed under the terms of the Ruby license.
# You can freely distribute/modify this library.
#
# Documentation: Shugo Maeda, with RDoc conversion and overview by William
# Webber.
#
# See Net::IMAP for documentation.
#

require "socket"
require "monitor"
require 'net/protocol'
begin
  require "openssl"
rescue LoadError
end

module Net

  # Net::IMAP implements Internet Message Access Protocol (\IMAP) client
  # functionality.  The protocol is described in
  # [IMAP4rev1[https://tools.ietf.org/html/rfc3501]].
  #--
  # TODO: and [IMAP4rev2[https://tools.ietf.org/html/rfc9051]].
  #++
  #
  # == \IMAP Overview
  #
  # An \IMAP client connects to a server, and then authenticates
  # itself using either #authenticate or #login.  Having
  # authenticated itself, there is a range of commands
  # available to it.  Most work with mailboxes, which may be
  # arranged in an hierarchical namespace, and each of which
  # contains zero or more messages.  How this is implemented on
  # the server is implementation-dependent; on a UNIX server, it
  # will frequently be implemented as files in mailbox format
  # within a hierarchy of directories.
  #
  # To work on the messages within a mailbox, the client must
  # first select that mailbox, using either #select or #examine
  # (for read-only access).  Once the client has successfully
  # selected a mailbox, they enter the "_selected_" state, and that
  # mailbox becomes the _current_ mailbox, on which mail-item
  # related commands implicitly operate.
  #
  # === Sequence numbers and UIDs
  #
  # Messages have two sorts of identifiers: message sequence
  # numbers and UIDs.
  #
  # Message sequence numbers number messages within a mailbox
  # from 1 up to the number of items in the mailbox.  If a new
  # message arrives during a session, it receives a sequence
  # number equal to the new size of the mailbox.  If messages
  # are expunged from the mailbox, remaining messages have their
  # sequence numbers "shuffled down" to fill the gaps.
  #
  # To avoid sequence number race conditions, servers must not expunge messages
  # when no command is in progress, nor when responding to #fetch, #store, or
  # #search.  Expunges _may_ be sent during any other command, including
  # #uid_fetch, #uid_store, and #uid_search.  The #noop and #idle commands are
  # both useful for this side-effect: they allow the server to send all mailbox
  # updates, including expunges.
  #
  # UIDs, on the other hand, are permanently guaranteed not to
  # identify another message within the same mailbox, even if
  # the existing message is deleted.  UIDs are required to
  # be assigned in ascending (but not necessarily sequential)
  # order within a mailbox; this means that if a non-IMAP client
  # rearranges the order of mail items within a mailbox, the
  # UIDs have to be reassigned.  An \IMAP client thus cannot
  # rearrange message orders.
  #
  # === Server capabilities and protocol extensions
  #
  # Net::IMAP <em>does not modify its behavior</em> according to server
  # #capability.  Users of the class must check for required capabilities before
  # issuing commands.  Special care should be taken to follow all #capability
  # requirements for #starttls, #login, and #authenticate.
  #
  # See the #capability method for more information.
  #
  # == Examples of Usage
  #
  # === List sender and subject of all recent messages in the default mailbox
  #
  #   imap = Net::IMAP.new('mail.example.com')
  #   imap.authenticate('LOGIN', 'joe_user', 'joes_password')
  #   imap.examine('INBOX')
  #   imap.search(["RECENT"]).each do |message_id|
  #     envelope = imap.fetch(message_id, "ENVELOPE")[0].attr["ENVELOPE"]
  #     puts "#{envelope.from[0].name}: \t#{envelope.subject}"
  #   end
  #
  # === Move all messages from April 2003 from "Mail/sent-mail" to "Mail/sent-apr03"
  #
  #   imap = Net::IMAP.new('mail.example.com')
  #   imap.authenticate('LOGIN', 'joe_user', 'joes_password')
  #   imap.select('Mail/sent-mail')
  #   if not imap.list('Mail/', 'sent-apr03')
  #     imap.create('Mail/sent-apr03')
  #   end
  #   imap.search(["BEFORE", "30-Apr-2003", "SINCE", "1-Apr-2003"]).each do |message_id|
  #     imap.copy(message_id, "Mail/sent-apr03")
  #     imap.store(message_id, "+FLAGS", [:Deleted])
  #   end
  #   imap.expunge
  #
  # == Thread Safety
  #
  # Net::IMAP supports concurrent threads. For example,
  #
  #   imap = Net::IMAP.new("imap.foo.net", "imap2")
  #   imap.authenticate("cram-md5", "bar", "password")
  #   imap.select("inbox")
  #   fetch_thread = Thread.start { imap.fetch(1..-1, "UID") }
  #   search_result = imap.search(["BODY", "hello"])
  #   fetch_result = fetch_thread.value
  #   imap.disconnect
  #
  # This script invokes the FETCH command and the SEARCH command concurrently.
  #
  # == Errors
  #
  # An \IMAP server can send three different types of responses to indicate
  # failure:
  #
  # NO:: the attempted command could not be successfully completed.  For
  #      instance, the username/password used for logging in are incorrect;
  #      the selected mailbox does not exist; etc.
  #
  # BAD:: the request from the client does not follow the server's
  #       understanding of the \IMAP protocol.  This includes attempting
  #       commands from the wrong client state; for instance, attempting
  #       to perform a SEARCH command without having SELECTed a current
  #       mailbox.  It can also signal an internal server
  #       failure (such as a disk crash) has occurred.
  #
  # BYE:: the server is saying goodbye.  This can be part of a normal
  #       logout sequence, and can be used as part of a login sequence
  #       to indicate that the server is (for some reason) unwilling
  #       to accept your connection.  As a response to any other command,
  #       it indicates either that the server is shutting down, or that
  #       the server is timing out the client connection due to inactivity.
  #
  # These three error response are represented by the errors
  # Net::IMAP::NoResponseError, Net::IMAP::BadResponseError, and
  # Net::IMAP::ByeResponseError, all of which are subclasses of
  # Net::IMAP::ResponseError.  Essentially, all methods that involve
  # sending a request to the server can generate one of these errors.
  # Only the most pertinent instances have been documented below.
  #
  # Because the IMAP class uses Sockets for communication, its methods
  # are also susceptible to the various errors that can occur when
  # working with sockets.  These are generally represented as
  # Errno errors.  For instance, any method that involves sending a
  # request to the server and/or receiving a response from it could
  # raise an Errno::EPIPE error if the network connection unexpectedly
  # goes down.  See the socket(7), ip(7), tcp(7), socket(2), connect(2),
  # and associated man pages.
  #
  # Finally, a Net::IMAP::DataFormatError is thrown if low-level data
  # is found to be in an incorrect format (for instance, when converting
  # between UTF-8 and UTF-16), and Net::IMAP::ResponseParseError is
  # thrown if a server response is non-parseable.
  #
  # == What's here?
  #
  # * {Connection control}[rdoc-ref:Net::IMAP@Connection+control+methods]
  # * {Core IMAP commands}[rdoc-ref:Net::IMAP@Core+IMAP+commands]
  #   * {...for any state}[rdoc-ref:Net::IMAP@IMAP+commands+for+any+state]
  #   * {...for the "not authenticated" state}[rdoc-ref:Net::IMAP@IMAP+commands+for+the+-22Not+Authenticated-22+state]
  #   * {...for the "authenticated" state}[rdoc-ref:Net::IMAP@IMAP+commands+for+the+-22Authenticated-22+state]
  #   * {...for the "selected" state}[rdoc-ref:Net::IMAP@IMAP+commands+for+the+-22Selected-22+state]
  #   * {...for the "logout" state}[rdoc-ref:Net::IMAP@IMAP+commands+for+the+-22Logout-22+state]
  # * {Supported IMAP extensions}[rdoc-ref:Net::IMAP@Supported+IMAP+extensions]
  # * {Handling server responses}[rdoc-ref:Net::IMAP@Handling+server+responses]
  #
  # === Connection control methods
  #
  # - Net::IMAP.new: A new client connects immediately and waits for a
  #   successful server greeting before returning the new client object.
  # - #starttls: Asks the server to upgrade a clear-text connection to use TLS.
  # - #logout: Tells the server to end the session. Enters the "_logout_" state.
  # - #disconnect: Disconnects the connection (without sending #logout first).
  # - #disconnected?: True if the connection has been closed.
  #
  # === Core \IMAP commands
  #
  # The following commands are defined either by
  # the [IMAP4rev1[https://tools.ietf.org/html/rfc3501]] base specification, or
  # by one of the following extensions:
  # [IDLE[https://tools.ietf.org/html/rfc2177]],
  # [NAMESPACE[https://tools.ietf.org/html/rfc2342]],
  # [UNSELECT[https://tools.ietf.org/html/rfc3691]],
  #--
  # TODO: [ENABLE[https://tools.ietf.org/html/rfc5161]],
  # TODO: [LIST-EXTENDED[https://tools.ietf.org/html/rfc5258]],
  # TODO: [LIST-STATUS[https://tools.ietf.org/html/rfc5819]],
  #++
  # [MOVE[https://tools.ietf.org/html/rfc6851]].
  # These extensions are widely supported by modern IMAP4rev1 servers and have
  # all been integrated into [IMAP4rev2[https://tools.ietf.org/html/rfc9051]].
  # <em>Note: Net::IMAP doesn't fully support IMAP4rev2 yet.</em>
  #
  #--
  # TODO: When IMAP4rev2 is supported, add the following to the each of the
  # appropriate commands below.
  #   Note:: CHECK has been removed from IMAP4rev2.
  #   Note:: LSUB is obsoleted by +LIST-EXTENDED and has been removed from IMAP4rev2.
  #   <em>Some arguments require the +LIST-EXTENDED+ or +IMAP4rev2+ capability.</em>
  #   <em>Requires either the +ENABLE+    or +IMAP4rev2+ capability.</em>
  #   <em>Requires either the +NAMESPACE+ or +IMAP4rev2+ capability.</em>
  #   <em>Requires either the +IDLE+      or +IMAP4rev2+ capability.</em>
  #   <em>Requires either the +UNSELECT+  or +IMAP4rev2+ capability.</em>
  #   <em>Requires either the +UIDPLUS+   or +IMAP4rev2+ capability.</em>
  #   <em>Requires either the +MOVE+      or +IMAP4rev2+ capability.</em>
  #++
  #
  # ==== \IMAP commands for any state
  #
  # - #capability: Returns the server's capabilities as an array of strings.
  #
  #   <em>Capabilities may change after</em> #starttls, #authenticate, or #login
  #   <em>and cached capabilities must be reloaded.</em>
  # - #noop: Allows the server to send unsolicited untagged #responses.
  # - #logout: Tells the server to end the session. Enters the "_logout_" state.
  #
  # ==== \IMAP commands for the "Not Authenticated" state
  #
  # In addition to the universal commands, the following commands are valid in
  # the "<em>not authenticated</em>" state:
  #
  # - #starttls: Upgrades a clear-text connection to use TLS.
  #
  #   <em>Requires the +STARTTLS+ capability.</em>
  # - #authenticate: Identifies the client to the server using a {SASL
  #   mechanism}[https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml].
  #   Enters the "_authenticated_" state.
  #
  #   <em>Requires the <tt>AUTH=#{mechanism}</tt> capability for the chosen
  #   mechanism.</em>
  # - #login: Identifies the client to the server using a plain text password.
  #   Using #authenticate is generally preferred.  Enters the "_authenticated_"
  #   state.
  #
  #   <em>The +LOGINDISABLED+ capability</em> <b>must NOT</b> <em>be listed.</em>
  #
  # ==== \IMAP commands for the "Authenticated" state
  #
  # In addition to the universal commands, the following commands are valid in
  # the "_authenticated_" state:
  #
  #--
  # - #enable: <em>Not implemented by Net::IMAP, yet.</em>
  #
  #   <em>Requires the +ENABLE+ capability.</em>
  #++
  # - #select:  Open a mailbox and enter the "_selected_" state.
  # - #examine: Open a mailbox read-only, and enter the "_selected_" state.
  # - #create: Creates a new mailbox.
  # - #delete: Permanently remove a mailbox.
  # - #rename: Change the name of a mailbox.
  # - #subscribe: Adds a mailbox to the "subscribed" set.
  # - #unsubscribe: Removes a mailbox from the "subscribed" set.
  # - #list: Returns names and attributes of mailboxes matching a given pattern.
  # - #namespace: Returns mailbox namespaces, with path prefixes and delimiters.
  #
  #   <em>Requires the +NAMESPACE+ capability.</em>
  # - #status: Returns mailbox information, e.g. message count, unseen message
  #   count, +UIDVALIDITY+ and +UIDNEXT+.
  # - #append: Appends a message to the end of a mailbox.
  # - #idle: Allows the server to send updates to the client, without the client
  #   needing to poll using #noop.
  #
  #   <em>Requires the +IDLE+ capability.</em>
  # - #lsub: Lists mailboxes the user has declared "active" or "subscribed".
  #--
  #   <em>Replaced by</em> <tt>LIST-EXTENDED</tt> <em>and removed from</em>
  #   +IMAP4rev2+.  <em>However, Net::IMAP hasn't implemented</em>
  #   <tt>LIST-EXTENDED</tt> _yet_.
  #++
  #
  # ==== \IMAP commands for the "Selected" state
  #
  # In addition to the universal commands and the "authenticated" commands, the
  # following commands are valid in the "_selected_" state:
  #
  # - #close: Closes the mailbox and returns to the "_authenticated_" state,
  #   expunging deleted messages, unless the mailbox was opened as read-only.
  # - #unselect: Closes the mailbox and returns to the "_authenticated_" state,
  #   without expunging any messages.
  #
  #   <em>Requires the +UNSELECT+ capability.</em>
  # - #expunge: Permanently removes messages which have the Deleted flag set.
  # - #uid_expunge: Restricts #expunge to only remove the specified UIDs.
  #
  #   <em>Requires the +UIDPLUS+ capability.</em>
  # - #search, #uid_search: Returns sequence numbers or UIDs of messages that
  #   match the given searching criteria.
  # - #fetch, #uid_fetch: Returns data associated with a set of messages,
  #   specified by sequence number or UID.
  # - #store, #uid_store: Alters a message's flags.
  # - #copy, #uid_copy: Copies the specified messages to the end of the
  #   specified destination mailbox.
  # - #move, #uid_move: Moves the specified messages to the end of the
  #   specified destination mailbox, expunging them from the current mailbox.
  #
  #   <em>Requires the +MOVE+ capability.</em>
  # - #check: Mostly obsolete.  Can be replaced with #noop or #idle.
  #--
  #   <em>Removed from IMAP4rev2.</em>
  #++
  #
  # ==== \IMAP commands for the "Logout" state
  #
  # No \IMAP commands are valid in the +logout+ state.  If the socket is still
  # open, Net::IMAP will close it after receiving server confirmation.
  # Exceptions will be raised by \IMAP commands that have already started and
  # are waiting for a response, as well as any that are called after logout.
  #
  # === Supported \IMAP extensions
  #
  # ==== RFC9051: +IMAP4rev2+
  #
  # Although IMAP4rev2[https://tools.ietf.org/html/rfc9051] is <em>not supported
  # yet</em>, Net::IMAP supports several extensions that have been folded into
  # it: +IDLE+, +MOVE+, +NAMESPACE+, +UIDPLUS+, and +UNSELECT+.
  #--
  # TODO: RFC4466, ABNF extensions (automatic support for other extensions)
  # TODO: +ESEARCH+, ExtendedSearchData
  # TODO: +SEARCHRES+,
  # TODO: +ENABLE+,
  # TODO: +SASL-IR+,
  # TODO: +LIST-EXTENDED+,
  # TODO: +LIST-STATUS+,
  # TODO: +LITERAL-+,
  # TODO: +BINARY+ (only the FETCH side)
  # TODO: +SPECIAL-USE+
  # implicitly supported, but we can do better: Response codes: RFC5530, etc
  # implicitly supported, but we can do better: <tt>STATUS=SIZE</tt>
  # implicitly supported, but we can do better: <tt>STATUS DELETED</tt>
  #++
  # Commands for these extensions are included with the {Core IMAP
  # commands}[rdoc-ref:Net::IMAP@Core+IMAP+commands], above.  Other supported
  # extensons are listed below.
  #
  # ==== RFC2087: +QUOTA+
  # - #getquota: returns the resource usage and limits for a quota root
  # - #getquotaroot: returns the list of quota roots for a mailbox, as well as
  #   their resource usage and limits.
  # - #setquota: sets the resource limits for a given quota root.
  #
  # ==== RFC2177: +IDLE+
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051], so it is also
  # listed with {Core IMAP commands}[rdoc-ref:Net::IMAP@Core+IMAP+commands].
  # - #idle: Allows the server to send updates to the client, without the client
  #   needing to poll using #noop.
  #
  # ==== RFC2342: +NAMESPACE+
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051], so it is also
  # listed with {Core IMAP commands}[rdoc-ref:Net::IMAP@Core+IMAP+commands].
  # - #namespace: Returns mailbox namespaces, with path prefixes and delimiters.
  #
  # ==== RFC2971: +ID+
  # - #id: exchanges client and server implementation information.
  #
  #--
  # ==== RFC3502: +MULTIAPPEND+
  # TODO...
  #++
  #
  #--
  # ==== RFC3516: +BINARY+
  # TODO...
  #++
  #
  # ==== RFC3691: +UNSELECT+
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051], so it is also
  # listed with {Core IMAP commands}[rdoc-ref:Net::IMAP@Core+IMAP+commands].
  # - #unselect: Closes the mailbox and returns to the "_authenticated_" state,
  #   without expunging any messages.
  #
  # ==== RFC4314: +ACL+
  # - #getacl: lists the authenticated user's access rights to a mailbox.
  # - #setacl: sets the access rights for a user on a mailbox
  #--
  # TODO: #deleteacl, #listrights, #myrights
  #++
  # - *_Note:_* +DELETEACL+, +LISTRIGHTS+, and +MYRIGHTS+ are not supported yet.
  #
  # ==== RFC4315: +UIDPLUS+
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051], so it is also
  # listed with {Core IMAP commands}[rdoc-ref:Net::IMAP@Core+IMAP+commands].
  # - #uid_expunge: Restricts #expunge to only remove the specified UIDs.
  # - Updates #select, #examine with the +UIDNOTSTICKY+ ResponseCode
  # - Updates #append with the +APPENDUID+ ResponseCode
  # - Updates #copy, #move with the +COPYUID+ ResponseCode
  #
  #--
  # ==== RFC4466: Collected Extensions to IMAP4 ABNF
  # TODO...
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051], this RFC updates
  # the protocol to enable new optional parameters to many commands: #select,
  # #examine, #create, #rename, #fetch, #uid_fetch, #store, #uid_store, #search,
  # #uid_search, and #append.  However, specific parameters are not defined.
  # Extensions to these commands use this syntax whenever possible.  Net::IMAP
  # may be partially compatible with extensions to these commands, even without
  # any explicit support.
  #++
  #
  #--
  # ==== RFC4731 +ESEARCH+
  # TODO...
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051].
  # - Updates #search, #uid_search to accept result options: +MIN+, +MAX+,
  #   +ALL+, +COUNT+, and to return ExtendedSearchData.
  #++
  #
  #--
  # ==== RFC4959: +SASL-IR+
  # TODO...
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051].
  # - Updates #authenticate to reduce round-trips for supporting mechanisms.
  #++
  #
  #--
  # ==== RFC4978: COMPRESS=DEFLATE
  # TODO...
  #++
  #
  #--
  # ==== RFC5182 +SEARCHRES+
  # TODO...
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051].
  # - Updates #search, #uid_search with the +SAVE+ result option.
  # - Updates #copy, #uid_copy, #fetch, #uid_fetch, #move, #uid_move, #search,
  #   #uid_search, #store, #uid_store, and #uid_expunge with ability to
  #   reference the saved result of a previous #search or #uid_search command.
  #++
  #
  # ==== RFC5256: +SORT+
  # - #sort, #uid_sort: An alternate version of #search or #uid_search which
  #   sorts the results by specified keys.
  # ==== RFC5256: +THREAD+
  # - #thread, #uid_thread: An alternate version of #search or #uid_search,
  #   which arranges the results into ordered groups or threads according to a
  #   chosen algorithm.
  #
  #--
  # ==== RFC5258 +LIST-EXTENDED+
  # TODO...
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051], this updates the
  # protocol with new optional parameters to the #list command, adding a few of
  # its own.  Net::IMAP may be forward-compatible with future #list extensions,
  # even without any explicit support.
  # - Updates #list to accept selection options: +SUBSCRIBED+, +REMOTE+, and
  #   +RECURSIVEMATCH+, and return options: +SUBSCRIBED+ and +CHILDREN+.
  #++
  #
  #--
  # ==== RFC5819 +LIST-STATUS+
  # TODO...
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051].
  # - Updates #list with +STATUS+ return option.
  #++
  #
  # ==== +XLIST+ (non-standard, deprecated)
  # - #xlist: replaced by +SPECIAL-USE+ attributes in #list responses.
  #
  #--
  # ==== RFC6154 +SPECIAL-USE+
  # TODO...
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051].
  # - Updates #list with the +SPECIAL-USE+ selection and return options.
  #++
  #
  # ==== RFC6851: +MOVE+
  # Folded into IMAP4rev2[https://tools.ietf.org/html/rfc9051], so it is also
  # listed with {Core IMAP commands}[rdoc-ref:Net::IMAP@Core+IMAP+commands].
  # - #move, #uid_move: Moves the specified messages to the end of the
  #   specified destination mailbox, expunging them from the current mailbox.
  #
  #--
  # ==== RFC6855: UTF8=ACCEPT
  # TODO...
  # ==== RFC6855: UTF8=ONLY
  # TODO...
  #++
  #
  #--
  # ==== RFC7888: <tt>LITERAL+</tt>, +LITERAL-+
  # TODO...
  # ==== RFC7162: +QRESYNC+
  # TODO...
  # ==== RFC7162: +CONDSTORE+
  # TODO...
  # ==== RFC8474: +OBJECTID+
  # TODO...
  # ==== RFC9208: +QUOTA+
  # TODO...
  #++
  #
  # === Handling server responses
  #
  # - #greeting: The server's initial untagged response, which can indicate a
  #   pre-authenticated connection.
  # - #responses: The untagged responses, as a hash.  Keys are the untagged
  #   response type (e.g. "OK", "FETCH", "FLAGS") and response code (e.g.
  #   "ALERT", "UIDVALIDITY", "UIDNEXT", "TRYCREATE", etc).  Values are arrays
  #   of UntaggedResponse or ResponseCode.
  # - #add_response_handler: Add a block to be called inside the receiver thread
  #   with every server response.
  # - #remove_response_handler: Remove a previously added response handler.
  #
  #
  # == References
  #--
  # TODO: Consider moving references list to REFERENCES.md or REFERENCES.rdoc.
  #++
  #
  # [{IMAP4rev1}[https://www.rfc-editor.org/rfc/rfc3501.html]]::
  #   Crispin, M., "INTERNET MESSAGE ACCESS PROTOCOL - \VERSION 4rev1",
  #   RFC 3501, DOI 10.17487/RFC3501, March 2003,
  #   <https://www.rfc-editor.org/info/rfc3501>.
  #
  # [IMAP-ABNF-EXT[https://www.rfc-editor.org/rfc/rfc4466.html]]::
  #   Melnikov, A. and C. Daboo, "Collected Extensions to IMAP4 ABNF",
  #   RFC 4466, DOI 10.17487/RFC4466, April 2006,
  #   <https://www.rfc-editor.org/info/rfc4466>.
  #
  #   <em>Note: Net::IMAP cannot parse the entire RFC4466 grammar yet.</em>
  #
  # [{IMAP4rev2}[https://www.rfc-editor.org/rfc/rfc9051.html]]::
  #   Melnikov, A., Ed., and B. Leiba, Ed., "Internet Message Access Protocol
  #   (\IMAP) - Version 4rev2", RFC 9051, DOI 10.17487/RFC9051, August 2021,
  #   <https://www.rfc-editor.org/info/rfc9051>.
  #
  #   <em>Note: Net::IMAP is not fully compatible with IMAP4rev2 yet.</em>
  #
  # [IMAP-IMPLEMENTATION[https://www.rfc-editor.org/info/rfc2683]]::
  #   Leiba, B., "IMAP4 Implementation Recommendations",
  #   RFC 2683, DOI 10.17487/RFC2683, September 1999,
  #   <https://www.rfc-editor.org/info/rfc2683>.
  #
  # [IMAP-MULTIACCESS[https://www.rfc-editor.org/info/rfc2180]]::
  #   Gahrns, M., "IMAP4 Multi-Accessed Mailbox Practice", RFC 2180, DOI
  #   10.17487/RFC2180, July 1997, <https://www.rfc-editor.org/info/rfc2180>.
  #
  # [UTF7[https://tools.ietf.org/html/rfc2152]]::
  #   Goldsmith, D. and M. Davis, "UTF-7 A Mail-Safe Transformation Format of
  #   Unicode", RFC 2152, DOI 10.17487/RFC2152, May 1997,
  #   <https://www.rfc-editor.org/info/rfc2152>.
  #
  # === Message envelope and body structure
  #
  # [RFC5322[https://tools.ietf.org/html/rfc5322]]::
  #   Resnick, P., Ed., "Internet Message Format",
  #   RFC 5322, DOI 10.17487/RFC5322, October 2008,
  #   <https://www.rfc-editor.org/info/rfc5322>.
  #
  #   <em>Note: obsoletes</em>
  #   RFC-2822[https://tools.ietf.org/html/rfc2822]<em> (April 2001) and</em>
  #   RFC-822[https://tools.ietf.org/html/rfc822]<em> (August 1982).</em>
  #
  # [CHARSET[https://tools.ietf.org/html/rfc2978]]::
  #   Freed, N. and J. Postel, "IANA Charset Registration Procedures", BCP 19,
  #   RFC 2978, DOI 10.17487/RFC2978, October 2000,
  #   <https://www.rfc-editor.org/info/rfc2978>.
  #
  # [DISPOSITION[https://tools.ietf.org/html/rfc2183]]::
  #    Troost, R., Dorner, S., and K. Moore, Ed., "Communicating Presentation
  #    Information in Internet Messages: The Content-Disposition Header
  #    Field", RFC 2183, DOI 10.17487/RFC2183, August 1997,
  #    <https://www.rfc-editor.org/info/rfc2183>.
  #
  # [MIME-IMB[https://tools.ietf.org/html/rfc2045]]::
  #    Freed, N. and N. Borenstein, "Multipurpose Internet Mail Extensions
  #    (MIME) Part One: Format of Internet Message Bodies",
  #    RFC 2045, DOI 10.17487/RFC2045, November 1996,
  #    <https://www.rfc-editor.org/info/rfc2045>.
  #
  # [MIME-IMT[https://tools.ietf.org/html/rfc2046]]::
  #    Freed, N. and N. Borenstein, "Multipurpose Internet Mail Extensions
  #    (MIME) Part Two: Media Types", RFC 2046, DOI 10.17487/RFC2046,
  #    November 1996, <https://www.rfc-editor.org/info/rfc2046>.
  #
  # [MIME-HDRS[https://tools.ietf.org/html/rfc2047]]::
  #    Moore, K., "MIME (Multipurpose Internet Mail Extensions) Part Three:
  #    Message Header Extensions for Non-ASCII Text",
  #    RFC 2047, DOI 10.17487/RFC2047, November 1996,
  #    <https://www.rfc-editor.org/info/rfc2047>.
  #
  # [RFC2231[https://tools.ietf.org/html/rfc2231]]::
  #    Freed, N. and K. Moore, "MIME Parameter Value and Encoded Word
  #    Extensions: Character Sets, Languages, and Continuations",
  #    RFC 2231, DOI 10.17487/RFC2231, November 1997,
  #    <https://www.rfc-editor.org/info/rfc2231>.
  #
  # [I18n-HDRS[https://tools.ietf.org/html/rfc6532]]::
  #    Yang, A., Steele, S., and N. Freed, "Internationalized Email Headers",
  #    RFC 6532, DOI 10.17487/RFC6532, February 2012,
  #    <https://www.rfc-editor.org/info/rfc6532>.
  #
  # [LANGUAGE-TAGS[https://www.rfc-editor.org/info/rfc3282]]::
  #    Alvestrand, H., "Content Language Headers",
  #    RFC 3282, DOI 10.17487/RFC3282, May 2002,
  #    <https://www.rfc-editor.org/info/rfc3282>.
  #
  # [LOCATION[https://www.rfc-editor.org/info/rfc2557]]::
  #    Palme, J., Hopmann, A., and N. Shelness, "MIME Encapsulation of
  #    Aggregate Documents, such as HTML (MHTML)",
  #    RFC 2557, DOI 10.17487/RFC2557, March 1999,
  #    <https://www.rfc-editor.org/info/rfc2557>.
  #
  # [MD5[https://tools.ietf.org/html/rfc1864]]::
  #    Myers, J. and M. Rose, "The Content-MD5 Header Field",
  #    RFC 1864, DOI 10.17487/RFC1864, October 1995,
  #    <https://www.rfc-editor.org/info/rfc1864>.
  #
  #--
  # TODO: Document IMAP keywords.
  #
  # [RFC3503[https://tools.ietf.org/html/rfc3503]]
  #   Melnikov, A., "Message Disposition Notification (MDN)
  #   profile for Internet Message Access Protocol (IMAP)",
  #   RFC 3503, DOI 10.17487/RFC3503, March 2003,
  #   <https://www.rfc-editor.org/info/rfc3503>.
  #++
  #
  # === Supported \IMAP Extensions
  #
  # [QUOTA[https://tools.ietf.org/html/rfc2087]]::
  #   Myers, J., "IMAP4 QUOTA extension", RFC 2087, DOI 10.17487/RFC2087,
  #   January 1997, <https://www.rfc-editor.org/info/rfc2087>.
  #--
  # TODO: test compatibility with updated QUOTA extension:
  # [QUOTA[https://tools.ietf.org/html/rfc9208]]::
  #   Melnikov, A., "IMAP QUOTA Extension", RFC 9208, DOI 10.17487/RFC9208,
  #   March 2022, <https://www.rfc-editor.org/info/rfc9208>.
  #++
  # [IDLE[https://tools.ietf.org/html/rfc2177]]::
  #   Leiba, B., "IMAP4 IDLE command", RFC 2177, DOI 10.17487/RFC2177,
  #   June 1997, <https://www.rfc-editor.org/info/rfc2177>.
  # [NAMESPACE[https://tools.ietf.org/html/rfc2342]]::
  #   Gahrns, M. and C. Newman, "IMAP4 Namespace", RFC 2342,
  #   DOI 10.17487/RFC2342, May 1998, <https://www.rfc-editor.org/info/rfc2342>.
  # [ID[https://tools.ietf.org/html/rfc2971]]::
  #   Showalter, T., "IMAP4 ID extension", RFC 2971, DOI 10.17487/RFC2971,
  #   October 2000, <https://www.rfc-editor.org/info/rfc2971>.
  # [ACL[https://tools.ietf.org/html/rfc4314]]::
  #   Melnikov, A., "IMAP4 Access Control List (ACL) Extension", RFC 4314,
  #   DOI 10.17487/RFC4314, December 2005,
  #   <https://www.rfc-editor.org/info/rfc4314>.
  # [UIDPLUS[https://www.rfc-editor.org/rfc/rfc4315.html]]::
  #   Crispin, M., "Internet Message Access Protocol (\IMAP) - UIDPLUS
  #   extension", RFC 4315, DOI 10.17487/RFC4315, December 2005,
  #   <https://www.rfc-editor.org/info/rfc4315>.
  # [SORT[https://tools.ietf.org/html/rfc5256]]::
  #   Crispin, M. and K. Murchison, "Internet Message Access Protocol - SORT and
  #   THREAD Extensions", RFC 5256, DOI 10.17487/RFC5256, June 2008,
  #   <https://www.rfc-editor.org/info/rfc5256>.
  # [THREAD[https://tools.ietf.org/html/rfc5256]]::
  #   Crispin, M. and K. Murchison, "Internet Message Access Protocol - SORT and
  #   THREAD Extensions", RFC 5256, DOI 10.17487/RFC5256, June 2008,
  #   <https://www.rfc-editor.org/info/rfc5256>.
  # [RFC5530[https://www.rfc-editor.org/rfc/rfc5530.html]]::
  #   Gulbrandsen, A., "IMAP Response Codes", RFC 5530, DOI 10.17487/RFC5530,
  #   May 2009, <https://www.rfc-editor.org/info/rfc5530>.
  # [MOVE[https://tools.ietf.org/html/rfc6851]]::
  #   Gulbrandsen, A. and N. Freed, Ed., "Internet Message Access Protocol
  #   (\IMAP) - MOVE Extension", RFC 6851, DOI 10.17487/RFC6851, January 2013,
  #   <https://www.rfc-editor.org/info/rfc6851>.
  #
  # === IANA registries
  #
  # * {IMAP Capabilities}[http://www.iana.org/assignments/imap4-capabilities]
  # * {IMAP Response Codes}[https://www.iana.org/assignments/imap-response-codes/imap-response-codes.xhtml]
  # * {IMAP Mailbox Name Attributes}[https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml]
  # * {IMAP and JMAP Keywords}[https://www.iana.org/assignments/imap-jmap-keywords/imap-jmap-keywords.xhtml]
  # * {IMAP Threading Algorithms}[https://www.iana.org/assignments/imap-threading-algorithms/imap-threading-algorithms.xhtml]
  #--
  # * {IMAP Quota Resource Types}[http://www.iana.org/assignments/imap4-capabilities#imap-capabilities-2]
  # * [{LIST-EXTENDED options and responses}[https://www.iana.org/assignments/imap-list-extended/imap-list-extended.xhtml]
  # * {IMAP METADATA Server Entry and Mailbox Entry Registries}[https://www.iana.org/assignments/imap-metadata/imap-metadata.xhtml]
  # * {IMAP ANNOTATE Extension Entries and Attributes}[https://www.iana.org/assignments/imap-annotate-extension/imap-annotate-extension.xhtml]
  # * {IMAP URLAUTH Access Identifiers and Prefixes}[https://www.iana.org/assignments/urlauth-access-ids/urlauth-access-ids.xhtml]
  # * {IMAP URLAUTH Authorization Mechanism Registry}[https://www.iana.org/assignments/urlauth-authorization-mechanism-registry/urlauth-authorization-mechanism-registry.xhtml]
  #++
  # * {SASL Mechanisms and SASL SCRAM Family Mechanisms}[https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml]
  # * {Service Name and Transport Protocol Port Number Registry}[https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml]:
  #   +imap+: tcp/143, +imaps+: tcp/993
  # * {GSSAPI/Kerberos/SASL Service Names}[https://www.iana.org/assignments/gssapi-service-names/gssapi-service-names.xhtml]:
  #   +imap+
  # * {Character sets}[https://www.iana.org/assignments/character-sets/character-sets.xhtml]
  #
  class IMAP < Protocol
    VERSION = "0.3.4"

    include MonitorMixin
    if defined?(OpenSSL::SSL)
      include OpenSSL
      include SSL
    end

    # Returns the initial greeting the server, an UntaggedResponse.
    attr_reader :greeting

    # Returns recorded untagged responses.
    #
    # For example:
    #
    #   imap.select("inbox")
    #   p imap.responses["EXISTS"][-1]
    #   #=> 2
    #   p imap.responses["UIDVALIDITY"][-1]
    #   #=> 968263756
    attr_reader :responses

    # Returns all response handlers.
    attr_reader :response_handlers

    # Seconds to wait until a connection is opened.
    # If the IMAP object cannot open a connection within this time,
    # it raises a Net::OpenTimeout exception. The default value is 30 seconds.
    attr_reader :open_timeout

    # Seconds to wait until an IDLE response is received.
    attr_reader :idle_response_timeout

    attr_accessor :client_thread # :nodoc:

    # Returns the debug mode.
    def self.debug
      return @@debug
    end

    # Sets the debug mode.
    def self.debug=(val)
      return @@debug = val
    end

    # The default port for IMAP connections, port 143
    def self.default_port
      return PORT
    end

    # The default port for IMAPS connections, port 993
    def self.default_tls_port
      return SSL_PORT
    end

    class << self
      alias default_imap_port default_port
      alias default_imaps_port default_tls_port
      alias default_ssl_port default_tls_port
    end

    # Disconnects from the server.
    #
    # Related: #logout
    def disconnect
      return if disconnected?
      begin
        begin
          # try to call SSL::SSLSocket#io.
          @sock.io.shutdown
        rescue NoMethodError
          # @sock is not an SSL::SSLSocket.
          @sock.shutdown
        end
      rescue Errno::ENOTCONN
        # ignore `Errno::ENOTCONN: Socket is not connected' on some platforms.
      rescue Exception => e
        @receiver_thread.raise(e)
      end
      @receiver_thread.join
      synchronize do
        @sock.close
      end
      raise e if e
    end

    # Returns true if disconnected from the server.
    #
    # Related: #logout, #disconnect
    def disconnected?
      return @sock.closed?
    end

    # Sends a {CAPABILITY command [IMAP4rev1 §6.1.1]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.1.1]
    # and returns an array of capabilities that the server supports.  Each
    # capability is a string.
    #
    # See the {IANA IMAP4 capabilities
    # registry}[http://www.iana.org/assignments/imap4-capabilities] for a list
    # of all standard capabilities, and their reference RFCs.
    #
    # >>>
    #   <em>*Note* that Net::IMAP does not currently modify its
    #   behaviour according to the capabilities of the server;
    #   it is up to the user of the class to ensure that
    #   a certain capability is supported by a server before
    #   using it.</em>
    #
    # Capability requirements—other than +IMAP4rev1+—are listed in the
    # documentation for each command method.
    #
    # ===== Basic IMAP4rev1 capabilities
    #
    # All IMAP4rev1 servers must include +IMAP4rev1+ in their capabilities list.
    # All IMAP4rev1 servers must _implement_ the +STARTTLS+,
    # <tt>AUTH=PLAIN</tt>, and +LOGINDISABLED+ capabilities, and clients must
    # respect their presence or absence.  See the capabilites requirements on
    # #starttls, #login, and #authenticate.
    #
    # ===== Using IMAP4rev1 extensions
    #
    # IMAP4rev1 servers must not activate incompatible behavior until an
    # explicit client action invokes a capability, e.g. sending a command or
    # command argument specific to that capability.  Extensions with backward
    # compatible behavior, such as response codes or mailbox attributes, may
    # be sent at any time.
    #
    # Invoking capabilities which are unknown to Net::IMAP may cause unexpected
    # behavior and errors, for example ResponseParseError is raised when unknown
    # response syntax is received.  Invoking commands or command parameters that
    # are unsupported by the server may raise NoResponseError, BadResponseError,
    # or cause other unexpected behavior.
    #
    # ===== Caching +CAPABILITY+ responses
    #
    # Servers may send their capability list, unsolicited, using the
    # +CAPABILITY+ response code or an untagged +CAPABILITY+ response.  These
    # responses can be retrieved and cached using #responses or
    # #add_response_handler.
    #
    # But cached capabilities _must_ be discarded after #starttls, #login, or
    # #authenticate.  The OK TaggedResponse to #login and #authenticate may
    # include +CAPABILITY+ response code data, but the TaggedResponse for
    # #starttls is sent clear-text and cannot be trusted.
    #
    def capability
      synchronize do
        send_command("CAPABILITY")
        return @responses.delete("CAPABILITY")[-1]
      end
    end

    # Sends an {ID command [RFC2971 §3.1]}[https://www.rfc-editor.org/rfc/rfc2971#section-3.1]
    # and returns a hash of the server's response, or nil if the server does not
    # identify itself.
    #
    # Note that the user should first check if the server supports the ID
    # capability. For example:
    #
    #    capabilities = imap.capability
    #    if capabilities.include?("ID")
    #      id = imap.id(
    #        name: "my IMAP client (ruby)",
    #        version: MyIMAP::VERSION,
    #        "support-url": "mailto:bugs@example.com",
    #        os: RbConfig::CONFIG["host_os"],
    #      )
    #    end
    #
    # See [ID[https://tools.ietf.org/html/rfc2971]] for field definitions.
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +ID+
    # [RFC2971[https://tools.ietf.org/html/rfc2971]]
    def id(client_id=nil)
      synchronize do
        send_command("ID", ClientID.new(client_id))
        @responses.delete("ID")&.last
      end
    end

    # Sends a {NOOP command [IMAP4rev1 §6.1.2]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.1.2]
    # to the server.
    #
    # This allows the server to send unsolicited untagged EXPUNGE #responses,
    # but does not execute any client request.  \IMAP servers are permitted to
    # send unsolicited untagged responses at any time, except for `EXPUNGE`.
    #
    # * +EXPUNGE+ can only be sent while a command is in progress.
    # * +EXPUNGE+ must _not_ be sent during #fetch, #store, or #search.
    # * +EXPUNGE+ may be sent during #uid_fetch, #uid_store, or #uid_search.
    #
    # Related: #idle, #check
    def noop
      send_command("NOOP")
    end

    # Sends a {LOGOUT command [IMAP4rev1 §6.1.3]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.1.3]
    # to inform the command to inform the server that the client is done with
    # the connection.
    #
    # Related: #disconnect
    def logout
      send_command("LOGOUT")
    end

    # Sends a {STARTTLS command [IMAP4rev1 §6.2.1]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.2.1]
    # to start a TLS session.
    #
    # Any +options+ are forwarded to OpenSSL::SSL::SSLContext#set_params.
    #
    # This method returns after TLS negotiation and hostname verification are
    # both successful.  Any error indicates that the connection has not been
    # secured.
    #
    # *Note:*
    # >>>
    #   Any #response_handlers added before STARTTLS should be aware that the
    #   TaggedResponse to STARTTLS is sent clear-text, _before_ TLS negotiation.
    #   TLS negotiation starts immediately after that response.
    #
    # Related: Net::IMAP.new, #login, #authenticate
    #
    # ===== Capability
    #
    # The server's capabilities must include +STARTTLS+.
    #
    # Server capabilities may change after #starttls, #login, and #authenticate.
    # Cached capabilities _must_ be invalidated after this method completes.
    #
    # The TaggedResponse to #starttls is sent clear-text, so the server <em>must
    # *not*</em> send capabilities in the #starttls response and clients <em>must
    # not</em> use them if they are sent.  Servers will generally send an
    # unsolicited untagged response immeditely _after_ #starttls completes.
    #
    def starttls(options = {}, verify = true)
      send_command("STARTTLS") do |resp|
        if resp.kind_of?(TaggedResponse) && resp.name == "OK"
          begin
            # for backward compatibility
            certs = options.to_str
            options = create_ssl_params(certs, verify)
          rescue NoMethodError
          end
          start_tls_session(options)
        end
      end
    end

    # :call-seq:
    #   authenticate(mechanism, ...)                               -> ok_resp
    #   authenticate(mech, *creds, **props) {|prop, auth| val }    -> ok_resp
    #   authenticate(mechanism, authnid, credentials, authzid=nil) -> ok_resp
    #   authenticate(mechanism, **properties)                      -> ok_resp
    #   authenticate(mechanism) {|propname, authctx| prop_value }  -> ok_resp
    #
    # Sends an {AUTHENTICATE command [IMAP4rev1 §6.2.2]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.2.2]
    # to authenticate the client.  If successful, the connection enters the
    # "_authenticated_" state.
    #
    # +mechanism+ is the name of the \SASL authentication mechanism to be used.
    # All other arguments are forwarded to the authenticator for the requested
    # mechanism.  The listed call signatures are suggestions.  <em>The
    # documentation for each individual mechanism must be consulted for its
    # specific parameters.</em>
    #
    # An exception Net::IMAP::NoResponseError is raised if authentication fails.
    #
    # Related: #login, #starttls
    #
    # ==== Supported SASL Mechanisms
    #
    # +PLAIN+::     See PlainAuthenticator.
    #               Login using clear-text username and password.
    #
    # +XOAUTH2+::   See XOauth2Authenticator.
    #               Login using a username and OAuth2 access token.
    #               Non-standard and obsoleted by +OAUTHBEARER+, but widely
    #               supported.
    #
    # >>>
    #   *Deprecated:*  <em>Obsolete mechanisms are available for backwards
    #   compatibility.</em>
    #
    #   For +DIGEST-MD5+ see DigestMD5Authenticator.
    #
    #   For +LOGIN+, see LoginAuthenticator.
    #
    #   For +CRAM-MD5+, see CramMD5Authenticator.
    #
    #   <em>Using a deprecated mechanism will print a warning.</em>
    #
    # See Net::IMAP::Authenticators for information on plugging in
    # authenticators for other mechanisms.  See the {SASL mechanism
    # registry}[https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml]
    # for information on these and other SASL mechanisms.
    #
    # ===== Capabilities
    #
    # Clients MUST NOT attempt to authenticate with a mechanism unless
    # <tt>"AUTH=#{mechanism}"</tt> for that mechanism is a server capability.
    #
    # Server capabilities may change after #starttls, #login, and #authenticate.
    # Cached capabilities _must_ be invalidated after this method completes.
    # The TaggedResponse to #authenticate may include updated capabilities in
    # its ResponseCode.
    #
    # ===== Example
    # If the authenticators ignore unhandled keyword arguments, the same config
    # can be used for multiple mechanisms:
    #
    #    password  = nil # saved locally, so we don't ask more than once
    #    accesstok = nil # saved locally...
    #    creds = {
    #      authcid:      username,
    #      password:     proc { password  ||= ui.prompt_for_password },
    #      oauth2_token: proc { accesstok ||= kms.fresh_access_token },
    #    }
    #    capa = imap.capability
    #    if    capa.include? "AUTH=OAUTHBEARER"
    #      imap.authenticate "OAUTHBEARER",   **creds # authcid, oauth2_token
    #    elsif capa.include? "AUTH=XOAUTH2"
    #      imap.authenticate "XOAUTH2",       **creds # authcid, oauth2_token
    #    elsif capa.include? "AUTH=SCRAM-SHA-256"
    #      imap.authenticate "SCRAM-SHA-256", **creds # authcid, password
    #    elsif capa.include? "AUTH=PLAIN"
    #      imap.authenticate "PLAIN",         **creds # authcid, password
    #    elsif capa.include? "AUTH=DIGEST-MD5"
    #      imap.authenticate "DIGEST-MD5",    **creds # authcid, password
    #    elsif capa.include? "LOGINDISABLED"
    #      raise "the server has disabled login"
    #    else
    #      imap.login username, password
    #    end
    #
    def authenticate(mechanism, *args, **props, &cb)
      authenticator = self.class.authenticator(mechanism, *args, **props, &cb)
      send_command("AUTHENTICATE", mechanism) do |resp|
        if resp.instance_of?(ContinuationRequest)
          data = authenticator.process(resp.data.text.unpack("m")[0])
          s = [data].pack("m0")
          send_string_data(s)
          put_string(CRLF)
        end
      end
    end

    # Sends a {LOGIN command [IMAP4rev1 §6.2.3]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.2.3]
    # to identify the client and carries the plaintext +password+ authenticating
    # this +user+.  If successful, the connection enters the "_authenticated_"
    # state.
    #
    # Using #authenticate is generally preferred over #login.  The LOGIN command
    # is not the same as #authenticate with the "LOGIN" +mechanism+.
    #
    # A Net::IMAP::NoResponseError is raised if authentication fails.
    #
    # Related: #authenticate, #starttls
    #
    # ==== Capabilities
    # Clients MUST NOT call #login if +LOGINDISABLED+ is listed with the
    # capabilities.
    #
    # Server capabilities may change after #starttls, #login, and #authenticate.
    # Cached capabilities _must_ be invalidated after this method completes.
    # The TaggedResponse to #login may include updated capabilities in its
    # ResponseCode.
    #
    def login(user, password)
      send_command("LOGIN", user, password)
    end

    # Sends a {SELECT command [IMAP4rev1 §6.3.1]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.1]
    # to select a +mailbox+ so that messages in the +mailbox+ can be accessed.
    #
    # After you have selected a mailbox, you may retrieve the number of items in
    # that mailbox from <tt>imap.responses["EXISTS"][-1]</tt>, and the number of
    # recent messages from <tt>imap.responses["RECENT"][-1]</tt>.  Note that
    # these values can change if new messages arrive during a session or when
    # existing messages are expunged; see #add_response_handler for a way to
    # detect these events.
    #
    # A Net::IMAP::NoResponseError is raised if the mailbox does not
    # exist or is for some reason non-selectable.
    #
    # Related: #examine
    #
    # ===== Capabilities
    #
    # If [UIDPLUS[https://www.rfc-editor.org/rfc/rfc4315.html]] is supported,
    # the server may return an untagged "NO" response with a "UIDNOTSTICKY"
    # response code indicating that the mailstore does not support persistent
    # UIDs:
    #   @responses["NO"].last.code.name == "UIDNOTSTICKY"
    def select(mailbox)
      synchronize do
        @responses.clear
        send_command("SELECT", mailbox)
      end
    end

    # Sends a {EXAMINE command [IMAP4rev1 §6.3.2]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.2]
    # to select a +mailbox+ so that messages in the +mailbox+ can be accessed.
    # Behaves the same as #select, except that the selected +mailbox+ is
    # identified as read-only.
    #
    # A Net::IMAP::NoResponseError is raised if the mailbox does not
    # exist or is for some reason non-examinable.
    #
    # Related: #select
    def examine(mailbox)
      synchronize do
        @responses.clear
        send_command("EXAMINE", mailbox)
      end
    end

    # Sends a {CREATE command [IMAP4rev1 §6.3.3]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.3]
    # to create a new +mailbox+.
    #
    # A Net::IMAP::NoResponseError is raised if a mailbox with that name
    # cannot be created.
    #
    # Related: #rename, #delete
    def create(mailbox)
      send_command("CREATE", mailbox)
    end

    # Sends a {DELETE command [IMAP4rev1 §6.3.4]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.4]
    # to remove the +mailbox+.
    #
    # A Net::IMAP::NoResponseError is raised if a mailbox with that name
    # cannot be deleted, either because it does not exist or because the
    # client does not have permission to delete it.
    #
    # Related: #create, #rename
    def delete(mailbox)
      send_command("DELETE", mailbox)
    end

    # Sends a {RENAME command [IMAP4rev1 §6.3.5]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.5]
    # to change the name of the +mailbox+ to +newname+.
    #
    # A Net::IMAP::NoResponseError is raised if a mailbox with the
    # name +mailbox+ cannot be renamed to +newname+ for whatever
    # reason; for instance, because +mailbox+ does not exist, or
    # because there is already a mailbox with the name +newname+.
    #
    # Related: #create, #delete
    def rename(mailbox, newname)
      send_command("RENAME", mailbox, newname)
    end

    # Sends a {SUBSCRIBE command [IMAP4rev1 §6.3.6]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.6]
    # to add the specified +mailbox+ name to the server's set of "active" or
    # "subscribed" mailboxes as returned by #lsub.
    #
    # A Net::IMAP::NoResponseError is raised if +mailbox+ cannot be
    # subscribed to; for instance, because it does not exist.
    #
    # Related: #unsubscribe, #lsub, #list
    def subscribe(mailbox)
      send_command("SUBSCRIBE", mailbox)
    end

    # Sends an {UNSUBSCRIBE command [IMAP4rev1 §6.3.7]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.7]
    # to remove the specified +mailbox+ name from the server's set of "active"
    # or "subscribed" mailboxes.
    #
    # A Net::IMAP::NoResponseError is raised if +mailbox+ cannot be
    # unsubscribed from; for instance, because the client is not currently
    # subscribed to it.
    #
    # Related: #subscribe, #lsub, #list
    def unsubscribe(mailbox)
      send_command("UNSUBSCRIBE", mailbox)
    end

    # Sends a {LIST command [IMAP4rev1 §6.3.8]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.8]
    # and returns a subset of names from the complete set of all names available
    # to the client.  +refname+ provides a context (for instance, a base
    # directory in a directory-based mailbox hierarchy).  +mailbox+ specifies a
    # mailbox or (via wildcards) mailboxes under that context.  Two wildcards
    # may be used in +mailbox+: '*', which matches all characters *including*
    # the hierarchy delimiter (for instance, '/' on a UNIX-hosted
    # directory-based mailbox hierarchy); and '%', which matches all characters
    # *except* the hierarchy delimiter.
    #
    # If +refname+ is empty, +mailbox+ is used directly to determine
    # which mailboxes to match.  If +mailbox+ is empty, the root
    # name of +refname+ and the hierarchy delimiter are returned.
    #
    # The return value is an array of MailboxList.
    #
    # Related: #lsub, MailboxList
    #
    # ===== For example:
    #
    #   imap.create("foo/bar")
    #   imap.create("foo/baz")
    #   p imap.list("", "foo/%")
    #   #=> [#<Net::IMAP::MailboxList attr=[:Noselect], delim="/", name="foo/">, \\
    #        #<Net::IMAP::MailboxList attr=[:Noinferiors, :Marked], delim="/", name="foo/bar">, \\
    #        #<Net::IMAP::MailboxList attr=[:Noinferiors], delim="/", name="foo/baz">]
    #
    #--
    # TODO: support LIST-EXTENDED extension [RFC5258].  Needed for IMAP4rev2.
    #++
    def list(refname, mailbox)
      synchronize do
        send_command("LIST", refname, mailbox)
        return @responses.delete("LIST")
      end
    end

    # Sends a {NAMESPACE command [RFC2342 §5]}[https://www.rfc-editor.org/rfc/rfc2342#section-5]
    # and returns the namespaces that are available.  The NAMESPACE command
    # allows a client to discover the prefixes of namespaces used by a server
    # for personal mailboxes, other users' mailboxes, and shared mailboxes.
    #
    # The return value is a Namespaces object which has +personal+, +other+, and
    # +shared+ fields, each an array of Namespace objects.  These arrays will be
    # empty when the server responds with +nil+.
    #
    # Many \IMAP servers are configured with the default personal namespaces as
    # <tt>("" "/")</tt>: no prefix and the "+/+" hierarchy delimiter. In that
    # common case, the naive client may not have any trouble naming mailboxes.
    # But many servers are configured with the default personal namespace as
    # e.g.  <tt>("INBOX." ".")</tt>, placing all personal folders under INBOX,
    # with "+.+" as the hierarchy delimiter. If the client does not check for
    # this, but naively assumes it can use the same folder names for all
    # servers, then folder creation (and listing, moving, etc) can lead to
    # errors.
    #
    # From RFC2342:
    #
    #    Although typically a server will support only a single Personal
    #    Namespace, and a single Other User's Namespace, circumstances exist
    #    where there MAY be multiples of these, and a client MUST be prepared
    #    for them.  If a client is configured such that it is required to create
    #    a certain mailbox, there can be circumstances where it is unclear which
    #    Personal Namespaces it should create the mailbox in.  In these
    #    situations a client SHOULD let the user select which namespaces to
    #    create the mailbox in.
    #
    # Related: #list, Namespaces, Namespace
    #
    # ===== For example:
    #
    #    capabilities = imap.capability
    #    if capabilities.include?("NAMESPACE")
    #      namespaces = imap.namespace
    #      if namespace = namespaces.personal.first
    #        prefix = namespace.prefix  # e.g. "" or "INBOX."
    #        delim  = namespace.delim   # e.g. "/" or "."
    #        # personal folders should use the prefix and delimiter
    #        imap.create(prefix + "foo")
    #        imap.create(prefix + "bar")
    #        imap.create(prefix + %w[path to my folder].join(delim))
    #      end
    #    end
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +NAMESPACE+
    # [RFC2342[https://tools.ietf.org/html/rfc2342]].
    def namespace
      synchronize do
        send_command("NAMESPACE")
        return @responses.delete("NAMESPACE")[-1]
      end
    end

    # Sends a XLIST command, and returns a subset of names from
    # the complete set of all names available to the client.
    # +refname+ provides a context (for instance, a base directory
    # in a directory-based mailbox hierarchy).  +mailbox+ specifies
    # a mailbox or (via wildcards) mailboxes under that context.
    # Two wildcards may be used in +mailbox+: '*', which matches
    # all characters *including* the hierarchy delimiter (for instance,
    # '/' on a UNIX-hosted directory-based mailbox hierarchy); and '%',
    # which matches all characters *except* the hierarchy delimiter.
    #
    # If +refname+ is empty, +mailbox+ is used directly to determine
    # which mailboxes to match.  If +mailbox+ is empty, the root
    # name of +refname+ and the hierarchy delimiter are returned.
    #
    # The XLIST command is like the LIST command except that the flags
    # returned refer to the function of the folder/mailbox, e.g. :Sent
    #
    # The return value is an array of MailboxList objects. For example:
    #
    #   imap.create("foo/bar")
    #   imap.create("foo/baz")
    #   p imap.xlist("", "foo/%")
    #   #=> [#<Net::IMAP::MailboxList attr=[:Noselect], delim="/", name="foo/">, \\
    #        #<Net::IMAP::MailboxList attr=[:Noinferiors, :Marked], delim="/", name="foo/bar">, \\
    #        #<Net::IMAP::MailboxList attr=[:Noinferiors], delim="/", name="foo/baz">]
    #
    # Related: #list, MailboxList
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +XLIST+,
    # a deprecated Gmail extension (replaced by +SPECIAL-USE+).
    #--
    # TODO: Net::IMAP doesn't yet have full SPECIAL-USE support.  Supporting
    # servers MAY return SPECIAL-USE attributes, but are not *required* to
    # unless the SPECIAL-USE return option is supplied.
    #++
    def xlist(refname, mailbox)
      synchronize do
        send_command("XLIST", refname, mailbox)
        return @responses.delete("XLIST")
      end
    end

    # Sends a {GETQUOTAROOT command [RFC2087 §4.3]}[https://www.rfc-editor.org/rfc/rfc2087#section-4.3]
    # along with the specified +mailbox+.  This command is generally available
    # to both admin and user.  If this mailbox exists, it returns an array
    # containing objects of type MailboxQuotaRoot and MailboxQuota.
    #
    # Related: #getquota, #setquota, MailboxQuotaRoot, MailboxQuota
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +QUOTA+
    # [RFC2087[https://tools.ietf.org/html/rfc2087]].
    def getquotaroot(mailbox)
      synchronize do
        send_command("GETQUOTAROOT", mailbox)
        result = []
        result.concat(@responses.delete("QUOTAROOT"))
        result.concat(@responses.delete("QUOTA"))
        return result
      end
    end

    # Sends a {GETQUOTA command [RFC2087 §4.2]}[https://www.rfc-editor.org/rfc/rfc2087#section-4.2]
    # along with specified +mailbox+.  If this mailbox exists, then an array
    # containing a MailboxQuota object is returned.  This command is generally
    # only available to server admin.
    #
    # Related: #getquotaroot, #setquota, MailboxQuota
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +QUOTA+
    # [RFC2087[https://tools.ietf.org/html/rfc2087]].
    def getquota(mailbox)
      synchronize do
        send_command("GETQUOTA", mailbox)
        return @responses.delete("QUOTA")
      end
    end

    # Sends a {SETQUOTA command [RFC2087 §4.1]}[https://www.rfc-editor.org/rfc/rfc2087#section-4.1]
    # along with the specified +mailbox+ and +quota+.  If +quota+ is nil, then
    # +quota+ will be unset for that mailbox.  Typically one needs to be logged
    # in as a server admin for this to work.
    #
    # Related: #getquota, #getquotaroot
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +QUOTA+
    # [RFC2087[https://tools.ietf.org/html/rfc2087]].
    def setquota(mailbox, quota)
      if quota.nil?
        data = '()'
      else
        data = '(STORAGE ' + quota.to_s + ')'
      end
      send_command("SETQUOTA", mailbox, RawData.new(data))
    end

    # Sends a {SETACL command [RFC4314 §3.1]}[https://www.rfc-editor.org/rfc/rfc4314#section-3.1]
    # along with +mailbox+, +user+ and the +rights+ that user is to have on that
    # mailbox.  If +rights+ is nil, then that user will be stripped of any
    # rights to that mailbox.
    #
    # Related: #getacl
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +ACL+
    # [RFC4314[https://tools.ietf.org/html/rfc4314]].
    def setacl(mailbox, user, rights)
      if rights.nil?
        send_command("SETACL", mailbox, user, "")
      else
        send_command("SETACL", mailbox, user, rights)
      end
    end

    # Sends a {GETACL command [RFC4314 §3.3]}[https://www.rfc-editor.org/rfc/rfc4314#section-3.3]
    # along with a specified +mailbox+.  If this mailbox exists, an array
    # containing objects of MailboxACLItem will be returned.
    #
    # Related: #setacl, MailboxACLItem
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +ACL+
    # [RFC4314[https://tools.ietf.org/html/rfc4314]].
    def getacl(mailbox)
      synchronize do
        send_command("GETACL", mailbox)
        return @responses.delete("ACL")[-1]
      end
    end

    # Sends a {LSUB command [IMAP4rev1 §6.3.9]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.9]
    # and returns a subset of names from the set of names that the user has
    # declared as being "active" or "subscribed."  +refname+ and +mailbox+ are
    # interpreted as for #list.
    #
    # The return value is an array of MailboxList objects.
    #
    # Related: #subscribe, #unsubscribe, #list, MailboxList
    def lsub(refname, mailbox)
      synchronize do
        send_command("LSUB", refname, mailbox)
        return @responses.delete("LSUB")
      end
    end

    # Sends a {STATUS commands [IMAP4rev1 §6.3.10]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.10]
    # and returns the status of the indicated +mailbox+. +attr+ is a list of one
    # or more attributes whose statuses are to be requested.  Supported
    # attributes include:
    #
    #   MESSAGES:: the number of messages in the mailbox.
    #   RECENT:: the number of recent messages in the mailbox.
    #   UNSEEN:: the number of unseen messages in the mailbox.
    #
    # The return value is a hash of attributes. For example:
    #
    #   p imap.status("inbox", ["MESSAGES", "RECENT"])
    #   #=> {"RECENT"=>0, "MESSAGES"=>44}
    #
    # A Net::IMAP::NoResponseError is raised if status values
    # for +mailbox+ cannot be returned; for instance, because it
    # does not exist.
    def status(mailbox, attr)
      synchronize do
        send_command("STATUS", mailbox, attr)
        return @responses.delete("STATUS")[-1].attr
      end
    end

    # Sends an {APPEND command [IMAP4rev1 §6.3.11]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.3.11]
    # to append the +message+ to the end of the +mailbox+. The optional +flags+
    # argument is an array of flags initially passed to the new message.  The
    # optional +date_time+ argument specifies the creation time to assign to the
    # new message; it defaults to the current time.
    #
    # For example:
    #
    #   imap.append("inbox", <<EOF.gsub(/\n/, "\r\n"), [:Seen], Time.now)
    #   Subject: hello
    #   From: shugo@ruby-lang.org
    #   To: shugo@ruby-lang.org
    #
    #   hello world
    #   EOF
    #
    # A Net::IMAP::NoResponseError is raised if the mailbox does
    # not exist (it is not created automatically), or if the flags,
    # date_time, or message arguments contain errors.
    #
    # ===== Capabilities
    #
    # If +UIDPLUS+ [RFC4315[https://www.rfc-editor.org/rfc/rfc4315.html]] is
    # supported and the destination supports persistent UIDs, the server's
    # response should include an +APPENDUID+ response code with UIDPlusData.
    # This will report the UIDVALIDITY of the destination mailbox and the
    # assigned UID of the appended message.
    #
    #--
    # TODO: add MULTIAPPEND support
    #++
    def append(mailbox, message, flags = nil, date_time = nil)
      args = []
      if flags
        args.push(flags)
      end
      args.push(date_time) if date_time
      args.push(Literal.new(message))
      send_command("APPEND", mailbox, *args)
    end

    # Sends a {CHECK command [IMAP4rev1 §6.4.1]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.1]
    # to request a checkpoint of the currently selected mailbox.  This performs
    # implementation-specific housekeeping; for instance, reconciling the
    # mailbox's in-memory and on-disk state.
    #
    # Related: #idle, #noop
    def check
      send_command("CHECK")
    end

    # Sends a {CLOSE command [IMAP4rev1 §6.4.2]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.2]
    # to close the currently selected mailbox.  The CLOSE command permanently
    # removes from the mailbox all messages that have the <tt>\\Deleted</tt>
    # flag set.
    #
    # Related: #unselect
    def close
      send_command("CLOSE")
    end

    # Sends an {UNSELECT command [RFC3691 §2]}[https://www.rfc-editor.org/rfc/rfc3691#section-3]
    # {[IMAP4rev2 §6.4.2]}[https://www.rfc-editor.org/rfc/rfc9051#section-6.4.2]
    # to free the session resources for a mailbox and return to the
    # "_authenticated_" state.  This is the same as #close, except that
    # <tt>\\Deleted</tt> messages are not removed from the mailbox.
    #
    # Related: #close
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +UNSELECT+
    # [RFC3691[https://tools.ietf.org/html/rfc3691]].
    def unselect
      send_command("UNSELECT")
    end

    # Sends an {EXPUNGE command [IMAP4rev1 §6.4.3]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.3]
    # Sends a EXPUNGE command to permanently remove from the currently
    # selected mailbox all messages that have the \Deleted flag set.
    #
    # Related: #uid_expunge
    def expunge
      synchronize do
        send_command("EXPUNGE")
        return @responses.delete("EXPUNGE")
      end
    end

    # Sends a {UID EXPUNGE command [RFC4315 §2.1]}[https://www.rfc-editor.org/rfc/rfc4315#section-2.1]
    # {[IMAP4rev2 §6.4.9]}[https://www.rfc-editor.org/rfc/rfc9051#section-6.4.9]
    # to permanently remove all messages that have both the <tt>\\Deleted</tt>
    # flag set and a UID that is included in +uid_set+.
    #
    # By using #uid_expunge instead of #expunge when resynchronizing with
    # the server, the client can ensure that it does not inadvertantly
    # remove any messages that have been marked as <tt>\\Deleted</tt> by other
    # clients between the time that the client was last connected and
    # the time the client resynchronizes.
    #
    # *Note:*
    # >>>
    #        Although the command takes a set of UIDs for its argument, the
    #        server still returns regular EXPUNGE responses, which contain
    #        a <em>sequence number</em>. These will be deleted from
    #        #responses and this method returns them as an array of
    #        <em>sequence number</em> integers.
    #
    # Related: #expunge
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +UIDPLUS+
    # [RFC4315[https://www.rfc-editor.org/rfc/rfc4315.html]].
    def uid_expunge(uid_set)
      synchronize do
        send_command("UID EXPUNGE", MessageSet.new(uid_set))
        return @responses.delete("EXPUNGE")
      end
    end

    # Sends a {SEARCH command [IMAP4rev1 §6.4.4]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.4]
    # to search the mailbox for messages that match the given searching
    # criteria, and returns message sequence numbers.  +keys+ can either be a
    # string holding the entire search string, or a single-dimension array of
    # search keywords and arguments.
    #
    # Related: #uid_search
    #
    # ===== Search criteria
    #
    # For a full list of search criteria,
    # see [{IMAP4rev1 §6.4.4}[https://www.rfc-editor.org/rfc/rfc3501.html#section-6.4.4]],
    # or  [{IMAP4rev2 §6.4.4}[https://www.rfc-editor.org/rfc/rfc9051.html#section-6.4.4]],
    # in addition to documentation for
    # any [CAPABILITIES[https://www.iana.org/assignments/imap-capabilities/imap-capabilities.xhtml]]
    # reported by #capability which may define additional search filters, e.g:
    # +CONDSTORE+, +WITHIN+, +FILTERS+, <tt>SEARCH=FUZZY</tt>, +OBJECTID+, or
    # +SAVEDATE+.  The following are some common search criteria:
    #
    # <message set>:: a set of message sequence numbers.  "<tt>,</tt>" indicates
    #                 an interval, "+:+" indicates a range.  For instance,
    #                 "<tt>2,10:12,15</tt>" means "<tt>2,10,11,12,15</tt>".
    #
    # BEFORE <date>:: messages with an internal date strictly before
    #                 <b><date></b>.  The date argument has a format similar
    #                 to <tt>8-Aug-2002</tt>, and can be formatted using
    #                 Net::IMAP.format_date.
    #
    # BODY <string>:: messages that contain <string> within their body.
    #
    # CC <string>:: messages containing <string> in their CC field.
    #
    # FROM <string>:: messages that contain <string> in their FROM field.
    #
    # NEW:: messages with the \Recent, but not the \Seen, flag set.
    #
    # NOT <search-key>:: negate the following search key.
    #
    # OR <search-key> <search-key>:: "or" two search keys together.
    #
    # ON <date>:: messages with an internal date exactly equal to <date>,
    #             which has a format similar to 8-Aug-2002.
    #
    # SINCE <date>:: messages with an internal date on or after <date>.
    #
    # SUBJECT <string>:: messages with <string> in their subject.
    #
    # TO <string>:: messages with <string> in their TO field.
    #
    # ===== For example:
    #
    #   p imap.search(["SUBJECT", "hello", "NOT", "NEW"])
    #   #=> [1, 6, 7, 8]
    #
    def search(keys, charset = nil)
      return search_internal("SEARCH", keys, charset)
    end

    # Sends a {UID SEARCH command [IMAP4rev1 §6.4.8]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.8]
    # to search the mailbox for messages that match the given searching
    # criteria, and returns unique identifiers (<tt>UID</tt>s).
    #
    # See #search for documentation of search criteria.
    def uid_search(keys, charset = nil)
      return search_internal("UID SEARCH", keys, charset)
    end

    # Sends a {FETCH command [IMAP4rev1 §6.4.5]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.5]
    # to retrieve data associated with a message in the mailbox.
    #
    # The +set+ parameter is a number or a range between two numbers,
    # or an array of those.  The number is a message sequence number,
    # where -1 represents a '*' for use in range notation like 100..-1
    # being interpreted as '100:*'.  Beware that the +exclude_end?+
    # property of a Range object is ignored, and the contents of a
    # range are independent of the order of the range endpoints as per
    # the protocol specification, so 1...5, 5..1 and 5...1 are all
    # equivalent to 1..5.
    #
    # +attr+ is a list of attributes to fetch; see the documentation
    # for FetchData for a list of valid attributes.
    #
    # The return value is an array of FetchData or nil
    # (instead of an empty array) if there is no matching message.
    #
    # Related: #uid_search, FetchData
    #
    # ===== For example:
    #
    #   p imap.fetch(6..8, "UID")
    #   #=> [#<Net::IMAP::FetchData seqno=6, attr={"UID"=>98}>, \\
    #        #<Net::IMAP::FetchData seqno=7, attr={"UID"=>99}>, \\
    #        #<Net::IMAP::FetchData seqno=8, attr={"UID"=>100}>]
    #   p imap.fetch(6, "BODY[HEADER.FIELDS (SUBJECT)]")
    #   #=> [#<Net::IMAP::FetchData seqno=6, attr={"BODY[HEADER.FIELDS (SUBJECT)]"=>"Subject: test\r\n\r\n"}>]
    #   data = imap.uid_fetch(98, ["RFC822.SIZE", "INTERNALDATE"])[0]
    #   p data.seqno
    #   #=> 6
    #   p data.attr["RFC822.SIZE"]
    #   #=> 611
    #   p data.attr["INTERNALDATE"]
    #   #=> "12-Oct-2000 22:40:59 +0900"
    #   p data.attr["UID"]
    #   #=> 98
    def fetch(set, attr, mod = nil)
      return fetch_internal("FETCH", set, attr, mod)
    end

    # Sends a {UID FETCH command [IMAP4rev1 §6.4.8]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.8]
    # to retrieve data associated with a message in the mailbox.
    #
    # Similar to #fetch, but the +set+ parameter contains unique identifiers
    # instead of message sequence numbers.
    #
    # >>>
    #   *Note:* Servers _MUST_ implicitly include the +UID+ message data item as
    #   part of any +FETCH+ response caused by a +UID+ command, regardless of
    #   whether a +UID+ was specified as a message data item to the +FETCH+.
    #
    # Related: #fetch, FetchData
    def uid_fetch(set, attr, mod = nil)
      return fetch_internal("UID FETCH", set, attr, mod)
    end

    # Sends a {STORE command [IMAP4rev1 §6.4.6]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.6]
    # to alter data associated with messages in the mailbox, in particular their
    # flags. The +set+ parameter is a number, an array of numbers, or a Range
    # object. Each number is a message sequence number.  +attr+ is the name of a
    # data item to store: 'FLAGS' will replace the message's flag list with the
    # provided one, '+FLAGS' will add the provided flags, and '-FLAGS' will
    # remove them.  +flags+ is a list of flags.
    #
    # The return value is an array of FetchData
    #
    # Related: #uid_store
    #
    # ===== For example:
    #
    #   p imap.store(6..8, "+FLAGS", [:Deleted])
    #   #=> [#<Net::IMAP::FetchData seqno=6, attr={"FLAGS"=>[:Seen, :Deleted]}>, \\
    #        #<Net::IMAP::FetchData seqno=7, attr={"FLAGS"=>[:Seen, :Deleted]}>, \\
    #        #<Net::IMAP::FetchData seqno=8, attr={"FLAGS"=>[:Seen, :Deleted]}>]
    def store(set, attr, flags)
      return store_internal("STORE", set, attr, flags)
    end

    # Sends a {UID STORE command [IMAP4rev1 §6.4.8]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.8]
    # to alter data associated with messages in the mailbox, in particular their
    # flags.
    #
    # Similar to #store, but +set+ contains unique identifiers instead of
    # message sequence numbers.
    #
    # Related: #store
    def uid_store(set, attr, flags)
      return store_internal("UID STORE", set, attr, flags)
    end

    # Sends a {COPY command [IMAP4rev1 §6.4.7]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.7]
    # to copy the specified message(s) to the end of the specified destination
    # +mailbox+. The +set+ parameter is a number, an array of numbers, or a
    # Range object.  The number is a message sequence number.
    #
    # Related: #uid_copy
    #
    # ===== Capabilities
    #
    # If +UIDPLUS+ [RFC4315[https://www.rfc-editor.org/rfc/rfc4315.html]] is
    # supported, the server's response should include a +COPYUID+ response code
    # with UIDPlusData.  This will report the UIDVALIDITY of the destination
    # mailbox, the UID set of the source messages, and the assigned UID set of
    # the moved messages.
    def copy(set, mailbox)
      copy_internal("COPY", set, mailbox)
    end

    # Sends a {UID COPY command [IMAP4rev1 §6.4.8]}[https://www.rfc-editor.org/rfc/rfc3501#section-6.4.8]
    # to copy the specified message(s) to the end of the specified destination
    # +mailbox+.
    #
    # Similar to #copy, but +set+ contains unique identifiers.
    #
    # ===== Capabilities
    #
    # +UIDPLUS+ affects #uid_copy the same way it affects #copy.
    def uid_copy(set, mailbox)
      copy_internal("UID COPY", set, mailbox)
    end

    # Sends a {MOVE command [RFC6851 §3.1]}[https://www.rfc-editor.org/rfc/rfc6851#section-3.1]
    # {[IMAP4rev2 §6.4.8]}[https://www.rfc-editor.org/rfc/rfc9051#section-6.4.8]
    # to move the specified message(s) to the end of the specified destination
    # +mailbox+. The +set+ parameter is a number, an array of numbers, or a
    # Range object. The number is a message sequence number.
    #
    # Related: #uid_move
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +MOVE+
    # [RFC6851[https://tools.ietf.org/html/rfc6851]].
    #
    # If +UIDPLUS+ [RFC4315[https://www.rfc-editor.org/rfc/rfc4315.html]] is
    # supported, the server's response should include a +COPYUID+ response code
    # with UIDPlusData.  This will report the UIDVALIDITY of the destination
    # mailbox, the UID set of the source messages, and the assigned UID set of
    # the moved messages.
    #
    def move(set, mailbox)
      copy_internal("MOVE", set, mailbox)
    end

    # Sends a {UID MOVE command [RFC6851 §3.2]}[https://www.rfc-editor.org/rfc/rfc6851#section-3.2]
    # {[IMAP4rev2 §6.4.9]}[https://www.rfc-editor.org/rfc/rfc9051#section-6.4.9]
    # to move the specified message(s) to the end of the specified destination
    # +mailbox+.
    #
    # Similar to #move, but +set+ contains unique identifiers.
    #
    # Related: #move
    #
    # ===== Capabilities
    #
    # Same as #move: The server's capabilities must include +MOVE+
    # [RFC6851[https://tools.ietf.org/html/rfc6851]].  +UIDPLUS+ also affects
    # #uid_move the same way it affects #move.
    def uid_move(set, mailbox)
      copy_internal("UID MOVE", set, mailbox)
    end

    # Sends a {SORT command [RFC5256 §3]}[https://www.rfc-editor.org/rfc/rfc5256#section-3]
    # to search a mailbox for messages that match +search_keys+ and return an
    # array of message sequence numbers, sorted by +sort_keys+.  +search_keys+
    # are interpreted the same as for #search.
    #
    #--
    # TODO: describe +sort_keys+
    #++
    #
    # Related: #uid_sort, #search, #uid_search, #thread, #uid_thread
    #
    # ===== For example:
    #
    #   p imap.sort(["FROM"], ["ALL"], "US-ASCII")
    #   #=> [1, 2, 3, 5, 6, 7, 8, 4, 9]
    #   p imap.sort(["DATE"], ["SUBJECT", "hello"], "US-ASCII")
    #   #=> [6, 7, 8, 1]
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +SORT+
    # [RFC5256[https://tools.ietf.org/html/rfc5256]].
    def sort(sort_keys, search_keys, charset)
      return sort_internal("SORT", sort_keys, search_keys, charset)
    end

    # Sends a {UID SORT command [RFC5256 §3]}[https://www.rfc-editor.org/rfc/rfc5256#section-3]
    # to search a mailbox for messages that match +search_keys+ and return an
    # array of unique identifiers, sorted by +sort_keys+.  +search_keys+ are
    # interpreted the same as for #search.
    #
    # Related: #sort, #search, #uid_search, #thread, #uid_thread
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +SORT+
    # [RFC5256[https://tools.ietf.org/html/rfc5256]].
    def uid_sort(sort_keys, search_keys, charset)
      return sort_internal("UID SORT", sort_keys, search_keys, charset)
    end

    # Sends a {THREAD command [RFC5256 §3]}[https://www.rfc-editor.org/rfc/rfc5256#section-3]
    # to search a mailbox and return message sequence numbers in threaded
    # format, as a ThreadMember tree.  +search_keys+ are interpreted the same as
    # for #search.
    #
    # The supported algorithms are:
    #
    # ORDEREDSUBJECT:: split into single-level threads according to subject,
    #                  ordered by date.
    # REFERENCES:: split into threads by parent/child relationships determined
    #              by which message is a reply to which.
    #
    # Unlike #search, +charset+ is a required argument.  US-ASCII
    # and UTF-8 are sample values.
    #
    # Related: #uid_thread, #search, #uid_search, #sort, #uid_sort
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +THREAD+
    # [RFC5256[https://tools.ietf.org/html/rfc5256]].
    def thread(algorithm, search_keys, charset)
      return thread_internal("THREAD", algorithm, search_keys, charset)
    end

    # Sends a {UID THREAD command [RFC5256 §3]}[https://www.rfc-editor.org/rfc/rfc5256#section-3]
    # Similar to #thread, but returns unique identifiers instead of
    # message sequence numbers.
    #
    # Related: #thread, #search, #uid_search, #sort, #uid_sort
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +THREAD+
    # [RFC5256[https://tools.ietf.org/html/rfc5256]].
    def uid_thread(algorithm, search_keys, charset)
      return thread_internal("UID THREAD", algorithm, search_keys, charset)
    end

    # Sends an {IDLE command [RFC2177 §3]}[https://www.rfc-editor.org/rfc/rfc6851#section-3]
    # {[IMAP4rev2 §6.3.13]}[https://www.rfc-editor.org/rfc/rfc9051#section-6.3.13]
    # that waits for notifications of new or expunged messages.  Yields
    # responses from the server during the IDLE.
    #
    # Use #idle_done to leave IDLE.
    #
    # If +timeout+ is given, this method returns after +timeout+ seconds passed.
    # +timeout+ can be used for keep-alive.  For example, the following code
    # checks the connection for each 60 seconds.
    #
    #   loop do
    #     imap.idle(60) do |res|
    #       ...
    #     end
    #   end
    #
    # Related: #idle_done, #noop, #check
    #
    # ===== Capabilities
    #
    # The server's capabilities must include +IDLE+
    # [RFC2177[https://tools.ietf.org/html/rfc2177]].
    def idle(timeout = nil, &response_handler)
      raise LocalJumpError, "no block given" unless response_handler

      response = nil

      synchronize do
        tag = Thread.current[:net_imap_tag] = generate_tag
        put_string("#{tag} IDLE#{CRLF}")

        begin
          add_response_handler(&response_handler)
          @idle_done_cond = new_cond
          @idle_done_cond.wait(timeout)
          @idle_done_cond = nil
          if @receiver_thread_terminating
            raise @exception || Net::IMAP::Error.new("connection closed")
          end
        ensure
          unless @receiver_thread_terminating
            remove_response_handler(response_handler)
            put_string("DONE#{CRLF}")
            response = get_tagged_response(tag, "IDLE", @idle_response_timeout)
          end
        end
      end

      return response
    end

    # Leaves IDLE.
    #
    # Related: #idle
    def idle_done
      synchronize do
        if @idle_done_cond.nil?
          raise Net::IMAP::Error, "not during IDLE"
        end
        @idle_done_cond.signal
      end
    end

    # Adds a response handler. For example, to detect when
    # the server sends a new EXISTS response (which normally
    # indicates new messages being added to the mailbox),
    # add the following handler after selecting the
    # mailbox:
    #
    #   imap.add_response_handler { |resp|
    #     if resp.kind_of?(Net::IMAP::UntaggedResponse) and resp.name == "EXISTS"
    #       puts "Mailbox now has #{resp.data} messages"
    #     end
    #   }
    #
    def add_response_handler(handler = nil, &block)
      raise ArgumentError, "two Procs are passed" if handler && block
      @response_handlers.push(block || handler)
    end

    # Removes the response handler.
    def remove_response_handler(handler)
      @response_handlers.delete(handler)
    end

    private

    CRLF = "\r\n"      # :nodoc:
    PORT = 143         # :nodoc:
    SSL_PORT = 993   # :nodoc:

    @@debug = false

    # :call-seq:
    #    Net::IMAP.new(host, options = {})
    #
    # Creates a new Net::IMAP object and connects it to the specified
    # +host+.
    #
    # +options+ is an option hash, each key of which is a symbol.
    #
    # The available options are:
    #
    # port::  Port number (default value is 143 for imap, or 993 for imaps)
    # ssl::   If +options[:ssl]+ is true, then an attempt will be made
    #         to use SSL (now TLS) to connect to the server.
    #         If +options[:ssl]+ is a hash, it's passed to
    #         OpenSSL::SSL::SSLContext#set_params as parameters.
    # open_timeout:: Seconds to wait until a connection is opened
    # idle_response_timeout:: Seconds to wait until an IDLE response is received
    #
    # The most common errors are:
    #
    # Errno::ECONNREFUSED:: Connection refused by +host+ or an intervening
    #                       firewall.
    # Errno::ETIMEDOUT:: Connection timed out (possibly due to packets
    #                    being dropped by an intervening firewall).
    # Errno::ENETUNREACH:: There is no route to that network.
    # SocketError:: Hostname not known or other socket error.
    # Net::IMAP::ByeResponseError:: The connected to the host was successful, but
    #                               it immediately said goodbye.
    def initialize(host, port_or_options = {},
                   usessl = false, certs = nil, verify = true)
      super()
      @host = host
      begin
        options = port_or_options.to_hash
      rescue NoMethodError
        # for backward compatibility
        options = {}
        options[:port] = port_or_options
        if usessl
          options[:ssl] = create_ssl_params(certs, verify)
        end
      end
      @port = options[:port] || (options[:ssl] ? SSL_PORT : PORT)
      @tag_prefix = "RUBY"
      @tagno = 0
      @open_timeout = options[:open_timeout] || 30
      @idle_response_timeout = options[:idle_response_timeout] || 5
      @parser = ResponseParser.new
      @sock = tcp_socket(@host, @port)
      begin
        if options[:ssl]
          start_tls_session(options[:ssl])
          @usessl = true
        else
          @usessl = false
        end
        @responses = Hash.new([].freeze)
        @tagged_responses = {}
        @response_handlers = []
        @tagged_response_arrival = new_cond
        @continued_command_tag = nil
        @continuation_request_arrival = new_cond
        @continuation_request_exception = nil
        @idle_done_cond = nil
        @logout_command_tag = nil
        @debug_output_bol = true
        @exception = nil

        @greeting = get_response
        if @greeting.nil?
          raise Error, "connection closed"
        end
        if @greeting.name == "BYE"
          raise ByeResponseError, @greeting
        end

        @client_thread = Thread.current
        @receiver_thread = Thread.start {
          begin
            receive_responses
          rescue Exception
          end
        }
        @receiver_thread_terminating = false
      rescue Exception
        @sock.close
        raise
      end
    end

    def tcp_socket(host, port)
      s = Socket.tcp(host, port, :connect_timeout => @open_timeout)
      s.setsockopt(:SOL_SOCKET, :SO_KEEPALIVE, true)
      s
    rescue Errno::ETIMEDOUT
      raise Net::OpenTimeout, "Timeout to open TCP connection to " +
        "#{host}:#{port} (exceeds #{@open_timeout} seconds)"
    end

    def receive_responses
      connection_closed = false
      until connection_closed
        synchronize do
          @exception = nil
        end
        begin
          resp = get_response
        rescue Exception => e
          synchronize do
            @sock.close
            @exception = e
          end
          break
        end
        unless resp
          synchronize do
            @exception = EOFError.new("end of file reached")
          end
          break
        end
        begin
          synchronize do
            case resp
            when TaggedResponse
              @tagged_responses[resp.tag] = resp
              @tagged_response_arrival.broadcast
              case resp.tag
              when @logout_command_tag
                return
              when @continued_command_tag
                @continuation_request_exception =
                  RESPONSE_ERRORS[resp.name].new(resp)
                @continuation_request_arrival.signal
              end
            when UntaggedResponse
              record_response(resp.name, resp.data)
              if resp.data.instance_of?(ResponseText) &&
                  (code = resp.data.code)
                record_response(code.name, code.data)
              end
              if resp.name == "BYE" && @logout_command_tag.nil?
                @sock.close
                @exception = ByeResponseError.new(resp)
                connection_closed = true
              end
            when ContinuationRequest
              @continuation_request_arrival.signal
            end
            @response_handlers.each do |handler|
              handler.call(resp)
            end
          end
        rescue Exception => e
          @exception = e
          synchronize do
            @tagged_response_arrival.broadcast
            @continuation_request_arrival.broadcast
          end
        end
      end
      synchronize do
        @receiver_thread_terminating = true
        @tagged_response_arrival.broadcast
        @continuation_request_arrival.broadcast
        if @idle_done_cond
          @idle_done_cond.signal
        end
      end
    end

    def get_tagged_response(tag, cmd, timeout = nil)
      if timeout
        deadline = Time.now + timeout
      end
      until @tagged_responses.key?(tag)
        raise @exception if @exception
        if timeout
          timeout = deadline - Time.now
          if timeout <= 0
            return nil
          end
        end
        @tagged_response_arrival.wait(timeout)
      end
      resp = @tagged_responses.delete(tag)
      case resp.name
      when /\A(?:OK)\z/ni
        return resp
      when /\A(?:NO)\z/ni
        raise NoResponseError, resp
      when /\A(?:BAD)\z/ni
        raise BadResponseError, resp
      else
        raise UnknownResponseError, resp
      end
    end

    def get_response
      buff = String.new
      while true
        s = @sock.gets(CRLF)
        break unless s
        buff.concat(s)
        if /\{(\d+)\}\r\n/n =~ s
          s = @sock.read($1.to_i)
          buff.concat(s)
        else
          break
        end
      end
      return nil if buff.length == 0
      if @@debug
        $stderr.print(buff.gsub(/^/n, "S: "))
      end
      return @parser.parse(buff)
    end

    def record_response(name, data)
      unless @responses.has_key?(name)
        @responses[name] = []
      end
      @responses[name].push(data)
    end

    def send_command(cmd, *args, &block)
      synchronize do
        args.each do |i|
          validate_data(i)
        end
        tag = generate_tag
        put_string(tag + " " + cmd)
        args.each do |i|
          put_string(" ")
          send_data(i, tag)
        end
        put_string(CRLF)
        if cmd == "LOGOUT"
          @logout_command_tag = tag
        end
        if block
          add_response_handler(&block)
        end
        begin
          return get_tagged_response(tag, cmd)
        ensure
          if block
            remove_response_handler(block)
          end
        end
      end
    end

    def generate_tag
      @tagno += 1
      return format("%s%04d", @tag_prefix, @tagno)
    end

    def put_string(str)
      @sock.print(str)
      if @@debug
        if @debug_output_bol
          $stderr.print("C: ")
        end
        $stderr.print(str.gsub(/\n(?!\z)/n, "\nC: "))
        if /\r\n\z/n.match(str)
          @debug_output_bol = true
        else
          @debug_output_bol = false
        end
      end
    end

    def search_internal(cmd, keys, charset)
      if keys.instance_of?(String)
        keys = [RawData.new(keys)]
      else
        normalize_searching_criteria(keys)
      end
      synchronize do
        if charset
          send_command(cmd, "CHARSET", charset, *keys)
        else
          send_command(cmd, *keys)
        end
        return @responses.delete("SEARCH")[-1]
      end
    end

    def fetch_internal(cmd, set, attr, mod = nil)
      case attr
      when String then
        attr = RawData.new(attr)
      when Array then
        attr = attr.map { |arg|
          arg.is_a?(String) ? RawData.new(arg) : arg
        }
      end

      synchronize do
        @responses.delete("FETCH")
        if mod
          send_command(cmd, MessageSet.new(set), attr, mod)
        else
          send_command(cmd, MessageSet.new(set), attr)
        end
        return @responses.delete("FETCH")
      end
    end

    def store_internal(cmd, set, attr, flags)
      if attr.instance_of?(String)
        attr = RawData.new(attr)
      end
      synchronize do
        @responses.delete("FETCH")
        send_command(cmd, MessageSet.new(set), attr, flags)
        return @responses.delete("FETCH")
      end
    end

    def copy_internal(cmd, set, mailbox)
      send_command(cmd, MessageSet.new(set), mailbox)
    end

    def sort_internal(cmd, sort_keys, search_keys, charset)
      if search_keys.instance_of?(String)
        search_keys = [RawData.new(search_keys)]
      else
        normalize_searching_criteria(search_keys)
      end
      normalize_searching_criteria(search_keys)
      synchronize do
        send_command(cmd, sort_keys, charset, *search_keys)
        return @responses.delete("SORT")[-1]
      end
    end

    def thread_internal(cmd, algorithm, search_keys, charset)
      if search_keys.instance_of?(String)
        search_keys = [RawData.new(search_keys)]
      else
        normalize_searching_criteria(search_keys)
      end
      normalize_searching_criteria(search_keys)
      send_command(cmd, algorithm, charset, *search_keys)
      return @responses.delete("THREAD")[-1]
    end

    def normalize_searching_criteria(keys)
      keys.collect! do |i|
        case i
        when -1, Range, Array
          MessageSet.new(i)
        else
          i
        end
      end
    end

    def create_ssl_params(certs = nil, verify = true)
      params = {}
      if certs
        if File.file?(certs)
          params[:ca_file] = certs
        elsif File.directory?(certs)
          params[:ca_path] = certs
        end
      end
      if verify
        params[:verify_mode] = VERIFY_PEER
      else
        params[:verify_mode] = VERIFY_NONE
      end
      return params
    end

    def start_tls_session(params = {})
      unless defined?(OpenSSL::SSL)
        raise "SSL extension not installed"
      end
      if @sock.kind_of?(OpenSSL::SSL::SSLSocket)
        raise RuntimeError, "already using SSL"
      end
      begin
        params = params.to_hash
      rescue NoMethodError
        params = {}
      end
      context = SSLContext.new
      context.set_params(params)
      if defined?(VerifyCallbackProc)
        context.verify_callback = VerifyCallbackProc
      end
      @sock = SSLSocket.new(@sock, context)
      @sock.sync_close = true
      @sock.hostname = @host if @sock.respond_to? :hostname=
      ssl_socket_connect(@sock, @open_timeout)
      if context.verify_mode != VERIFY_NONE
        @sock.post_connection_check(@host)
      end
    end

  end
end

require_relative "imap/errors"
require_relative "imap/command_data"
require_relative "imap/data_encoding"
require_relative "imap/flags"
require_relative "imap/response_data"
require_relative "imap/response_parser"
require_relative "imap/authenticators"
require_relative "imap/sasl"
