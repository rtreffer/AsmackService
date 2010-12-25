package com.googlecode.asmack;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * XmppIdentity defines the internal and on the wire format for identities as
 * defined in xep-0030 (Service Discovery) and
 * http://xmpp.org/registrar/disco-categories.html (Service Discovery
 * Categories).
 */
public class XmppIdentity implements Parcelable {

    /**
     * The service discovery category.
     */
    private String category = "";

    /**
     * The service discovery type.
     */
    private String type = "";

    /**
     * The service discovery identity language.
     */
    private String lang = "";

    /**
     * The service discovery identity name (value).
     */
    private String name = "";

    /**
     * Create a new identity instance.
     */
    public XmppIdentity() {
    }

    /**
     * Create a new xmpp service discovery identity.
     * @param category The identity category.
     * @param type The identity type.
     * @param lang The identity language.
     * @param name The identity name.
     */
    public XmppIdentity(
        String category,
        String type,
        String lang,
        String name
    ) {
        setCategory(category);
        setType(type);
        setLang(lang);
        setName(name);
    }

    /**
     * Deserialize a parcel into a new XmppIdentity.
     * @param source The source parcel.
     */
    public XmppIdentity(Parcel source) {
        this(source.readString(), source.readString(),
             source.readString(), source.readString());
    }

    /**
     * Retrieve the xmpp identity category.
     * @return The identity category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the xmpp identity category.
     * @param category The new identity category.
     */
    public void setCategory(String category) {
        if (category == null) {
            category = "";
        }
        this.category = category;
    }

    /**
     * Retrieve the xmpp identity type.
     * @return The identity type.
     */
    public String getType() {
        return type;
    }

    /**
     * Set the xmpp identity type.
     * @param type The new identity type.
     */
    public void setType(String type) {
        if (type == null) {
            type = "";
        }
        this.type = type;
    }

    /**
     * Retrieve the xmpp identity language.
     * @return The identity language.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Set the xmpp identity language.
     * @param lang The new identity language.
     */
    public void setLang(String lang) {
        if (lang == null) {
            lang = "";
        }
        this.lang = lang;
    }

    /**
     * Retrieve the xmpp identity name (often used as value).
     * @return The identity name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the xmpp identity name (or value).
     * @param name The new identity name.
     */
    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    /**
     * Retrieve the special flags of this parcelable. None.
     * @return 0. (No special flags)
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Persist the xmpp identity to a Parcel.
     * @param dest The target parcel.
     * @param flags Ignored
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(type);
        dest.writeString(lang);
        dest.writeString(name);
    }

    /**
     * A Parcelable.Creator for reading XmppIdentity objects. Named CREATOR as
     * required by the parcelable convention.
     */
    public final static Parcelable.Creator<XmppIdentity> CREATOR =
        new Creator<XmppIdentity>() {

        /**
         * Create a new XmppIdentity array of the given size.
         * @param size The size of the returned array.
         * @return A new XmppIdentity array of length size.
         */
        @Override
        public XmppIdentity[] newArray(int size) {
            return new XmppIdentity[size];
        }

        /**
         * Create a new XmppIdentity with the values of the source parcel. This
         * method delegates to the XmppIdentity(Parcel) constructor.
         * @param source The source parcel.
         */
        @Override
        public XmppIdentity createFromParcel(Parcel source) {
            return new XmppIdentity(source);
        }

    };

}
