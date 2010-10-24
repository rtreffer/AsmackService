package com.googlecode.asmack.contacts;

import android.provider.ContactsContract.StatusUpdates;

import com.googlecode.asmack.contacts.ImMetadata.Protocol;

/**
 * A status update represents a short message as well as an online status
 * for instant messaging. StatusUpdate refers to status updates of a single
 * instant messaging metadata of a remote raw contact.
 */
public class StatusUpdate {

    /**
     * The presence status type.
     */
    public static enum Presence {

        /**
         * Contact is offline.
         */
        OFFLINE(StatusUpdates.OFFLINE),

        /**
         * Contact is invisible.
         */
        INVISIBLE(StatusUpdates.INVISIBLE),

        /**
         * Contact is away.
         */
        AWAY(StatusUpdates.AWAY),

        /**
         * Contact is idle.
         */
        IDLE(StatusUpdates.IDLE),

        /**
         * Contact doesn't want to be disturbed.
         */
        DO_NOT_DISTURB(StatusUpdates.DO_NOT_DISTURB),

        /**
         * Contact is available.
         */
        AVAILABLE(StatusUpdates.AVAILABLE);

        /**
         * The internal presence value, as stored in the database.
         */
        private final int value;

        /**
         * Create a new presence, wrapping a the given raw value.
         * @param value The database value for this type.
         */
        Presence(int value) {
            this.value = value;
        }

        /**
         * Retrieve a type instalce for a given database value, or null.
         * @param id The database value.
         * @return A corresponding Presence instance, or null.
         */
        public static Presence byPresenceId(int id) {
            switch(id) {
            case StatusUpdates.OFFLINE: return OFFLINE;
            case StatusUpdates.INVISIBLE: return INVISIBLE;
            case StatusUpdates.AWAY: return AWAY;
            case StatusUpdates.IDLE: return IDLE;
            case StatusUpdates.DO_NOT_DISTURB: return DO_NOT_DISTURB;
            case StatusUpdates.AVAILABLE: return AVAILABLE;
            }
            return null;
        }

        /**
         * Retrieve the database value for this presence type.
         * @return The database value of this presence type.
         */
        public int getValue() {
            return value;
        }

    }

    /**
     * The data field id.
     */
    private long dataId;

    /**
     * The status update protocol.
     */
    private Protocol protocol = Protocol.JABBER;

    /**
     * The presence status.
     */
    private Presence presence = Presence.OFFLINE;

    /**
     * The im handle, the remote jid.
     */
    private String imHandle;

    /**
     * The im acocunt, the local jid.
     */
    private String imAccount;

    /**
     * The status text.
     */
    private String status;

    /**
     * The unix timestamp of the last status update.
     */
    private long timestamp;

    /**
     * Retrieve the associated metadata data row id.
     * @return The metadata data row id.
     */
    public long getDataId() {
        return dataId;
    }

    /**
     * Change the associated metadata data row id.
     * @param dataId The new metadata data row id.
     */
    public void setDataId(long dataId) {
        this.dataId = dataId;
    }

    /**
     * Retrieve the current protocol
     * @return The im protocol.
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Change the im protocol.
     * @param protocol The new im protocol.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Retrieve the presence status.
     * @return The presence status.
     */
    public Presence getPresence() {
        return presence;
    }

    /**
     * Change the presence status.
     * @param presence The new presence status.
     */
    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    /**
     * Retrieve the im hanldle, the remote jid.
     * @return The im handle, the remote jid.
     */
    public String getImHandle() {
        return imHandle;
    }

    /**
     * Change the im handle, the remote jid.
     * @param imHandle The new im handle, the remote jid.
     */
    public void setImHandle(String imHandle) {
        this.imHandle = imHandle;
    }

    /**
     * Retrieve the im account, the local jid.
     * @return The im account, the local jid.
     */
    public String getImAccount() {
        return imAccount;
    }

    /**
     * Change the im account, the local jid.
     * @param imAccount The new local jid.
     */
    public void setImAccount(String imAccount) {
        this.imAccount = imAccount;
    }

    /**
     * Retrieve the current status string.
     * @return The current status string.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Change the status string.
     * @param status The new status string.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retrieve the unix timestamp of the last status update.
     * @return The unix timestamp of the last status update.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Change the timestamp of the last status update.
     * @param timestamp The new timestamp of the last status update.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
