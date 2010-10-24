package com.googlecode.asmack.connection;

import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.connection.impl.TcpConnection;
import com.googlecode.asmack.connection.impl.XmppConnection;

/**
 * Factory to create new connections based on connection configurations.
 */
public class ConnectionFactory {

    /**
     * Create a new connection for "tcp:" and "xmpp:" connection strings.
     * @param account The xmpp account used for the connect.
     * @return A new connection instance.
     */
    public final static Connection createConnection(XmppAccount account) {
        String connectionUri = account.getConnection();
        if (connectionUri.startsWith("tcp:")) {
            return new TcpConnection(account);
        }
        if (connectionUri.startsWith("xmpp:")) {
            return new XmppConnection(account);
        }
        return null;
    }

}
