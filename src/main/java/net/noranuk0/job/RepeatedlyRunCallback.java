package net.noranuk0.job;

/**
 * RepeatedlyRunCallback.java
 * <p/>
 * Copyright (c) 2016 Hiroyuki Mizuhara
 * <p/>
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
public class RepeatedlyRunCallback<T extends Job> extends Callback<T> {
    private int delayTimeIfSuccessful;
    private int delayTimeIfFailed;
    private int delayTimeIfException;

    public RepeatedlyRunCallback(int delayTimeIfSuccessful,
                                 int delayTimeIfFailed,
                                 int delayTimeIfException) {
        this.delayTimeIfSuccessful = delayTimeIfSuccessful;
        this.delayTimeIfFailed = delayTimeIfFailed;
        this.delayTimeIfException = delayTimeIfException;
    }

    public void success(T sender) {
        if (delayTimeIfSuccessful > 0) {
            long current = System.currentTimeMillis();
            sender.setNextScheduleTime(current + delayTimeIfSuccessful);
        }
    }

    @Override
    public void fail(T sender) {
        if (delayTimeIfFailed > 0) {
            long current = System.currentTimeMillis();
            sender.setNextScheduleTime(current + delayTimeIfFailed);
        }
    }

    @Override
    public void exception(T sender, Exception e) {
        if (delayTimeIfException > 0) {
            long current = System.currentTimeMillis();
            sender.setNextScheduleTime(current + delayTimeIfException);
        }
    }
}
