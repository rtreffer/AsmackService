package com.googlecode.asmack;

import com.googlecode.asmack.connection.Connection;

/**
 * A stanza input receiver. Called after a stanza is successfully read or on
 * every read abortion. The sink is the receiving endpoint of a xmpp
 * connection.
 */
public interface StanzaSink {

    /**
     * Called whenever a stanza was received.
     * @param stanza The received Stanza.
     */
    void receive(Stanza stanza);

    /**
     * Called on read connection abortion or XML stream errors.
     * @param connection The failed connection.
     * @param exception The causing exception, wrapped into XMPP logic.
     */
    void connectionFailed(Connection connection, XmppException exception);

}
