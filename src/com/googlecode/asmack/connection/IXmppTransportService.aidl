package com.googlecode.asmack.connection;

import com.googlecode.asmack.Stanza;

/**
 * Service interface for a xmpp service.
 */
interface IXmppTransportService {

    /**
     * Try to login with a given jid/password pair. Returns true on success.
     * @param jid The user jid.
     * @param password The user password.
     */
    boolean tryLogin(String jid, String password);
    /**
     * Send a stanza via this service, return true on successfull delivery to
     * the network buffer (plus flush). Please note that some phones ignore
     * flush request, thus "true" doesn't mean "on the wire".
     * @param stanza The stanza to send.
     */
    boolean send(in Stanza stanza);
    /**
     * Scan all connections for the current connection of the given jid and
     * return the full resource jid for the user.
     * @return The full user jid (including resource).
     */
    String getFullJidByBare(String bare);

}
