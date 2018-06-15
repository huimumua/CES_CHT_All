package com.askey.iotcontrol.scheduler;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;

/**
 * Created by edison_chang on 11/22/2017.
 */

public class ScheduleJob {

    private Context context;
    private int nodeId;
    private int jobId;
    private int variableValue;
    private long periodic;
    private boolean repeat = false;

    public ScheduleJob(Context context, int nodeId, int jobId, int variableValue, long periodic, boolean repeat) {
        this.context = context;
        this.nodeId = nodeId;
        this.jobId = jobId;
        this.variableValue = variableValue;
        this.periodic = periodic;
        this.repeat = repeat;
    }

    public JobInfo getJob() {
        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(context.getPackageName(), SchedulerService.class.getName()));
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putInt("nodeId", nodeId);
        persistableBundle.putInt("variableValue", variableValue);
        persistableBundle.putBoolean("repeat", repeat);
        builder.setExtras(persistableBundle);
        builder.setPeriodic(periodic);
        Log.i("SCHEDULE_JOB", " NodeId = " + nodeId + " VariableValue = " + variableValue + " Periodic = " + periodic + " Repeat = " + repeat);
        return builder.build();
    }
}