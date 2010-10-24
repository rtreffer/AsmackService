package com.googlecode.asmack.connection;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XmppException;

/**
 * Runnable to execute ping request in the background.
 */
public class PingRunnable implements Runnable {

    /**
     * The connection to use for ping.
     */
    private final Connection connection;

    /**
     * Create a new ping runnable bound to a given output connection.
     * @param connection The ping output connection.
     */
    public PingRunnable(Connection connection) {
        this.connection = connection;
    }

    /**
     * Execute the ping on the given connection.
     */
    @Override
    public void run() {
        Stanza stanza = new Stanza(
                "iq",
                "",
                "",
                "<iq from='" +
                connection.getResourceJid() +
                "' id='ping_" +
                Long.toHexString((int)(Integer.MAX_VALUE * Math.random()))
                + "'><ping xmlns='urn:xmpp:ping'/></iq>",
                null
        );
        try {
            connection.send(stanza);
        } catch (XmppException e) {
            /* PING is non critical */
        }
    }

}
