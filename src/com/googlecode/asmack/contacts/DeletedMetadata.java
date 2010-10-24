package com.googlecode.asmack.contacts;

/**
 * Helper class to symbolize deleted metadata.
 */
public class DeletedMetadata extends Metadata {

    /**
     * Create a new deleted metadata instance for a give metadata id.
     * @param _ID The metadata id to be removed.
     */
    public DeletedMetadata(long _ID) {
        ID = _ID;
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     * @param ID Ignored.
     */
    @Override
    public void setID(long ID) {
        throw new UnsupportedOperationException("DeletedMetadata is immutable");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     * @param rawContactID Ignored.
     */
    @Override
    public void setRawContactID(long rawContactID) {
        throw new UnsupportedOperationException("DeletedMetadata is immutable");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     * @param mimetype Ignored.
     */
    @Override
    public void setMimetype(String mimetype) {
        throw new UnsupportedOperationException("DeletedMetadata is immutable");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     * @param index Ignored.
     * @param value Ignored.
     */
    @Override
    public void setData(int index, String value) {
        throw new UnsupportedOperationException("DeletedMetadata is immutable");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     * @param index Ignored.
     * @param value Ignored.
     */
    @Override
    public void setSync(int index, String value) {
        throw new UnsupportedOperationException("DeletedMetadata is immutable");
    }

}
