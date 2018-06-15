package com.askey.iotcontrol.scheduler;

import com.askey.iotcontrol.jni.ZwaveControlHelper;
import com.askey.iotcontrol.utils.Logg;

import java.util.TimerTask;

/**
 * Created by edison_chang on 11/22/2017.
 */

public class ScheduleJobTimerTask extends TimerTask {

    private static String TAG = ScheduleJobTimerTask.class.getSimpleName();

    private int nodeId;
    private int variableValue;
    //private boolean repeat = false;

    public ScheduleJobTimerTask(int nodeId, int variableValue/*, boolean repeat*/) {
        this.nodeId = nodeId;
        this.variableValue = variableValue;
        //this.repeat = repeat;
    }

    @Override
    public void run() {
        Logg.i(TAG, "Timer task running");
        Logg.i(TAG, "Timer task running nodeId = " + nodeId);
        Logg.i(TAG, "Timer task running variableValue = " + variableValue);
        ZwaveControlHelper.ZwController_SetBasic(nodeId, variableValue);

        //if (!repeat) {
        //    cancel();
        //}
    }
}