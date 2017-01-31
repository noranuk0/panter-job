package net.noranuk0.job;

/**
 * Job.java
 * <p/>
 * Copyright (c) 2016 Hiroyuki Mizuhara
 * <p/>
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
public abstract class Job {
    public int getPriority() {
        return priority;
    }

    public int getJobGroupMask() {
        return jobGroupMask;
    }

    public long getNextExecuteSystemTime() {
        return nextExecuteSystemTime;
    }

    public void setNextScheduleTime(long nextExecuteSystemTime) {
        this.nextExecuteSystemTime = nextExecuteSystemTime;
    }

    public Callback getCallback() {
        return callback;
    }

    public static class Builder {
        private int priority = JobManager.PRIORITY_NORMAL;
        private int jobGroupMask = 0;
        private long nextExecuteSystemTime = 0;
        private Callback callback;

        private Builder(int priority) {
            if (priority >= JobManager.PRIORITY_MIN && priority <= JobManager.PRIORITY_MAX) {
                this.priority = priority;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public static Builder create(int priority) {
            return new Builder(priority);
        }

        public Builder nextExecuteSystemTime(long systemTime) {
            this.nextExecuteSystemTime = systemTime;
            return this;
        }

        public Builder addGroup(int addGroupIndex) {
            if (addGroupIndex >= 0 && addGroupIndex <= JobManager.MAX_JOB_GROUP_ID) {
                this.jobGroupMask |= (1 << addGroupIndex);
                return this;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public Builder callback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public int getPriority() {
            return priority;
        }

        public int getJobGroupMask() {
            return jobGroupMask;
        }

        public long nextExecuteSystemTime() {
            return nextExecuteSystemTime;
        }

        public Callback getCallback() {
            return callback;
        }
    }

    public Job(Builder builder) {
        this.priority = builder.getPriority();
        this.jobGroupMask = builder.getJobGroupMask();
        this.nextExecuteSystemTime = builder.nextExecuteSystemTime();
        this.callback = builder.getCallback();
    }

    protected final int priority;
    protected final int jobGroupMask;
    protected long nextExecuteSystemTime;
    protected final Callback callback;

    public String description() {
        return this.getClass().getSimpleName();
    }

    public boolean containJobGroup(int groupId) {
        return ((jobGroupMask >> groupId) & 0x1) != 0;
    }

    public abstract boolean execute() throws Exception;
}
