package com.googlecode.asmack.contacts;

import android.provider.ContactsContract.CommonDataKinds.Nickname;

/**
 * <p>An object oriented representation of a nickname column.</p>
 * <p>The Android mapping is
 * <ul>
 *   <li>DATA1: nickname value</li>
 *   <li>DATA2: nickname type</li>
 *   <li>DATA3: label for custom nickname type</li>
 * </ul>
 * </p>
 */
public class NicknameMetadata extends Metadata {

    /**
     * The nickname mime type ({@link Nickname#CONTENT_ITEM_TYPE}).
     */
    public static final String MIMETYPE = Nickname.CONTENT_ITEM_TYPE;

    /**
     * The nickname type definition.
     */
    public static enum Type {
        /**
         * Custom nickname type.
         */
        CUSTOM(Nickname.TYPE_CUSTOM),
        /**
         * Default nickname type.
         */
        DEFAULT(Nickname.TYPE_DEFAULT),
        /**
         * Other / unspecified nickname type.
         */
        OTHER_NAME(Nickname.TYPE_OTHER_NAME),
        /**
         * Maiden name type.
         */
        MAIDEN_NAME(Nickname.TYPE_MAINDEN_NAME),
        /**
         * Short name type.
         */
        SHORT_NAME(Nickname.TYPE_SHORT_NAME),
        /**
         * Name initials type.
         */
        INITIALS(Nickname.TYPE_INITIALS);

        /**
         * The internal enum value.
         */
        private final int value;

        /**
         * Create a new enum value, wrapping a given low level value.
         * @param value The wrapped value.
         */
        Type(int value) {
            this.value = value;
        }

        /**
         * Retrieve an nickname type for a given id. 
         * @param id The id in question.
         * @return The resolved enum type, or null.
         */
        private static Type byTypeId(int id) {
            switch(id) {
            case Nickname.TYPE_CUSTOM: return Type.CUSTOM;
            case Nickname.TYPE_DEFAULT: return Type.DEFAULT;
            case Nickname.TYPE_OTHER_NAME: return Type.OTHER_NAME;
            case Nickname.TYPE_MAINDEN_NAME: return Type.MAIDEN_NAME;
            case Nickname.TYPE_SHORT_NAME: return Type.SHORT_NAME;
            case Nickname.TYPE_INITIALS: return Type.INITIALS;
            }
            return null;
        }

        /**
         * Retrieve the internal wrapped enum value
         * @return
         */
        public int getValue() {
            return value;
        }
    };

    /**
     * Create a new nickname metadata instance of the default type-
     */
    public NicknameMetadata() {
        mimetype = MIMETYPE;
        setType(Type.DEFAULT);
    }

    /**
     * Retrieve the current nickname.
     * @return The current nickname.
     */
    public String getNickname() {
        return getData(0);
    }

    /**
     * Change the current nickname (DATA1 / setData(0)).
     * @param nickname The new nickname.
     */
    public void setNickname(String nickname) {
        setData(0, nickname);
    }

    /**
     * Retrieve the current nickname type.
     * @return The current nickname type.
     */
    public Type getType() {
        return Type.byTypeId(Integer.parseInt(getData(1)));
    }

    /**
     * Change the nickname type.
     * @param type The new nickname type.
     */
    public void setType(Type type) {
        setData(1, Integer.toString(type.getValue()));
    }

    /**
     * Change the custom label. Requires the type to be {@link Type#CUSTOM}.
     * @param name The custom label.
     */
    public void setCustomLabel(String name) {
        setData(2, name);
    }

    /**
     * Retrieve the custom label. Requires the type to be {@link Type#CUSTOM}.
     * @return The custom label.
     */
    public String getCustomLabel() {
        return getData(2);
    }

    /**
     * Setting the mimetype is not allowed for the nickname metadata type.
     * @param mimetype The new denied mimetype.
     */
    @Override
    public void setMimetype(String mimetype) {
        throw new UnsupportedOperationException("Mimetype of Nickname is " + MIMETYPE);
    }

}
