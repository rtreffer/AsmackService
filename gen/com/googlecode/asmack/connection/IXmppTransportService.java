/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/treffer/workspace/AsmackService/src/com/googlecode/asmack/connection/IXmppTransportService.aidl
 */
package com.googlecode.asmack.connection;
/**
 * Service interface for a xmpp service.
 */
public interface IXmppTransportService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.googlecode.asmack.connection.IXmppTransportService
{
private static final java.lang.String DESCRIPTOR = "com.googlecode.asmack.connection.IXmppTransportService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.googlecode.asmack.connection.IXmppTransportService interface,
 * generating a proxy if needed.
 */
public static com.googlecode.asmack.connection.IXmppTransportService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.googlecode.asmack.connection.IXmppTransportService))) {
return ((com.googlecode.asmack.connection.IXmppTransportService)iin);
}
return new com.googlecode.asmack.connection.IXmppTransportService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_tryLogin:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.tryLogin(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_send:
{
data.enforceInterface(DESCRIPTOR);
com.googlecode.asmack.Stanza _arg0;
if ((0!=data.readInt())) {
_arg0 = com.googlecode.asmack.Stanza.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.send(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getFullJidByBare:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getFullJidByBare(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.googlecode.asmack.connection.IXmppTransportService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Try to login with a given jid/password pair. Returns true on success.
     * @param jid The user jid.
     * @param password The user password.
     */
public boolean tryLogin(java.lang.String jid, java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(jid);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_tryLogin, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Send a stanza via this service, return true on successfull delivery to
     * the network buffer (plus flush). Please note that some phones ignore
     * flush request, thus "true" doesn't mean "on the wire".
     * @param stanza The stanza to send.
     */
public boolean send(com.googlecode.asmack.Stanza stanza) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((stanza!=null)) {
_data.writeInt(1);
stanza.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_send, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Scan all connections for the current connection of the given jid and
     * return the full resource jid for the user.
     * @return The full user jid (including resource).
     */
public java.lang.String getFullJidByBare(java.lang.String bare) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(bare);
mRemote.transact(Stub.TRANSACTION_getFullJidByBare, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_tryLogin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_send = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getFullJidByBare = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * Try to login with a given jid/password pair. Returns true on success.
     * @param jid The user jid.
     * @param password The user password.
     */
public boolean tryLogin(java.lang.String jid, java.lang.String password) throws android.os.RemoteException;
/**
     * Send a stanza via this service, return true on successfull delivery to
     * the network buffer (plus flush). Please note that some phones ignore
     * flush request, thus "true" doesn't mean "on the wire".
     * @param stanza The stanza to send.
     */
public boolean send(com.googlecode.asmack.Stanza stanza) throws android.os.RemoteException;
/**
     * Scan all connections for the current connection of the given jid and
     * return the full resource jid for the user.
     * @return The full user jid (including resource).
     */
public java.lang.String getFullJidByBare(java.lang.String bare) throws android.os.RemoteException;
}
