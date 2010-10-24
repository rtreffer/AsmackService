package com.googlecode.asmack.connection;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XmppException;

/**
 * A runnable to move the presence update into the background, reducing the
 * total roundtrip of a presence updates.
 */
public class PresenceRunnable implements Runnable {

    /**
     * The connection for the presence update.
     */
    private final Connection connection;

    /**
     * Create a new presence runnable for a given connection.
     * @param connection The output connection for the presence update.
     */
    public PresenceRunnable(Connection connection) {
        this.connection = connection;
    }

    /**
     * Execute the presence update.
     */
    @Override
    public void run() {
        Stanza stanza = new Stanza(
                "presence",
                "",
                "",
                "<presence />",
                null
        );
        try {
            connection.send(stanza);
        } catch (XmppException e) {
            /* PING is non critical */
        }
    }

}
