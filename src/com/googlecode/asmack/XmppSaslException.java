package com.googlecode.asmack;

/**
 * XmppSaslException is thrown whenever the authentification failes.
 */
public class XmppSaslException extends XmppException {

    /**
     * SerialVersionUID as required by serializable.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Chain an exception with a given error message.
     * @param detailMessage The detailed error message.
     * @param throwable The cause for the exception.
     */
    public XmppSaslException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Create a new XmppSaslException with the given message.
     * @param detailMessage The detailed error message.
     */
    public XmppSaslException(String detailMessage) {
        super(detailMessage);
    }

}
