package com.askey.mobile.zwave.control.deviceContr.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/10/19 11:35
 * 修改人：skysoft
 * 修改时间：2017/10/19 11:35
 * 修改备注：
 */
public class ProvisionInfo implements Parcelable{

    private String dsk;

    public String getDsk() {
        return dsk;
    }

    public void setDsk(String dsk) {
        this.dsk = dsk;
    }

    public String getDeviceBootMode() {
        return deviceBootMode;
    }

    public void setDeviceBootMode(String deviceBootMode) {
        this.deviceBootMode = deviceBootMode;
    }

    public String getDeviceInclusionState() {
        return deviceInclusionState;
    }

    public void setDeviceInclusionState(String deviceInclusionState) {
        this.deviceInclusionState = deviceInclusionState;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceLocation() {
        return deviceLocation;
    }

    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    private String deviceBootMode;
    private String deviceInclusionState;
    private String deviceName;
    private String deviceLocation;


    public static final Creator<ProvisionInfo> CREATOR = new Creator<ProvisionInfo>() {
        @Override
        public ProvisionInfo createFromParcel(Parcel in) {
            ProvisionInfo provisionInfo = new ProvisionInfo();
            provisionInfo.dsk = in.readString();
            provisionInfo.deviceBootMode = in.readString();
            provisionInfo.deviceInclusionState = in.readString();
            provisionInfo.deviceName = in.readString();
            provisionInfo.deviceLocation = in.readString();
            return provisionInfo;
        }

        @Override
        public ProvisionInfo[] newArray(int size) {
            return new ProvisionInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(dsk);
        parcel.writeString(deviceBootMode);
        parcel.writeString(deviceInclusionState);
        parcel.writeString(deviceName);
        parcel.writeString(deviceLocation);
    }

        public String getGenericCls() {
            return genericCls;
        }

        public void setGenericCls(String genericCls) {
            this.genericCls = genericCls;
        }

        public String getIconType() {
            return iconType;
        }

        public void setIconType(String iconType) {
            this.iconType = iconType;
        }

        public String getSpecificCls() {
            return specificCls;
        }

        public void setSpecificCls(String specificCls) {
            this.specificCls = specificCls;
        }

        private String genericCls;
    private String specificCls;
    private String iconType;

        private String manufacturerId;
        private String productType;
        private String productId;
        private String appVersion;
        private String appSubVer;

        public String getManufacturerId() {
            return manufacturerId;
        }

        public void setManufacturerId(String manufacturerId) {
            this.manufacturerId = manufacturerId;
        }

        public String getProductType() {
            return productType;
        }

        public void setProductType(String productType) {
            this.productType = productType;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getAppSubVer() {
            return appSubVer;
        }

        public void setAppSubVer(String appSubVer) {
            this.appSubVer = appSubVer;
        }

        private String nodeId;
        private String status;

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
}
