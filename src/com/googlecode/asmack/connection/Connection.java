package com.googlecode.asmack.connection;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.StanzaSink;
import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.XmppException;

/**
 * Connection interface for all XMPP connection types.
 */
public interface Connection {

    /**
     * Try to connect and bind the given stanza sink on success.
     * @param sink The target stanza sink.
     * @throws XmppException On error.
     */
    void connect(StanzaSink sink) throws XmppException;

    /**
     * Return the full resource jid of this connection.
     * @return The full resource jid.
     */
    String getResourceJid();

    /**
     * Send a single stanza, throwing a XmppException on error.
     * @param stanza The stanza to send.
     * @throws XmppException On error.
     */
    void send(Stanza stanza) throws XmppException;

    /**
     * Close the current connection.
     * @throws XmppException On error.
     */
    void close() throws XmppException;

    /***
     * Return the timestamp of the last received stanza.
     * @return The timestamp of the last received stanza.
     */
    long lastReceive();

    /**
     * Retrieve the bound account.
     * @return The account used by this connection.
     */
    XmppAccount getAccount();

    /**
     * Check if this connection has been closed.
     * @return True if close() has been called.
     */
    boolean isClosed();

}
