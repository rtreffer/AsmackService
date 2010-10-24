package com.googlecode.asmack.connection.impl;

import android.util.Log;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.StanzaSink;
import com.googlecode.asmack.XmppException;
import com.googlecode.asmack.connection.Connection;

/**
 * Transform {@link XmppInputStream#nextStanza()} pull events into
 * {@link StanzaSink#receive(Stanza)} events.
 */
public class ConncetionPullToSinkPushThread extends Thread {

    /**
     * Class debugging tag (ConnectionPullToSinkPushThread).
     * Value: {@value TAG}
     */
    private static final String TAG = ConncetionPullToSinkPushThread.class
                                        .getSimpleName();

    /**
     * Sink to receive events.
     */
    private final StanzaSink sink;

    /**
     * The xmpp input stream used for reading. 
     */
    private final XmppInputStream xmppInput;

    /**
     * The lowleve connection uswed by the xmpp input stream.
     */
    private final Connection connection;

    /**
     * <p>Create a new Thread to pull stanzas from a {@link XmppInputStream}
     * and push it to a {@link StanzaSink}.</p>
     *
     * <p> This Thread must be explicitly explicitly started (as required for
     * polymorphism).</p>
     *
     * @param connection The symbolic {@link Connection}.
     * @param xmppInput The {@link XmppInputStream} for the connection.
     * @param sink The receiving {@link StanzaSink}.
     */
    public ConncetionPullToSinkPushThread(
        Connection connection,
        XmppInputStream xmppInput,
        StanzaSink sink
    ) {
        this.connection = connection;
        this.xmppInput = xmppInput;
        this.sink = sink;
    }

    /**
     * <p>Run the main pull/push loop.</p>
     * <p>The {@link XmppInputStream#nextStanza()} to
     * {@link StanzaSink#receive(Stanza)} will run until a
     * {@link XmppException} is received.</p>
     */
    @Override
    public void run() {
        String resourceJid = connection.getResourceJid();
        try {
            while (true) {
                Stanza stanza = xmppInput.nextStanza();
                stanza.setVia(resourceJid);
                sink.receive(stanza);
            }
        } catch (XmppException e) {
            Log.e(TAG, "Connection aborted", e);
            sink.connectionFailed(connection, e);
        }
    }

}
