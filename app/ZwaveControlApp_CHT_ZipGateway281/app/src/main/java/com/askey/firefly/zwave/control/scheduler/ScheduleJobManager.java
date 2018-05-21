package com.askey.firefly.zwave.control.scheduler;

import android.content.Context;
import android.util.Log;

import com.askey.firefly.zwave.control.dao.ZwaveSchedule;
import com.askey.firefly.zwave.control.dao.ZwaveScheduleManager;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by edison_chang on 11/22/2017.
 */

public class ScheduleJobManager {

    private static String LOG_TAG = ScheduleJobManager.class.getSimpleName();
    private static ScheduleJobManager instance;
    private Context context;
    private Timer timer = new Timer();
    private String[] weeks = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static long oneWeekMillions = 604800000;
    private ConcurrentHashMap<Integer, Vector<ScheduleJobTimerTask>> taskMap = new ConcurrentHashMap<Integer, Vector<ScheduleJobTimerTask>>();

    public ScheduleJobManager(Context context) {
        this.context = context;
        startScheduleJob();
    }

    public static ScheduleJobManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ScheduleJobManager.class) {
                instance = new ScheduleJobManager(context);
            }
        }
        return instance;
    }

    private void startScheduleJob() {
        Log.i(LOG_TAG, "Start Schedule Job");
        List<ZwaveSchedule> zwaveScheduleList = ZwaveScheduleManager.getInstance(context).getZwaveScheduleList();

        for (ZwaveSchedule zwaveSchedule: zwaveScheduleList) {
            if (zwaveSchedule.getActive().equals("true")) {
                long startTime = calculatePeriodicTime(zwaveSchedule.getStartTime(), zwaveSchedule.getDay());
                long endTime = calculatePeriodicTime(zwaveSchedule.getEndTime(), zwaveSchedule.getDay());

                setScheduleJob(zwaveSchedule, startTime, endTime);
            }
        }
    }

    private long calculatePeriodicTime(String time, String dayOfWeek) {
        String hr = time.split(":")[0];
        String min = time.split(":")[1];
        Log.i(LOG_TAG, "Hr = " + hr + " Min = " + min);
        for (int i = 0; i < weeks.length; i++) {
            if (weeks[i].equals(dayOfWeek)){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E a");
                Log.i(LOG_TAG, "Match Day = " + dayOfWeek);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR, Integer.valueOf(hr));
                calendar.set(Calendar.MINUTE, Integer.valueOf(min));
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hr));
                calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                long diffTime = calendar.getTimeInMillis() - new Date().getTime();
                if (diffTime < 0) {
                    Log.i(LOG_TAG, simpleDateFormat.format(calendar.getTime()));
                    Log.i(LOG_TAG, "Set schedule time less than current time");
                    diffTime = oneWeekMillions - Math.abs(diffTime);
                    Log.i(LOG_TAG, "Diff Time : " + diffTime);
                    return Math.abs(diffTime);
                }
                Log.i(LOG_TAG, simpleDateFormat.format(calendar.getTime()));
                Log.i(LOG_TAG, "Set schedule time exceed current time");
                Log.i(LOG_TAG, "Diff Time : " + diffTime);
                return diffTime;
            }
        }
        return 0;
    }

    public void addSchedule(int nodeId, int variableValueStart, int variableValueEnd, String startTime, String endTime, String day, String active) {
        long start = calculatePeriodicTime(startTime, day);
        long end = calculatePeriodicTime(endTime, day);
        ZwaveSchedule zwaveSchedule = new ZwaveSchedule();
        zwaveSchedule.setNodeId(nodeId);
        int jobId = Integer.valueOf(String.valueOf(nodeId) + String.valueOf(Arrays.asList(weeks).indexOf(day)));
        zwaveSchedule.setJobId(jobId++);
        zwaveSchedule.setVariableValueStart(variableValueStart);
        zwaveSchedule.setVariableValueEnd(variableValueEnd);
        zwaveSchedule.setStartTime(startTime);
        zwaveSchedule.setEndTime(endTime);
        zwaveSchedule.setDay(day);
        zwaveSchedule.setActive(active);
        ZwaveScheduleManager.getInstance(context).addZwaveSchedule(zwaveSchedule);
        if (active.equals("true")) {
            Log.i(LOG_TAG, "NodeId = " + nodeId + " variableValueStart = " + variableValueStart);
            Log.i(LOG_TAG, "NodeId = " + nodeId + " variableValueEnd = " + variableValueEnd);
            Log.i(LOG_TAG, "StartTime = " + startTime + " EndTime = " + endTime + " Day = " + day + " Active = " + active);
            Log.i(LOG_TAG, "start = " + start + " end = " + end);
            setScheduleJob(zwaveSchedule, start, end);
        }
    }

    public void setScheduleActive(int nodeId, String active) {
        ZwaveScheduleManager.getInstance(context).updateZwaveScheduleActive(nodeId, active);
        List<ZwaveSchedule> zwaveScheduleList = ZwaveScheduleManager.getInstance(context).getZwaveScheduleList(nodeId);
        if (active.equals("true")) {
            for (ZwaveSchedule zwaveSchedule : zwaveScheduleList) {
                long start = calculatePeriodicTime(zwaveSchedule.getStartTime(), zwaveSchedule.getDay());
                long end = calculatePeriodicTime(zwaveSchedule.getEndTime(), zwaveSchedule.getDay());

                setScheduleJob(zwaveSchedule, start, end);
            }
        } else {
            for (ZwaveSchedule zwaveSchedule : zwaveScheduleList) {
                cancelSchedule(zwaveSchedule.getJobId());
            }
        }
    }

    private void setScheduleJob(ZwaveSchedule zwaveSchedule, long startTime, long endTime) {
        doScheduleJob(zwaveSchedule, startTime, endTime, oneWeekMillions);
        //doScheduleJob(zwaveSchedule, endTime, oneWeekMillions);
    }

    private void doScheduleJob(ZwaveSchedule zwaveSchedule, long startDelay, long endDelay, long period) {
        ScheduleJobTimerTask startScheduleJob = new ScheduleJobTimerTask(zwaveSchedule.getNodeId(), zwaveSchedule.getVariableValueStart());
        ScheduleJobTimerTask endScheduleJob = new ScheduleJobTimerTask(zwaveSchedule.getNodeId(), zwaveSchedule.getVariableValueEnd());
        timer.schedule(startScheduleJob, startDelay, period);
        timer.schedule(endScheduleJob, endDelay, period);
        if (taskMap.containsKey(zwaveSchedule.getJobId())) {
            if (taskMap.get(zwaveSchedule.getJobId()) != null) {
                taskMap.get(zwaveSchedule.getJobId()).add(startScheduleJob);
                taskMap.get(zwaveSchedule.getJobId()).add(endScheduleJob);
                taskMap.put(zwaveSchedule.getJobId(), taskMap.get(zwaveSchedule.getJobId()));
            } else {
                Vector vec = new Vector();
                vec.add(startScheduleJob);
                vec.add(endScheduleJob);
                taskMap.put(zwaveSchedule.getJobId(), vec);
            }
        } else {
            Vector vec = new Vector();
            vec.add(startScheduleJob);
            vec.add(endScheduleJob);
            taskMap.put(zwaveSchedule.getJobId(), vec);
        }
    }

    public void cancelSchedule(int jobId) {
        //remove schedule from database first
        ZwaveScheduleManager.getInstance(context).deleteZwaveSchedule(jobId);
        if (taskMap.containsKey(jobId)) {
            Vector vec = taskMap.get(jobId);
            for (int i = 0; i < vec.size(); i++) {
                ((ScheduleJobTimerTask) vec.get(i)).cancel();
            }
            taskMap.remove(jobId);
        }
    }
}
