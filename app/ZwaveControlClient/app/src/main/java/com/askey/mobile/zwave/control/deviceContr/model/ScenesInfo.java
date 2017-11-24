package com.askey.mobile.zwave.control.deviceContr.model;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/10/19 11:35
 * 修改人：skysoft
 * 修改时间：2017/10/19 11:35
 * 修改备注：
 */
public class ScenesInfo {
    /**
     * 情景模式Id
     * */
    private String ScenesId;
    /**
     * 情景模式名称
     * */
    private String ScenesName;

    public ScenesInfo(String scenesId, String scenesName) {
        ScenesId = scenesId;
        ScenesName = scenesName;
    }

    public String getScenesId() {
        return ScenesId;
    }

    public void setScenesId(String scenesId) {
        ScenesId = scenesId;
    }

    public String getScenesName() {
        return ScenesName;
    }

    public void setScenesName(String scenesName) {
        ScenesName = scenesName;
    }
}
