package com.googlecode.asmack;

/**
 * Malformed xml or xmpp stream exception. Thrown whenever the xml stream
 * input breaks.
 */
public class XmppMalformedException extends XmppException {

    /**
     * SerialVersionUID as required by serializable.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Chain an exception with a given error message.
     * @param detailMessage The detailed error message.
     * @param throwable The cause for the exception.
     */
    public XmppMalformedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Create a new XmppMalformedException with the given message.
     * @param detailMessage The detailed error message.
     */
    public XmppMalformedException(String detailMessage) {
        super(detailMessage);
    }

}
