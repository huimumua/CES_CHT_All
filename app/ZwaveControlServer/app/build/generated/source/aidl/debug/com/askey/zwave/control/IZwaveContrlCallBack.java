/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Volumes/Data/chiapin/MQTT/ZwaveControlServer/app/src/main/aidl/com/askey/zwave/control/IZwaveContrlCallBack.aidl
 */
package com.askey.zwave.control;
// Declare any non-default types here with import statements

public interface IZwaveContrlCallBack extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.askey.zwave.control.IZwaveContrlCallBack
{
private static final java.lang.String DESCRIPTOR = "com.askey.zwave.control.IZwaveContrlCallBack";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.askey.zwave.control.IZwaveContrlCallBack interface,
 * generating a proxy if needed.
 */
public static com.askey.zwave.control.IZwaveContrlCallBack asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.askey.zwave.control.IZwaveContrlCallBack))) {
return ((com.askey.zwave.control.IZwaveContrlCallBack)iin);
}
return new com.askey.zwave.control.IZwaveContrlCallBack.Stub.Proxy(obj);
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
case TRANSACTION_addDeviceCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.addDeviceCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_openControlCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.openControlCallBack(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_removeDeviceCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.removeDeviceCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getDevicesCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getDevicesCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getDevicesInfoCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getDevicesInfoCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeFailedDeviceCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.removeFailedDeviceCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_replaceFailedDeviceCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.replaceFailedDeviceCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_stopAddDeviceCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.stopAddDeviceCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_stopRemoveDeviceCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.stopRemoveDeviceCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getDeviceBatteryCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getDeviceBatteryCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getSensorMultiLevelCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getSensorMultiLevelCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_updateNodeCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.updateNodeCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_reNameDeviceCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.reNameDeviceCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setDefaultCallBack:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setDefaultCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getConfiguration:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getConfiguration(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setConfiguration:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setConfiguration(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getSupportedSwitchType:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getSupportedSwitchType(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_startStopSwitchLevelChange:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.startStopSwitchLevelChange(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getPowerLevel:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getPowerLevel(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setSwitchAllOn:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setSwitchAllOn(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setSwitchAllOff:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setSwitchAllOff(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getBasic:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getBasic(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setBasic:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setBasic(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getSwitchMultiLevel:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getSwitchMultiLevel(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setSwitchMultiLevel:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setSwitchMultiLevel(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.askey.zwave.control.IZwaveContrlCallBack
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
@Override public void addDeviceCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_addDeviceCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void openControlCallBack(java.lang.String result, int length) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
_data.writeInt(length);
mRemote.transact(Stub.TRANSACTION_openControlCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void removeDeviceCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_removeDeviceCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getDevicesCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getDevicesCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getDevicesInfoCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getDevicesInfoCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void removeFailedDeviceCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_removeFailedDeviceCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void replaceFailedDeviceCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_replaceFailedDeviceCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopAddDeviceCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_stopAddDeviceCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopRemoveDeviceCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_stopRemoveDeviceCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getDeviceBatteryCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getDeviceBatteryCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getSensorMultiLevelCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getSensorMultiLevelCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void updateNodeCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_updateNodeCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void reNameDeviceCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_reNameDeviceCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setDefaultCallBack(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_setDefaultCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getConfiguration(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getConfiguration, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setConfiguration(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_setConfiguration, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getSupportedSwitchType(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getSupportedSwitchType, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void startStopSwitchLevelChange(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_startStopSwitchLevelChange, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getPowerLevel(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getPowerLevel, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setSwitchAllOn(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_setSwitchAllOn, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setSwitchAllOff(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_setSwitchAllOff, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getBasic(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getBasic, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setBasic(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_setBasic, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getSwitchMultiLevel(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_getSwitchMultiLevel, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setSwitchMultiLevel(java.lang.String result) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(result);
mRemote.transact(Stub.TRANSACTION_setSwitchMultiLevel, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_addDeviceCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_openControlCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_removeDeviceCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getDevicesCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getDevicesInfoCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_removeFailedDeviceCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_replaceFailedDeviceCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_stopAddDeviceCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_stopRemoveDeviceCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getDeviceBatteryCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getSensorMultiLevelCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_updateNodeCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_reNameDeviceCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_setDefaultCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_getConfiguration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_setConfiguration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_getSupportedSwitchType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_startStopSwitchLevelChange = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_getPowerLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_setSwitchAllOn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_setSwitchAllOff = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_getBasic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_setBasic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_getSwitchMultiLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_setSwitchMultiLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
}
public void addDeviceCallBack(java.lang.String result) throws android.os.RemoteException;
public void openControlCallBack(java.lang.String result, int length) throws android.os.RemoteException;
public void removeDeviceCallBack(java.lang.String result) throws android.os.RemoteException;
public void getDevicesCallBack(java.lang.String result) throws android.os.RemoteException;
public void getDevicesInfoCallBack(java.lang.String result) throws android.os.RemoteException;
public void removeFailedDeviceCallBack(java.lang.String result) throws android.os.RemoteException;
public void replaceFailedDeviceCallBack(java.lang.String result) throws android.os.RemoteException;
public void stopAddDeviceCallBack(java.lang.String result) throws android.os.RemoteException;
public void stopRemoveDeviceCallBack(java.lang.String result) throws android.os.RemoteException;
public void getDeviceBatteryCallBack(java.lang.String result) throws android.os.RemoteException;
public void getSensorMultiLevelCallBack(java.lang.String result) throws android.os.RemoteException;
public void updateNodeCallBack(java.lang.String result) throws android.os.RemoteException;
public void reNameDeviceCallBack(java.lang.String result) throws android.os.RemoteException;
public void setDefaultCallBack(java.lang.String result) throws android.os.RemoteException;
public void getConfiguration(java.lang.String result) throws android.os.RemoteException;
public void setConfiguration(java.lang.String result) throws android.os.RemoteException;
public void getSupportedSwitchType(java.lang.String result) throws android.os.RemoteException;
public void startStopSwitchLevelChange(java.lang.String result) throws android.os.RemoteException;
public void getPowerLevel(java.lang.String result) throws android.os.RemoteException;
public void setSwitchAllOn(java.lang.String result) throws android.os.RemoteException;
public void setSwitchAllOff(java.lang.String result) throws android.os.RemoteException;
public void getBasic(java.lang.String result) throws android.os.RemoteException;
public void setBasic(java.lang.String result) throws android.os.RemoteException;
public void getSwitchMultiLevel(java.lang.String result) throws android.os.RemoteException;
public void setSwitchMultiLevel(java.lang.String result) throws android.os.RemoteException;
}
