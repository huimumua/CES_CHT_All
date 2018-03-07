package com.askey.mobile.zwave.control.deviceContr.dao;

import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/10/19 13:23
 * 修改人：skysoft
 * 修改时间：2017/10/19 13:23
 * 修改备注：
 */
public class DeviceDao {

    public static List<DeviceInfo> getAllDeviceInfo() {
        List<DeviceInfo> devcieList = new ArrayList<DeviceInfo>();

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceId("TEU5PYXNES2QHCOE2MJZU7IX2A");
        deviceInfo.setBrand("Aeotec");
        deviceInfo.setDeviceModel("FIREFLY");
        deviceInfo.setUniqueId("be31eb33253d1cc718");
        deviceInfo.setDeviceType("BULB");
        deviceInfo.setDisplayName("SwitchTest");
        deviceInfo.setScenesId("0");
        deviceInfo.setRoomId("0");//My Home
        deviceInfo.setRooms("MyHome");
        deviceInfo.setIsFavorite("1");// 1 表示favorite
        deviceInfo.setIsRemove("0");
        deviceInfo.setRecentlyUseTime("223456633");
        deviceInfo.setTriggerCondition("-1");      /*** 通知触发条件  -1 没有触发 0 High Consumption  1 Atypical Consumption  2 device switched on  3 switch switched on */
        deviceInfo.setTriggerType("3");  /**通知类型  -1 不做任何推送  0 push Notification  1 Email  3 push Notification and Email * */
        deviceInfo.setScheduleStartTime("223456633");
        deviceInfo.setScheduleEndTime("223456633");
        deviceInfo.setScheduleDurationTime("5");
        deviceInfo.setIsLookInhabited("1");       /**是否模拟家中有人 0 不开启  1 开启* */
        deviceInfo.setDeviceTriggersType("2");  /**触发设备类型 -1 都关闭不触发 0 if leaving 离开时关闭 1 if entering 返回时打开 2  都打开触发* */
        devcieList.add(deviceInfo);

        DeviceInfo deviceInfo4 = new DeviceInfo();
        deviceInfo4.setDeviceId("TEU5PYXNES2QHCOE2MJZU7IX2A");
        deviceInfo4.setBrand("Aeotec");
        deviceInfo4.setDeviceModel("FIREFLY");
        deviceInfo4.setUniqueId("be31eb33253d1cc718");
        deviceInfo4.setDeviceType("WALLMOTE");
        deviceInfo4.setDisplayName("SwitchTest");
        deviceInfo4.setScenesId("0");
        deviceInfo4.setRoomId("0");//My Home
        deviceInfo4.setRooms("MyHome");
        deviceInfo4.setIsFavorite("1");
        deviceInfo4.setIsRemove("0");
        deviceInfo4.setRecentlyUseTime("223456634");
        deviceInfo4.setTriggerCondition("-1");      /*** 通知触发条件  -1 没有触发 0 High Consumption  1 Atypical Consumption  2 device switched on  3 switch switched on */
        deviceInfo4.setTriggerType("3");  /**通知类型  -1 不做任何推送  0 push Notification  1 Email  3 push Notification and Email * */
        deviceInfo4.setScheduleStartTime("223456633");
        deviceInfo4.setScheduleEndTime("223456633");
        deviceInfo4.setScheduleDurationTime("5");
        deviceInfo4.setIsLookInhabited("1");       /**是否模拟家中有人 0 不开启  1 开启* */
        deviceInfo4.setDeviceTriggersType("2");  /**触发设备类型 -1 都关闭不触发 0 if leaving 离开时关闭 1 if entering 返回时打开 2  都打开触发* */
        devcieList.add(deviceInfo4);

        DeviceInfo deviceInfo1 = new DeviceInfo();
        deviceInfo1.setDeviceId("TEU5PYXNES2QHCOE2MJZU7IX2A");
        deviceInfo1.setBrand("Aeotec");
        deviceInfo1.setDeviceModel("FIREFLY");
        deviceInfo1.setUniqueId("be31eb33253d1cc718");
        deviceInfo1.setDeviceType("DIMMER");
        deviceInfo1.setDisplayName("DinnerTest");
        deviceInfo1.setScenesId("0");
        deviceInfo1.setRoomId("1");//My Home
        deviceInfo1.setRooms("LivingRoom");
        deviceInfo1.setIsFavorite("1");
        deviceInfo1.setIsRemove("0");
        deviceInfo1.setRecentlyUseTime("223456635");
        deviceInfo1.setTriggerCondition("-1");      /*** 通知触发条件  -1 没有触发 0 High Consumption  1 Atypical Consumption  2 device switched on  3 switch switched on */
        deviceInfo1.setTriggerType("3");  /**通知类型  -1 不做任何推送  0 push Notification  1 Email  3 push Notification and Email * */
        deviceInfo1.setScheduleStartTime("223456633");
        deviceInfo1.setScheduleEndTime("223456633");
        deviceInfo1.setScheduleDurationTime("5");
        deviceInfo1.setIsLookInhabited("1");       /**是否模拟家中有人 0 不开启  1 开启* */
        deviceInfo1.setDeviceTriggersType("2");  /**触发设备类型 -1 都关闭不触发 0 if leaving 离开时关闭 1 if entering 返回时打开 2  都打开触发* */
        devcieList.add(deviceInfo1);


        DeviceInfo deviceInfo2 = new DeviceInfo();
        deviceInfo2.setDeviceId("TEU5PYXNES2QHCOE2MJZU7IX2A");
        deviceInfo2.setBrand("Aeotec");
        deviceInfo2.setDeviceModel("FIREFLY");
        deviceInfo2.setUniqueId("be31eb33253d1cc718");
        deviceInfo2.setDeviceType("WallMote");
        deviceInfo2.setDisplayName("WallMoteTest");
        deviceInfo2.setScenesId("0");
        deviceInfo2.setRoomId("1");//My Home
        deviceInfo2.setRooms("LivingRoom");
        deviceInfo2.setIsFavorite("1");
        deviceInfo2.setIsRemove("0");
        deviceInfo2.setRecentlyUseTime("223456636");
        deviceInfo2.setTriggerCondition("-1");      /*** 通知触发条件  -1 没有触发 0 High Consumption  1 Atypical Consumption  2 device switched on  3 switch switched on */
        deviceInfo2.setTriggerType("3");  /**通知类型  -1 不做任何推送  0 push Notification  1 Email  3 push Notification and Email * */
        deviceInfo2.setScheduleStartTime("223456633");
        deviceInfo2.setScheduleEndTime("223456633");
        deviceInfo2.setScheduleDurationTime("5");
        deviceInfo2.setIsLookInhabited("1");       /**是否模拟家中有人 0 不开启  1 开启* */
        deviceInfo2.setDeviceTriggersType("2");  /**触发设备类型 -1 都关闭不触发 0 if leaving 离开时关闭 1 if entering 返回时打开 2  都打开触发* */
        devcieList.add(deviceInfo2);

        DeviceInfo deviceInfo3 = new DeviceInfo();
        deviceInfo3.setDeviceId("TEU5PYXNES2QHCOE2MJZU7IX2A");
        deviceInfo3.setBrand("Aeotec");
        deviceInfo3.setDeviceModel("FIREFLY");
        deviceInfo3.setUniqueId("be31eb33253d1cc718");
        deviceInfo3.setDeviceType("Dimmer");
        deviceInfo3.setDisplayName("DimmerTest");
        deviceInfo3.setScenesId("0");
        deviceInfo3.setRoomId("1");//My Home
        deviceInfo3.setRooms("LivingRoom");
        deviceInfo3.setIsFavorite("0");
        deviceInfo3.setIsRemove("0");
        deviceInfo3.setRecentlyUseTime("223456637");
        deviceInfo3.setTriggerCondition("-1");      /*** 通知触发条件  -1 没有触发 0 High Consumption  1 Atypical Consumption  2 device switched on  3 switch switched on */
        deviceInfo3.setTriggerType("3");  /**通知类型  -1 不做任何推送  0 push Notification  1 Email  3 push Notification and Email * */
        deviceInfo3.setScheduleStartTime("223456633");
        deviceInfo3.setScheduleEndTime("223456633");
        deviceInfo3.setScheduleDurationTime("5");
        deviceInfo3.setIsLookInhabited("1");       /**是否模拟家中有人 0 不开启  1 开启* */
        deviceInfo3.setDeviceTriggersType("2");  /**触发设备类型 -1 都关闭不触发 0 if leaving 离开时关闭 1 if entering 返回时打开 2  都打开触发* */
        devcieList.add(deviceInfo3);

        return devcieList;
    }


}
