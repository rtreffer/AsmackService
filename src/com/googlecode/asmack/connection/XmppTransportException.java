package com.googlecode.asmack.connection;

import com.googlecode.asmack.XmppException;

/**
 * A xmpp transport exception will be thrown in case of lowleve io problems.
 */
public class XmppTransportException extends XmppException {

    /**
     * SerialVersionUID as required by serializable.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Chain an exception with a given error description.
     * @param detailMessage The detailed error description.
     * @param throwable The cause of this exception.
     */
    public XmppTransportException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Create a plain XmppTransportException based on an error description.
     * @param detailMessage The detailed error description.
     */
    public XmppTransportException(String detailMessage) {
        super(detailMessage);
    }

}
