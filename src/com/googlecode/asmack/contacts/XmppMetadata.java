package com.googlecode.asmack.contacts;

public class XmppMetadata extends Metadata {

    public final static String MIMETYPE =
        "vnd.android.cursor.item/vnd.xmpp.profile";

    public XmppMetadata() {
        mimetype = MIMETYPE;
        setSummary("Xmpp-Profil");
        setDetail("Show Profile");
    }

    public void setJid(String jid) {
        setDetail("Show " + jid);
        setData(0, jid);
    }

    public String getJid() {
        return getData(0);
    }

    public void setSummary(String summary) {
        setData(1, summary);
    }

    public String getSummary() {
        return getData(1);
    }

    public void setDetail(String detail) {
        setData(2, detail);
    }

    public String getDetail() {
        return getData(2);
    }
}
