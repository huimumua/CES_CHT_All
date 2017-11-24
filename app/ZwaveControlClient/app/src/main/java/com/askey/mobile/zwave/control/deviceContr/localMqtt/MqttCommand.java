package com.askey.mobile.zwave.control.deviceContr.localMqtt;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/26 11:12
 * 修改人：skysoft
 * 修改时间：2017/7/26 11:12
 * 修改备注：
 */
public class MqttCommand {

    /**
     * 回调接口
     * @author Administrator
     *
     */
    public interface MqttCommandCallback {
        public void messageArrived(String str);
    }


}
