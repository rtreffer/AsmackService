package com.googlecode.asmack;

/**
 * XMPPUtils provide helper for xmpp specific functionality like jid splitting.
 */
public class XMPPUtils {

    /**
     * Get the bare jid (username@domain.tld) out of a resource jid
     * (username@domain.tld/resource).
     * @param resourceJid The resource jid.
     * @return A bare jid representation of the jid.
     */
    public final static String getBareJid(String resourceJid) {
        int index = resourceJid.indexOf('/');
        if (index == -1) {
            return resourceJid;
        }
        return resourceJid.substring(0, index);
    }

    /**
     * Retrieve the domain out of a full resource jid or bare jid.
     * @param jid The full or bare jid.
     * @return The contained domain.
     */
    public static String getDomain(String jid) {
        String bareJid = getBareJid(jid);
        return bareJid.substring(bareJid.indexOf('@') + 1);
    }

    /**
     * Retrieve the username of a full or bare jid.
     * @param jid The full or bare jud.
     * @return The username part of the jid.
     */
    public static String getUser(String jid) {
        String bareJid = getBareJid(jid);
        int index = bareJid.indexOf('@');
        if (index <= 0) {
            return null;
        }
        return jid.substring(0, index);
    }

}
