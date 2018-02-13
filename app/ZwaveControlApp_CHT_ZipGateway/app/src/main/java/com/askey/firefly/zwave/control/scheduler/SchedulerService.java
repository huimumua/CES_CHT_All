package com.askey.firefly.zwave.control.scheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;

import com.askey.firefly.zwave.control.jni.ZwaveControlHelper;
import com.askey.firefly.zwave.control.utils.Logg;

/**
 * Created by edison_chang on 11/20/2017.
 */

public class SchedulerService extends JobService {

    private static String TAG = SchedulerService.class.getSimpleName();

    private Handler schedulerServiceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Logg.i(TAG, "JobService task running");
            JobParameters params = (JobParameters) msg.obj;
            PersistableBundle persistableBundle = (PersistableBundle) params.getExtras();
            int nodeId = persistableBundle.getInt("nodeId");
            Logg.i(TAG, "JobService task running nodeId = " + nodeId);
            int variableValue = persistableBundle.getInt("variableValue");
            Logg.i(TAG, "JobService task running variableValue = " + variableValue);
            boolean repeat = persistableBundle.getBoolean("repeat");
            Logg.i(TAG, "JobService task running repeat = " + repeat);
            ZwaveControlHelper.ZwController_SetBasic(nodeId, variableValue);
            jobFinished((JobParameters) msg.obj, repeat);
            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        schedulerServiceHandler.sendMessage(Message.obtain(schedulerServiceHandler, 1, params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        schedulerServiceHandler.removeMessages(1);
        return false;
    }
}
