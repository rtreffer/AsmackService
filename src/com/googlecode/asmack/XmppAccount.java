package com.googlecode.asmack;

/**
 * Xmpp Account metadata. This class holds passwords, jids and connection
 * strings.
 */
public class XmppAccount {

    /**
     * The user jid as username@domain.tld.
     */
    private String jid;

    /**
     * The user password.
     */
    private String password;

    /**
     * The connection string in any form accepted by the ConnectionFactory.
     */
    private String connection;

    /**
     * The preferred account resource. The server may alter this. The actual
     * resource can be queried via the connection interface.
     */
    private String resource;

    /**
     * Boolean representing the roster sync and contacts integration status.
     */
    private boolean rosterSyncEnabled;

    /**
     * The last roster version, if available.
     */
    private String rosterVersion;

    /**
     * Retrieve the account user jid.
     * @return A jid matching username@domain.tld.
     */
    public String getJid() {
        return jid;
    }

    /**
     * Change the account user jid-
     * @param jid The new jid.
     */
    public void setJid(String jid) {
        this.jid = jid;
    }

    /**
     * Retrieve the account password.
     * @return The account password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the account password. This does not affect any server passwords.
     * @param password The account password to be used.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieve the connection string.
     * @return The connection string.
     */
    public String getConnection() {
        return connection;
    }

    /**
     * Change the connection string.
     * @param connection The new connection string.
     */
    public void setConnection(String connection) {
        this.connection = connection;
    }

    /**
     * Retrieve the preferred resource name.
     * @return The preferred resource name.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Change the preferred resource name.
     * @param resource The new preferred resource name.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Retrieve the sync roster and contacts integration state.
     * @return True if roster sync is enabled.
     */
    public boolean isRosterSyncEnabled() {
        return rosterSyncEnabled;
    }

    /**
     * Set the roster sync state (enabled/disabled).
     * @param rosterSyncEnabled The new roster sync state.
     */
    public void setRosterSyncEnabled(boolean rosterSyncEnabled) {
        this.rosterSyncEnabled = rosterSyncEnabled;
    }

    /**
     * Retrieve the latest roster version.
     * @return The last received roster version string.
     */
    public String getRosterVersion() {
        return rosterVersion;
    }

    /**
     * Set the last received roster version string.
     * @param rosterVersion The new roster version string.
     */
    public void setRosterVersion(String rosterVersion) {
        this.rosterVersion = rosterVersion;
    }

}
