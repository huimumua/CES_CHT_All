package com.askey.iotcontrol.scene;

import android.graphics.Color;

import com.askey.iotcontrol.dao.ZwaveDeviceScene;
import com.askey.iotcontrol.jni.ZwaveControlHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by edison_chang on 11/30/2017.
 */

public class SceneTimer {

    /*private enum ACTION {
        SWITCH_ON,
        SWITCH_OFF,
        TOGGLE
    }*/

    public SceneTimer(ZwaveDeviceScene zwaveDeviceScene, int currentVariableValue) {
        doSwitchOnOff(zwaveDeviceScene, currentVariableValue);
    }

    public SceneTimer(ZwaveDeviceScene zwaveDeviceScene, String currentWarm, String currentWhite, String currentRGB, int currentVariableValue) {
        doToggleScene(zwaveDeviceScene, currentWarm, currentWhite, currentRGB, currentVariableValue);
    }

    /*private void doScene(ZwaveDeviceScene zwaveDeviceScene, int currentVariableValue) {
        switch (ACTION.valueOf(zwaveDeviceScene.getAction())) {
            case SWITCH_ON:
                doSwitchOnOff(zwaveDeviceScene, currentVariableValue);
                break;
            case SWITCH_OFF:
                doSwitchOnOff(zwaveDeviceScene, currentVariableValue);
                break;
            case TOGGLE:
                doToggle(zwaveDeviceScene, currentVariableValue);
                break;
            default:
                break;
        }
    }*/

    private void doSwitchOnOff(ZwaveDeviceScene zwaveDeviceScene, int currentVariableValue) {
        String[] timerTimeList = zwaveDeviceScene.getTimerTime().split(":");
        if (timerTimeList.length > 1) {
            int hrs = Integer.valueOf(timerTimeList[0]);
            int mins = Integer.valueOf(timerTimeList[1]);
            int secs = Integer.valueOf(timerTimeList[2]);

            ZwaveControlHelper.ZwController_SetBasic(zwaveDeviceScene.getNodeId(), zwaveDeviceScene.getVariableValue());

            long timeSecs = (hrs * 60 * 60) + (mins * 60) + secs;

            Timer timer = new Timer();
            timer.schedule(new SceneTimerTask(zwaveDeviceScene, null, null, null, currentVariableValue), timeSecs * 1000);
        } else {
            ZwaveControlHelper.ZwController_SetBasic(zwaveDeviceScene.getNodeId(), zwaveDeviceScene.getVariableValue());
        }
    }

    private void doToggleScene(ZwaveDeviceScene zwaveDeviceScene, String currentWarm, String currentWhite, String currentRGB, int currentVariableValue) {
        String[] timerTimeList = zwaveDeviceScene.getTimerTime().split(":");
        if (timerTimeList.length > 1) {
            int hrs = Integer.valueOf(timerTimeList[0]);
            int mins = Integer.valueOf(timerTimeList[1]);
            int secs = Integer.valueOf(timerTimeList[2]);

            trunOnOffColorBulb(zwaveDeviceScene);

            long timeSecs = (hrs * 60 * 60) + (mins * 60) + secs;

            Timer timer = new Timer();
            timer.schedule(new SceneTimerTask(zwaveDeviceScene, currentWarm, currentWhite, currentRGB, currentVariableValue), timeSecs * 1000);
        } else {
            trunOnOffColorBulb(zwaveDeviceScene);
        }
    }

    private void trunOnOffColorBulb(ZwaveDeviceScene zwaveDeviceScene) {
        if (zwaveDeviceScene.getWarm() != null) {
            ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x00, Integer.valueOf(zwaveDeviceScene.getWarm()));
        } else if (zwaveDeviceScene.getWhite() != null) {
            ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x01, Integer.valueOf(zwaveDeviceScene.getWhite()));
        } else if (zwaveDeviceScene.getRgb() != null) {
            String[] rgbs = zwaveDeviceScene.getRgb().split(",");
            ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x02, Color.red(Integer.valueOf(rgbs[0])));
            ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x03, Color.green(Integer.valueOf(rgbs[1])));
            ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x04, Color.blue(Integer.valueOf(rgbs[2])));
        }
        ZwaveControlHelper.ZwController_SetBasic(zwaveDeviceScene.getNodeId(), zwaveDeviceScene.getVariableValue());
    }

    private class SceneTimerTask extends TimerTask {

        private ZwaveDeviceScene zwaveDeviceScene;
        private int currentVariableValue;
        private String currentWarm;
        private String currentWhite;
        private String currentRGB;

        public SceneTimerTask(ZwaveDeviceScene zwaveDeviceScene, String currentWarm, String currentWhite,
                              String currentRGB, int currentVariableValue) {
            this.zwaveDeviceScene = zwaveDeviceScene;
            this.currentWarm = currentWarm;
            this.currentWhite = currentWhite;
            this.currentRGB = currentRGB;
            this.currentVariableValue = currentVariableValue;
        }

        @Override
        public void run() {
            if (currentWarm != null) {
                ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x00, Integer.valueOf(currentWarm));
            } else if (currentWhite != null) {
                ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x01, Integer.valueOf(currentWhite));
            } else if (currentRGB != null) {
                String[] rgbs = currentRGB.split(",");
                ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x02, Color.red(Integer.valueOf(rgbs[0])));
                ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x03, Color.green(Integer.valueOf(rgbs[1])));
                ZwaveControlHelper.ZwController_setSwitchColor(zwaveDeviceScene.getNodeId(), 0x04, Color.blue(Integer.valueOf(rgbs[2])));
            }
            ZwaveControlHelper.ZwController_SetBasic(zwaveDeviceScene.getNodeId(), currentVariableValue);
        }
    }
}
