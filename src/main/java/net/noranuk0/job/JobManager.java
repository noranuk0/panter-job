package net.noranuk0.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JobManager.java
 * <p/>
 * Copyright (c) 2016 Hiroyuki Mizuhara
 * <p/>
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
public class JobManager {
    public static final int PRIORITY_MIN = 0;
    public static final int PRIORITY_BELOW_NORMAL = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_ABOVE_NORMAL = 3;
    public static final int PRIORITY_MAX = 4;

    public static final int MAX_JOB_GROUP_ID = 30;

    @SuppressWarnings("unchecked")
    private final List<Job>[] priorityJobList = new ArrayList[PRIORITY_MAX + 1];

    private final long[] jobGroupSuspendLimitTimes = new long[MAX_JOB_GROUP_ID];
    private int currentEnabledJobGroupMask = 0x7fffffff;

    private static JobManager instance;
    private Worker worker;

    private static void initialize() {
        instance = new JobManager();
    }

    public static JobManager instance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    private class Worker extends Thread {
        public Worker() {
            super("Job#WorkerThread");
        }

        private Job get() {

            final int[] workJobSizeArray = new int[PRIORITY_MAX + 1];
            final long[] workJobGroupSuspendLimitTimes = new long[MAX_JOB_GROUP_ID];
            Job result = null;
            while (result == null) {
                long nextScheduledJobTime = Long.MAX_VALUE;
                long currentTimeMillis = System.currentTimeMillis();
                synchronized (priorityJobList) {
                    for (int priority = PRIORITY_MAX; priority >= PRIORITY_MIN; priority--) {
                        workJobSizeArray[priority] = priorityJobList[priority].size();
                    }
                }
                int workActiveJobGroupMask;
                synchronized (JobManager.this) {
                    workActiveJobGroupMask = JobManager.this.currentEnabledJobGroupMask;
                    System.arraycopy(
                            JobManager.this.jobGroupSuspendLimitTimes, 0,
                            workJobGroupSuspendLimitTimes,
                            0, workJobGroupSuspendLimitTimes.length);
                }
                for (int index = 0; index < MAX_JOB_GROUP_ID; index++) {
                    if (workJobGroupSuspendLimitTimes[index] <= currentTimeMillis) {
                        workJobGroupSuspendLimitTimes[index] = 0;
                    } else {
                        if (workJobGroupSuspendLimitTimes[index] < nextScheduledJobTime) {
                            nextScheduledJobTime = workJobGroupSuspendLimitTimes[index];
                        }
                        workActiveJobGroupMask &= ~(1 << index);
                    }
                }
                for (int priority = PRIORITY_MAX; priority >= PRIORITY_MIN; priority--) {
                    List<Job> targetList = priorityJobList[priority];
                    for (int index = 0; index < workJobSizeArray[priority]; index++) {
                        Job target = targetList.get(index);
                        if ((target.getJobGroupMask() & workActiveJobGroupMask)
                                != target.getJobGroupMask()) {
                            continue;
                        }
                        if ((target.getJobGroupMask() & workActiveJobGroupMask)
                                != target.getJobGroupMask()) {
                            continue;
                        }
                        if (nextScheduledJobTime > target.getNextExecuteSystemTime()) {
                            nextScheduledJobTime = target.getNextExecuteSystemTime();
                        }
                        if (target.getNextExecuteSystemTime() > currentTimeMillis) {
                            continue;
                        }
                        targetList.remove(index);
                        target.setNextScheduleTime(0);
                        result = target;
                        break;
                    }
                    if (result != null) {
                        break;
                    }
                }
                if (result == null) {
                    try {
                        synchronized (worker) {
                            if (nextScheduledJobTime < Long.MAX_VALUE) {
                                currentTimeMillis = System.currentTimeMillis();
                                wait(nextScheduledJobTime - currentTimeMillis);
                            } else {
                                wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        public void run() {
            while (true) {
                final long systemTime = System.currentTimeMillis();
                Job target = get();
                Callback<Job> callback = target.getCallback();
                try {
                    boolean result =
                            target.execute();
                    if (result) {
                        if (callback != null) {
                            callback.success(target);
                        }
                    } else {
                        if (callback != null) {
                            callback.fail(target);
                        }
                    }
                } catch (Exception e) {
                    try {
                        if (callback != null) {
                            callback.exception(target, e);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (target.getNextExecuteSystemTime() >= systemTime) {
                    JobManager.this.register(target);
                }
            }
        }
    }

    private JobManager() {
        for (int index = PRIORITY_MIN; index <= PRIORITY_MAX; index++) {
            priorityJobList[index] = new ArrayList<>();
        }
        this.worker = new Worker();
        worker.start();
    }

    public void register(Job job) {
        synchronized (priorityJobList) {
            final List<Job> targetList = priorityJobList[job.getPriority()];
            targetList.add(job);
        }
        synchronized (worker) {
            worker.notify();
        }
    }

    public void suspendGroup(int jobGroupId, long wakeUpTime) {
        long current = System.currentTimeMillis();
        synchronized (this) {
            jobGroupSuspendLimitTimes[jobGroupId] = wakeUpTime;
        }
        if (wakeUpTime <= current) {
            synchronized (worker) {
                worker.notify();
            }
        }
    }

    public Map<Class<?>, Integer> count() {
        Map<Class<?>, Integer> result = new HashMap<>();
        synchronized(this) {
            for (List<Job> listJob : priorityJobList) {
                for (Job job : listJob) {
                    if (result.get(job.getClass()) == null) {
                        result.put(job.getClass(), 0);
                    }
                    result.put(job.getClass(), result.get(job.getClass()) + 1);
                }
            }
        }
        return result;
    }
    
    public void enableGroup(int groupId) {
        synchronized (this) {
            currentEnabledJobGroupMask |= (1 << groupId);
        }
        synchronized (worker) {
            worker.notify();
        }
    }

    public void disableGroup(int groupId) {
        synchronized (this) {
            currentEnabledJobGroupMask &= ~(1 << groupId);
        }
    }

    public void cleanUp() {
        synchronized (this) {
            JobManager.instance = null;
            JobManager.initialize();
        }
    }
}
