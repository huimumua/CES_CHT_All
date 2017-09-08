/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Volumes/Data/chiapin/MQTT/ZwaveControlServer/app/src/main/aidl/com/askey/zwave/control/IZwaveControlInterface.aidl
 */
package com.askey.zwave.control;
// Declare any non-default types here with import statements

public interface IZwaveControlInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.askey.zwave.control.IZwaveControlInterface
{
private static final java.lang.String DESCRIPTOR = "com.askey.zwave.control.IZwaveControlInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.askey.zwave.control.IZwaveControlInterface interface,
 * generating a proxy if needed.
 */
public static com.askey.zwave.control.IZwaveControlInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.askey.zwave.control.IZwaveControlInterface))) {
return ((com.askey.zwave.control.IZwaveControlInterface)iin);
}
return new com.askey.zwave.control.IZwaveControlInterface.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
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
case TRANSACTION_registerListener:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
this.registerListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unRegisterListener:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
this.unRegisterListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_openController:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
this.openController(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_addDevice:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _result = this.addDevice(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_removeDevice:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _result = this.removeDevice(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getDevices:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _result = this.getDevices(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getDeviceInfo:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.getDeviceInfo(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_removeFailedDevice:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.removeFailedDevice(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_replaceFailedDevice:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.replaceFailedDevice(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_stopAddDevice:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _result = this.stopAddDevice(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_stopRemoveDevice:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _result = this.stopRemoveDevice(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getDeviceBattery:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.getDeviceBattery(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSensorMultiLevel:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.getSensorMultiLevel(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_updateNode:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.updateNode(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_reNameDevice:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
java.lang.String _arg3;
_arg3 = data.readString();
int _result = this.reNameDevice(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setDefault:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _result = this.setDefault(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getConfiguration:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
int _arg5;
_arg5 = data.readInt();
int _arg6;
_arg6 = data.readInt();
int _result = this.getConfiguration(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setConfiguration:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
int _arg5;
_arg5 = data.readInt();
int _arg6;
_arg6 = data.readInt();
int _result = this.setConfiguration(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSupportedSwitchType:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.getSupportedSwitchType(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_startStopSwitchLevelChange:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
int _arg5;
_arg5 = data.readInt();
int _arg6;
_arg6 = data.readInt();
int _arg7;
_arg7 = data.readInt();
int _result = this.startStopSwitchLevelChange(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getPowerLevel:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.getPowerLevel(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setSwitchAllOn:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.setSwitchAllOn(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setSwitchAllOff:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.setSwitchAllOff(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getBasic:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.getBasic(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setBasic:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _result = this.setBasic(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSwitchMultiLevel:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _result = this.getSwitchMultiLevel(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setSwitchMultiLevel:
{
data.enforceInterface(DESCRIPTOR);
com.askey.zwave.control.IZwaveContrlCallBack _arg0;
_arg0 = com.askey.zwave.control.IZwaveContrlCallBack.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _result = this.setSwitchMultiLevel(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_closeController:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.closeController();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.askey.zwave.control.IZwaveControlInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void registerListener(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unRegisterListener(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unRegisterListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     *Initialize zwave server
     */
@Override public void openController(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_openController, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int addDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_addDevice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int removeDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_removeDevice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getDevices(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getDevices, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getDeviceInfo(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_getDeviceInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int removeFailedDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_removeFailedDevice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int replaceFailedDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_replaceFailedDevice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int stopAddDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_stopAddDevice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int stopRemoveDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_stopRemoveDevice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getDeviceBattery(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_getDeviceBattery, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSensorMultiLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_getSensorMultiLevel, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int updateNode(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_updateNode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int reNameDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, java.lang.String newName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeString(homeId);
_data.writeInt(deviceId);
_data.writeString(newName);
mRemote.transact(Stub.TRANSACTION_reNameDevice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setDefault(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setDefault, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getConfiguration(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, int paramMode, int paramNumber, int rangeStart, int rangeEnd) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeString(homeId);
_data.writeInt(deviceId);
_data.writeInt(paramMode);
_data.writeInt(paramNumber);
_data.writeInt(rangeStart);
_data.writeInt(rangeEnd);
mRemote.transact(Stub.TRANSACTION_getConfiguration, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setConfiguration(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, int paramNumber, int paramSize, int useDefault, int paramValue) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeString(homeId);
_data.writeInt(deviceId);
_data.writeInt(paramNumber);
_data.writeInt(paramSize);
_data.writeInt(useDefault);
_data.writeInt(paramValue);
mRemote.transact(Stub.TRANSACTION_setConfiguration, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSupportedSwitchType(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_getSupportedSwitchType, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int startStopSwitchLevelChange(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, int startLvlVal, int duration, int pmyChangeDir, int secChangeDir, int secStep) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeString(homeId);
_data.writeInt(deviceId);
_data.writeInt(startLvlVal);
_data.writeInt(duration);
_data.writeInt(pmyChangeDir);
_data.writeInt(secChangeDir);
_data.writeInt(secStep);
mRemote.transact(Stub.TRANSACTION_startStopSwitchLevelChange, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getPowerLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_getPowerLevel, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setSwitchAllOn(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_setSwitchAllOn, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setSwitchAllOff(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_setSwitchAllOff, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getBasic(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_getBasic, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setBasic(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId, int value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
_data.writeInt(value);
mRemote.transact(Stub.TRANSACTION_setBasic, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSwitchMultiLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
mRemote.transact(Stub.TRANSACTION_getSwitchMultiLevel, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setSwitchMultiLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId, int value, int duration) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
_data.writeInt(deviceId);
_data.writeInt(value);
_data.writeInt(duration);
mRemote.transact(Stub.TRANSACTION_setSwitchMultiLevel, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int closeController() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_closeController, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_registerListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unRegisterListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_openController = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_addDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_removeDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getDevices = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getDeviceInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_removeFailedDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_replaceFailedDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_stopAddDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_stopRemoveDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getDeviceBattery = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getSensorMultiLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_updateNode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_reNameDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_setDefault = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_getConfiguration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_setConfiguration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_getSupportedSwitchType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_startStopSwitchLevelChange = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_getPowerLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_setSwitchAllOn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_setSwitchAllOff = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_getBasic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_setBasic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_getSwitchMultiLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_setSwitchMultiLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_closeController = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
}
public void registerListener(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public void unRegisterListener(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
/**
     *Initialize zwave server
     */
public void openController(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public int addDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public int removeDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public int getDevices(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public int getDeviceInfo(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int removeFailedDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int replaceFailedDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int stopAddDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public int stopRemoveDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public int getDeviceBattery(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int getSensorMultiLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int updateNode(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int reNameDevice(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, java.lang.String newName) throws android.os.RemoteException;
public int setDefault(com.askey.zwave.control.IZwaveContrlCallBack callBack) throws android.os.RemoteException;
public int getConfiguration(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, int paramMode, int paramNumber, int rangeStart, int rangeEnd) throws android.os.RemoteException;
public int setConfiguration(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, int paramNumber, int paramSize, int useDefault, int paramValue) throws android.os.RemoteException;
public int getSupportedSwitchType(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int startStopSwitchLevelChange(com.askey.zwave.control.IZwaveContrlCallBack callBack, java.lang.String homeId, int deviceId, int startLvlVal, int duration, int pmyChangeDir, int secChangeDir, int secStep) throws android.os.RemoteException;
public int getPowerLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int setSwitchAllOn(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int setSwitchAllOff(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int getBasic(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int setBasic(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId, int value) throws android.os.RemoteException;
public int getSwitchMultiLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId) throws android.os.RemoteException;
public int setSwitchMultiLevel(com.askey.zwave.control.IZwaveContrlCallBack callBack, int deviceId, int value, int duration) throws android.os.RemoteException;
public int closeController() throws android.os.RemoteException;
}
