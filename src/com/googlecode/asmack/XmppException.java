package com.googlecode.asmack;

/**
 * Root exception for all xmpp related exceptions. Chaining exception without
 * a message is forbidden.
 */
public class XmppException extends Exception {

    /**
     * SerialVersionUID as required by serializable.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Chain an exception as a XMPP exception.
     * @param detailMessage The detailed error description.
     * @param throwable The cause of this exception.
     */
    public XmppException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Create a plain XmppException based on an error description.
     * @param detailMessage The detailed error description.
     */
    public XmppException(String detailMessage) {
        super(detailMessage);
    }

}
